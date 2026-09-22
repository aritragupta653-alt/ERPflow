
package com.erpflow.service;

import com.erpflow.dao.SalesOrderDAO;
import com.erpflow.dao.SalesOrderItemDAO;
import com.erpflow.model.Item;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;

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

        validateAndCalculate(
                salesOrder,
                salesOrderItems
        );

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
    //
    // Existing matching item lines are updated in place.
    // New lines are inserted.
    // Removed lines are deleted individually, but only if they
    // are not referenced by a package.
    // =========================================================

    public void updateSalesOrder(
            int salesOrderId,
            SalesOrder updatedOrder,
            List<SalesOrderItem> updatedItems
    ) {

        SalesOrder existingOrder =
                salesOrderDAO.findById(salesOrderId);

        if (existingOrder == null) {
            throw new RuntimeException(
                    "Sales order not found"
            );
        }

        if (!"CREATED".equalsIgnoreCase(existingOrder.getStatus())) {
            throw new RuntimeException(
                    "Only CREATED sales orders can be edited"
            );
        }

        validateAndCalculate(
                updatedOrder,
                updatedItems
        );

        // Load the existing persisted order lines.
        List<SalesOrderItem> oldItems =
                salesOrderItemDAO.findBySalesOrder(existingOrder);

        /*
         * Group existing lines by item ID.
         *
         * A list is used for each item ID so that this logic
         * remains predictable even if an order contains the same
         * item more than once.
         */
        Map<Integer, List<SalesOrderItem>> oldItemsByItemId =
                new LinkedHashMap<>();

        for (SalesOrderItem oldItem : oldItems) {

            int itemId = oldItem.getItem().getId();

            oldItemsByItemId
                    .computeIfAbsent(
                            itemId,
                            key -> new ArrayList<>()
                    )
                    .add(oldItem);
        }

        /*
         * Match incoming lines to existing lines by item ID.
         *
         * Matched lines retain their database IDs.
         * Unmatched incoming lines are new lines.
         * Existing lines left unmatched have been removed.
         */
        List<SalesOrderItem> matchedOldItems =
                new ArrayList<>();

        List<SalesOrderItem> newItems =
                new ArrayList<>();

        Set<Integer> retainedOldItemIds =
                new HashSet<>();

        for (SalesOrderItem incomingItem : updatedItems) {

            int incomingItemId =
                    incomingItem.getItem().getId();

            List<SalesOrderItem> candidates =
                    oldItemsByItemId.get(incomingItemId);

            if (candidates != null && !candidates.isEmpty()) {

                SalesOrderItem existingLine =
                        candidates.remove(0);

                // Preserve the existing sales_order_items.id.
                incomingItem.setId(existingLine.getId());

                incomingItem.setSalesOrder(existingOrder);

                matchedOldItems.add(existingLine);

                retainedOldItemIds.add(existingLine.getId());

            } else {

                // This item is new to the order.
                incomingItem.setId(0);

                incomingItem.setSalesOrder(updatedOrder);

                newItems.add(incomingItem);
            }
        }

        // Identify old lines that were not retained.
        List<SalesOrderItem> removedItems =
                new ArrayList<>();

        for (SalesOrderItem oldItem : oldItems) {

            if (!retainedOldItemIds.contains(oldItem.getId())) {
                removedItems.add(oldItem);
            }
        }

        /*
         * Before changing stock or the order header, check whether
         * any removed line is referenced by package_items.
         *
         * Such lines cannot safely be deleted by this edit flow.
         */
        for (SalesOrderItem removedItem : removedItems) {

            if (salesOrderItemDAO.isReferencedByPackage(
                    removedItem.getId()
            )) {

                throw new RuntimeException(
                        "Cannot remove item "
                                + removedItem.getItem().getName()
                                + " because it is already referenced by a package"
                );
            }
        }

        /*
         * Release stock reserved by the previous order.
         * The existing inventory service handles inventory-tracked
         * items only.
         */
        for (SalesOrderItem oldItem : oldItems) {

            Item item = oldItem.getItem();

            if (item != null && item.isTrackInventory()) {

                inventoryService.releaseStock(
                        item,
                        oldItem.getQuantity()
                );
            }
        }

        /*
         * Reserve stock for the edited order lines.
         * This includes both matched lines and newly added lines.
         */
        for (SalesOrderItem incomingItem : updatedItems) {

            Item item = incomingItem.getItem();

            if (item.isTrackInventory()) {

                inventoryService.reserveStock(
                        item,
                        incomingItem.getQuantity()
                );
            }
        }

        // Update the existing order header.
        updatedOrder.setId(salesOrderId);

        updatedOrder.setOrderDate(
                existingOrder.getOrderDate()
        );

        updatedOrder.setStatus(
                existingOrder.getStatus()
        );

        salesOrderDAO.update(updatedOrder);

        /*
         * Update matched lines in place.
         *
         * The existing row IDs are retained, so package references
         * to those lines are not broken.
         */
        for (SalesOrderItem incomingItem : updatedItems) {

            incomingItem.setSalesOrder(updatedOrder);

            if (incomingItem.getId() > 0) {

                salesOrderItemDAO.update(incomingItem);

            } else {

                salesOrderItemDAO.save(incomingItem);
            }
        }

        /*
         * Delete only lines that were removed from the edited order.
         * Each deletion is guarded against package references.
         */
        for (SalesOrderItem removedItem : removedItems) {

            salesOrderItemDAO.deleteByIdAndSalesOrderId(
                    removedItem.getId(),
                    salesOrderId
            );
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
            throw new RuntimeException(
                    "Sales order cannot be null"
            );
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

        taxRate = taxRate.setScale(
                2,
                RoundingMode.HALF_UP
        );

        BigDecimal subtotal = BigDecimal.ZERO;

        for (SalesOrderItem orderItem : salesOrderItems) {

            if (orderItem == null || orderItem.getItem() == null) {
                throw new RuntimeException(
                        "Item cannot be null"
                );
            }

            if (orderItem.getQuantity() <= 0) {
                throw new RuntimeException(
                        "Quantity must be greater than 0"
                );
            }

            if (orderItem.getSellingPrice() == null) {
                throw new RuntimeException(
                        "Selling price is required"
                );
            }

            if (orderItem.getSellingPrice()
                    .compareTo(BigDecimal.ZERO) < 0) {

                throw new RuntimeException(
                        "Selling price cannot be negative"
                );
            }

            BigDecimal lineTotal =
                    orderItem.getSellingPrice().multiply(
                            BigDecimal.valueOf(
                                    orderItem.getQuantity()
                            )
                    );

            subtotal = subtotal.add(lineTotal);
        }

        subtotal = subtotal.setScale(
                2,
                RoundingMode.HALF_UP
        );

        BigDecimal taxAmount = subtotal
                .multiply(taxRate)
                .divide(
                        new BigDecimal("100"),
                        2,
                        RoundingMode.HALF_UP
                );

        BigDecimal totalAmount = subtotal
                .add(taxAmount)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );

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