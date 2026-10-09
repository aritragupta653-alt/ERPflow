package com.erpflow.service;

import com.erpflow.dao.SalesOrderDAO;
import com.erpflow.dao.SalesOrderItemDAO;
import com.erpflow.model.Item;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;
import com.erpflow.model.enums.SalesOrderStatus;
import com.erpflow.util.TransactionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SalesOrderService {

    private final SalesOrderDAO salesOrderDAO = new SalesOrderDAO();
    private final SalesOrderItemDAO salesOrderItemDAO = new SalesOrderItemDAO();
    private final InventoryService inventoryService = new InventoryService();

    public void createSalesOrder(
            SalesOrder salesOrder,
            List<SalesOrderItem> salesOrderItems) {

        validateAndCalculate(salesOrder, salesOrderItems);

        TransactionManager.execute(() -> {
            for (SalesOrderItem orderItem : salesOrderItems) {
                Item item = orderItem.getItem();
                if (item != null && item.isTrackInventory()) {
                    inventoryService.reserveStock(item, orderItem.getQuantity());
                }
            }

            // A newly created order always starts at zero fulfillment counters.
            for (SalesOrderItem orderItem : salesOrderItems) {
                orderItem.setPackedQuantity(0);
                orderItem.setShippedQuantity(0);
            }

            salesOrderDAO.save(salesOrder);

            for (SalesOrderItem orderItem : salesOrderItems) {
                orderItem.setSalesOrder(salesOrder);
                salesOrderItemDAO.save(orderItem);
            }

            return null;
        });
    }

    public void updateSalesOrder(
            int salesOrderId,
            SalesOrder updatedOrder,
            List<SalesOrderItem> updatedItems) {

        validateAndCalculate(updatedOrder, updatedItems);

        TransactionManager.execute(() -> {
            SalesOrder existingOrder = salesOrderDAO.findById(salesOrderId);

            if (existingOrder == null) {
                throw new IllegalArgumentException("Sales order not found");
            }

            if (existingOrder.getStatus() != SalesOrderStatus.CREATED) {
                throw new IllegalStateException(
                        "Only CREATED sales orders can be edited");
            }

            List<SalesOrderItem> oldItems =
                    salesOrderItemDAO.findForUpdateBySalesOrder(salesOrderId);

            // Once fulfillment has started, changing quantities/items would
            // make the fulfillment counters and package history inconsistent.
            for (SalesOrderItem oldItem : oldItems) {
                if (oldItem.getPackedQuantity() > 0
                        || oldItem.getShippedQuantity() > 0) {
                    throw new IllegalStateException(
                            "Sales Order cannot be edited after packing or shipping has started");
                }
            }

            Map<Integer, List<SalesOrderItem>> oldItemsByItemId = new LinkedHashMap<>();
            for (SalesOrderItem oldItem : oldItems) {
                oldItemsByItemId
                        .computeIfAbsent(oldItem.getItem().getId(), key -> new ArrayList<>())
                        .add(oldItem);
            }

            Set<Integer> retainedIds = new HashSet<>();

            for (SalesOrderItem incoming : updatedItems) {
                List<SalesOrderItem> candidates = oldItemsByItemId.get(
                        incoming.getItem().getId());

                if (candidates != null && !candidates.isEmpty()) {
                    SalesOrderItem existingLine = candidates.remove(0);
                    incoming.setId(existingLine.getId());
                    incoming.setSalesOrder(existingOrder);
                    retainedIds.add(existingLine.getId());
                } else {
                    incoming.setId(0);
                    incoming.setSalesOrder(existingOrder);
                    incoming.setPackedQuantity(0);
                    incoming.setShippedQuantity(0);
                }
            }

            List<SalesOrderItem> removedItems = new ArrayList<>();
            for (SalesOrderItem oldItem : oldItems) {
                if (!retainedIds.contains(oldItem.getId())) {
                    removedItems.add(oldItem);
                }
            }

            for (SalesOrderItem removed : removedItems) {
                if (salesOrderItemDAO.isReferencedByPackage(removed.getId())) {
                    throw new IllegalStateException(
                            "Cannot remove item " + removed.getItem().getName()
                                    + " because it is referenced by a package");
                }
            }

            // Preserve the old stock behavior, but execute it inside the same
            // JDBC transaction so an order edit cannot partially commit.
            for (SalesOrderItem oldItem : oldItems) {
                Item item = oldItem.getItem();
                if (item != null && item.isTrackInventory()) {
                    inventoryService.releaseStock(item, oldItem.getQuantity());
                }
            }

            for (SalesOrderItem incoming : updatedItems) {
                Item item = incoming.getItem();
                if (item != null && item.isTrackInventory()) {
                    inventoryService.reserveStock(item, incoming.getQuantity());
                }
            }

            updatedOrder.setId(salesOrderId);
            updatedOrder.setStatus(existingOrder.getStatus());
            salesOrderDAO.update(updatedOrder);

            for (SalesOrderItem incoming : updatedItems) {
                incoming.setSalesOrder(existingOrder);
                if (incoming.getId() > 0) {
                    salesOrderItemDAO.update(incoming);
                } else {
                    salesOrderItemDAO.save(incoming);
                }
            }

            for (SalesOrderItem removed : removedItems) {
                salesOrderItemDAO.deleteByIdAndSalesOrderId(
                        removed.getId(), salesOrderId);
            }

            return null;
        });
    }

    private void validateAndCalculate(
            SalesOrder salesOrder,
            List<SalesOrderItem> salesOrderItems) {

        if (salesOrder == null) {
            throw new IllegalArgumentException("Sales order cannot be null");
        }
        if (salesOrderItems == null || salesOrderItems.isEmpty()) {
            throw new IllegalArgumentException(
                    "Sales Order must contain at least one item");
        }
        if (salesOrder.getOrderDate() == null) {
            throw new IllegalArgumentException("Order date is required");
        }
        if (salesOrder.getExpectedDeliveryDate() == null) {
            throw new IllegalArgumentException("Expected delivery date is required");
        }
        if (salesOrder.getExpectedDeliveryDate()
                .isBefore(salesOrder.getOrderDate().toLocalDate())) {
            throw new IllegalArgumentException(
                    "Expected delivery date cannot be before order date");
        }

        BigDecimal taxRate = salesOrder.getTaxRate();
        if (taxRate == null) {
            taxRate = BigDecimal.ZERO;
        }
        if (taxRate.compareTo(BigDecimal.ZERO) < 0
                || taxRate.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Tax rate must be between 0 and 100");
        }
        taxRate = taxRate.setScale(2, RoundingMode.HALF_UP);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (SalesOrderItem orderItem : salesOrderItems) {
            if (orderItem == null || orderItem.getItem() == null) {
                throw new IllegalArgumentException("Item cannot be null");
            }
            if (orderItem.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }
            if (orderItem.getSellingPrice() == null) {
                throw new IllegalArgumentException("Selling price is required");
            }
            if (orderItem.getSellingPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Selling price cannot be negative");
            }

            subtotal = subtotal.add(
                    orderItem.getSellingPrice()
                            .multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = subtotal
                .multiply(taxRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal
                .add(taxAmount)
                .setScale(2, RoundingMode.HALF_UP);

        salesOrder.setTaxRate(taxRate);
        salesOrder.setSubtotal(subtotal);
        salesOrder.setTaxAmount(taxAmount);
        salesOrder.setTotalAmount(totalAmount);
    }

    public List<SalesOrder> getAllSalesOrders() {
        return salesOrderDAO.findAll();
    }

    public List<SalesOrder> getSalesOrdersByStatus(String status) {
        return salesOrderDAO.findAll(status);
    }

    public SalesOrder getSalesOrderById(int id) {
        return salesOrderDAO.findById(id);
    }

    public List<SalesOrderItem> getSalesOrderItems(SalesOrder salesOrder) {
        if (salesOrder == null || salesOrder.getId() <= 0) {
            throw new IllegalArgumentException("Valid Sales Order is required");
        }
        List<SalesOrderItem> items = salesOrderItemDAO.findBySalesOrder(salesOrder.getId());
        for (SalesOrderItem item : items) {
            item.setSalesOrder(salesOrder);
        }
        return items;
    }
}
