
package com.erpflow.service;

import com.erpflow.dao.SalesReturnDAO;
import com.erpflow.util.TransactionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SalesReturnService {

    private final SalesReturnDAO dao = new SalesReturnDAO();

    public List<Map<String, Object>> getEligibleLines(int salesOrderId) {
        if (salesOrderId <= 0) {
            throw new IllegalArgumentException("Invalid sales order ID");
        }

        if (dao.lockSalesOrder(salesOrderId) == null) {
            throw new IllegalArgumentException("Sales order not found");
        }

        return dao.findEligibleLines(salesOrderId);
    }

    public List<Map<String, Object>> getAllReturns() {
        return dao.findAll();
    }

    public Map<String, Object> getReturnById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid sales return ID");
        }

        Map<String, Object> result = dao.findById(id);

        if (result == null) {
            throw new IllegalArgumentException("Sales return not found");
        }

        return result;
    }

    public Map<String, Object> createReturn(Map<String, Object> request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        int salesOrderId = positiveInt(request.get("salesOrderId"), "salesOrderId");

        Object rawItems = request.get("items");
        if (!(rawItems instanceof List<?> requestedItems) || requestedItems.isEmpty()) {
            throw new IllegalArgumentException("At least one return item is required");
        }

        String headerReason = optionalText(request.get("reason"));
        String notes = optionalText(request.get("notes"));

        LocalDate returnDate = LocalDate.now();
        Object dateValue = request.get("returnDate");
        if (dateValue != null && !dateValue.toString().isBlank()) {
            try {
                returnDate = LocalDate.parse(dateValue.toString());
            } catch (Exception e) {
                throw new IllegalArgumentException("returnDate must use YYYY-MM-DD format");
            }
        }

        final LocalDate finalReturnDate = returnDate;

        // Validate request structure before opening the transaction.
        Map<Integer, Integer> requestedQuantities = new LinkedHashMap<>();
        Map<Integer, String> lineReasons = new HashMap<>();

        for (Object raw : requestedItems) {
            if (!(raw instanceof Map<?, ?> item)) {
                throw new IllegalArgumentException("Each item must be a JSON object");
            }

            int orderLineId = positiveInt(
                item.get("salesOrderItemId"), "salesOrderItemId"
            );
            int quantity = positiveInt(item.get("quantity"), "quantity");

            if (requestedQuantities.putIfAbsent(orderLineId, quantity) != null) {
                throw new IllegalArgumentException(
                    "Duplicate salesOrderItemId in request: " + orderLineId
                );
            }

            lineReasons.put(orderLineId, optionalText(item.get("reason")));
        }

        return TransactionManager.execute(() -> {
            Map<String, Object> order = dao.lockSalesOrder(salesOrderId);

            if (order == null) {
                throw new IllegalArgumentException("Sales order not found");
            }

            int customerId = (Integer) order.get("customerId");

            // Lock all order lines in a consistent order.
            Map<Integer, Map<String, Object>> orderLines =
                dao.findOrderLinesForUpdate(salesOrderId);

            if (orderLines.isEmpty()) {
                throw new IllegalStateException("Sales order has no line items");
            }

            // This is based on sales_return_items detail rows, not header counts.
            Map<Integer, Integer> reserved =
                dao.findReservedQuantities(salesOrderId);

            BigDecimal total = BigDecimal.ZERO;

            for (Map.Entry<Integer, Integer> entry : requestedQuantities.entrySet()) {
                int lineId = entry.getKey();
                int requestedQty = entry.getValue();

                Map<String, Object> line = orderLines.get(lineId);
                if (line == null) {
                    throw new IllegalArgumentException(
                        "Sales order line " + lineId +
                        " does not belong to sales order " + salesOrderId
                    );
                }

                int shippedQty = (Integer) line.get("shippedQuantity");
                int alreadyReserved = reserved.getOrDefault(lineId, 0);
                int returnableQty = Math.max(0, shippedQty - alreadyReserved);

                if (requestedQty > returnableQty) {
                    throw new IllegalArgumentException(
                        "Cannot return " + requestedQty + " units for order line " +
                        lineId + ". Returnable quantity is " + returnableQty +
                        " (shipped: " + shippedQty +
                        ", already reserved/returned: " + alreadyReserved + ")"
                    );
                }

                BigDecimal unitPrice = (BigDecimal) line.get("unitPrice");
                BigDecimal amount = unitPrice
                    .multiply(BigDecimal.valueOf(requestedQty))
                    .setScale(2, RoundingMode.HALF_UP);

                total = total.add(amount);
            }

            total = total.setScale(2, RoundingMode.HALF_UP);

            String returnNumber = "SR-" +
                finalReturnDate.format(DateTimeFormatter.BASIC_ISO_DATE) +
                "-" + UUID.randomUUID().toString()
                    .replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);

            int returnId = dao.insertHeader(
                returnNumber,
                salesOrderId,
                customerId,
                Date.valueOf(finalReturnDate),
                total,
                headerReason,
                notes
            );

            for (Map.Entry<Integer, Integer> entry : requestedQuantities.entrySet()) {
                int lineId = entry.getKey();
                int quantity = entry.getValue();

                Map<String, Object> line = orderLines.get(lineId);
                long itemId = (Long) line.get("itemId");
                BigDecimal unitPrice = (BigDecimal) line.get("unitPrice");
                BigDecimal amount = unitPrice
                    .multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP);

                String reason = lineReasons.get(lineId);
                if (reason == null) reason = headerReason;

                dao.insertReturnItem(
                    returnId, lineId, itemId, quantity,
                    unitPrice, amount, reason
                );
            }

            Map<String, Object> created = dao.findById(returnId);
            if (created == null) {
                throw new IllegalStateException("Sales return was created but could not be read");
            }

            return created;
        });
    }

    public Map<String, Object> updateStatus(int id, String requestedStatus) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid sales return ID");
        }

        if (requestedStatus == null || requestedStatus.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }

        String newStatus = requestedStatus.trim().toUpperCase(Locale.ROOT);

        if (!Set.of("RECEIVED", "COMPLETED").contains(newStatus)) {
            throw new IllegalArgumentException(
                "Status must be RECEIVED or COMPLETED"
            );
        }

        return TransactionManager.execute(() -> {
            String current = dao.findStatusForUpdate(id);

            if (current == null) {
                throw new IllegalArgumentException("Sales return not found");
            }

            boolean valid =
                ("CREATED".equals(current) && "RECEIVED".equals(newStatus)) ||
                ("RECEIVED".equals(current) && "COMPLETED".equals(newStatus));

            if (!valid) {
                throw new IllegalStateException(
                    "Invalid sales return status transition: " +
                    current + " -> " + newStatus
                );
            }

            dao.updateStatus(id, newStatus);
            return dao.findById(id);
        });
    }

    private int positiveInt(Object value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }

        try {
            int parsed;
            if (value instanceof Number number) {
                parsed = new BigDecimal(number.toString()).intValueExact();
            } else {
                parsed = Integer.parseInt(value.toString());
            }

            if (parsed <= 0) {
                throw new IllegalArgumentException(field + " must be greater than zero");
            }

            return parsed;
        } catch (NumberFormatException | ArithmeticException e) {
            throw new IllegalArgumentException(field + " must be a positive integer");
        }
    }

    private String optionalText(Object value) {
        if (value == null) return null;

        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }
}
