
package com.erpflow.service;

import com.erpflow.dao.SalesOrderDAO;
import com.erpflow.dao.SalesOrderItemDAO;
import com.erpflow.model.Item;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SalesOrderService {

    private final SalesOrderDAO salesOrderDAO =
            new SalesOrderDAO();

    private final SalesOrderItemDAO salesOrderItemDAO =
            new SalesOrderItemDAO();

    private final InventoryService inventoryService =
            new InventoryService();

    // =========================================================
    // CREATE SALES ORDER
    // =========================================================

    public void createSalesOrder(
            SalesOrder salesOrder,
            List<SalesOrderItem> salesOrderItems
    ) {
        validateAndCalculate(salesOrder, salesOrderItems);

        // Reserve stock only for inventory-tracked items.
        for (SalesOrderItem orderItem : salesOrderItems) {
            Item item = orderItem.getItem();

            if (item.isTrackInventory()) {
                inventoryService.reserveStock(
                        item,
                        orderItem.getQuantity()
                );
            }
        }

        salesOrderDAO.save(salesOrder);

        for (SalesOrderItem orderItem : salesOrderItems) {
            orderItem.setSalesOrder(salesOrder);
            salesOrderItemDAO.save(orderItem);
        }
    }

    // =========================================================
    // UPDATE SALES ORDER
    // =========================================================

    public void updateSalesOrder(
            int salesOrderId,
            SalesOrder updatedOrder,
            List<SalesOrderItem> updatedItems
    ) {
        SalesOrder existingOrder =
                salesOrderDAO.findById(salesOrderId);

        if (existingOrder == null) {
            throw new RuntimeException("Sales order not found");
        }

        if (!"CREATED".equalsIgnoreCase(existingOrder.getStatus())) {
            throw new RuntimeException(
                    "Only CREATED sales orders can be edited"
            );
        }

        validateAndCalculate(updatedOrder, updatedItems);

        // Release the stock reserved by the existing order.
        List<SalesOrderItem> oldItems =
                salesOrderItemDAO.findBySalesOrder(existingOrder);

        for (SalesOrderItem oldItem : oldItems) {
            Item item = oldItem.getItem();

            if (item != null && item.isTrackInventory()) {
                inventoryService.releaseStock(
                        item,
                        oldItem.getQuantity()
                );
            }
        }

        // Update the existing order's header.
        updatedOrder.setId(salesOrderId);
        salesOrderDAO.update(updatedOrder);

        // Remove old lines before saving the replacement lines.
        salesOrderItemDAO.deleteBySalesOrderId(salesOrderId);

        // Reserve stock for the replacement lines.
        for (SalesOrderItem orderItem : updatedItems) {
            Item item = orderItem.getItem();

            if (item.isTrackInventory()) {
                inventoryService.reserveStock(
                        item,
                        orderItem.getQuantity()
                );
            }

            orderItem.setSalesOrder(updatedOrder);
            salesOrderItemDAO.save(orderItem);
        }
    }

    // =========================================================
    // VALIDATE ITEMS AND CALCULATE TOTALS
    // =========================================================

    private void validateAndCalculate(
            SalesOrder salesOrder,
            List<SalesOrderItem> salesOrderItems
    ) {
        if (salesOrder == null) {
            throw new RuntimeException("Sales order cannot be null");
        }

        if (salesOrderItems == null || salesOrderItems.isEmpty()) {
            throw new RuntimeException(
                    "Sales Order must contain at least one item"
            );
        }

        BigDecimal taxRate = salesOrder.getTaxRate();

        if (taxRate == null) {
            taxRate = BigDecimal.ZERO;
        }

        if (taxRate.compareTo(BigDecimal.ZERO) < 0
                || taxRate.compareTo(new BigDecimal("100")) > 0) {
            throw new RuntimeException(
                    "Tax rate must be between 0 and 100"
            );
        }

        taxRate = taxRate.setScale(2, RoundingMode.HALF_UP);

        BigDecimal subtotal = BigDecimal.ZERO;
        Set<Integer> itemIds = new HashSet<>();

        for (SalesOrderItem orderItem : salesOrderItems) {
            if (orderItem == null || orderItem.getItem() == null) {
                throw new RuntimeException("Item cannot be null");
            }

            if (orderItem.getQuantity() <= 0) {
                throw new RuntimeException(
                        "Quantity must be greater than 0"
                );
            }

            if (orderItem.getSellingPrice() == null) {
                throw new RuntimeException("Selling price is required");
            }

            if (orderItem.getSellingPrice()
                    .compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException(
                        "Selling price cannot be negative"
                );
            }

            BigDecimal lineTotal =
                    orderItem.getSellingPrice().multiply(
                            BigDecimal.valueOf(orderItem.getQuantity())
                    );

            subtotal = subtotal.add(lineTotal);
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxAmount = subtotal
                .multiply(taxRate)
                .divide(
                        new BigDecimal("100"),
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal totalAmount = subtotal
                .add(taxAmount)
                .setScale(2, RoundingMode.HALF_UP);

        salesOrder.setTaxRate(taxRate);
        salesOrder.setSubtotal(subtotal);
        salesOrder.setTaxAmount(taxAmount);
        salesOrder.setTotalAmount(totalAmount);
    }

    // =========================================================
    // GET ALL SALES ORDERS
    // =========================================================

    public List<SalesOrder> getAllSalesOrders() {
        return salesOrderDAO.findAll();
    }

    // =========================================================
    // GET SALES ORDER BY ID
    // =========================================================

    public SalesOrder getSalesOrderById(int id) {
        return salesOrderDAO.findById(id);
    }

    // =========================================================
    // GET SALES ORDER ITEMS
    // =========================================================

    public List<SalesOrderItem> getSalesOrderItems(
            SalesOrder salesOrder
    ) {
        return salesOrderItemDAO.findBySalesOrder(salesOrder);
    }
}