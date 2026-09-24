package com.erpflow.service;

import com.erpflow.dao.PurchaseOrderDAO;
import com.erpflow.dao.PurchaseOrderItemDAO;
import com.erpflow.model.Item;
import com.erpflow.model.PurchaseOrder;
import com.erpflow.model.PurchaseOrderItem;
import com.erpflow.model.enums.PurchaseOrderStatus;

import java.util.List;

public class PurchaseOrderService {

    private final PurchaseOrderDAO purchaseOrderDAO = new PurchaseOrderDAO();
    private final PurchaseOrderItemDAO purchaseOrderItemDAO = new PurchaseOrderItemDAO();
    private final InventoryService inventoryService = new InventoryService();

    public void createPurchaseOrder(
            PurchaseOrder purchaseOrder,
            List<PurchaseOrderItem> items) {

        if (purchaseOrder == null || purchaseOrder.getSupplier() == null) {
            throw new RuntimeException("Purchase order and supplier are required");
        }

        if (items == null || items.isEmpty()) {
            throw new RuntimeException("At least one item is required");
        }

        for (PurchaseOrderItem orderItem : items) {
            validateItem(orderItem);

            // Service/non-stock lines are represented with quantity 1
            // for schemas where the quantity column is NOT NULL.
            if (!orderItem.getItem().isTrackInventory()) {
                orderItem.setQuantity(1);
            }

            orderItem.setReceivedQuantity(0);
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.CREATED);
        purchaseOrderDAO.save(purchaseOrder);

        for (PurchaseOrderItem orderItem : items) {
            orderItem.setPurchaseOrder(purchaseOrder);
            purchaseOrderItemDAO.save(orderItem);
        }
    }

    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderDAO.findAll();
    }

    public PurchaseOrder getPurchaseOrderById(int id) {
        return purchaseOrderDAO.findById(id);
    }

    public List<PurchaseOrderItem> getPurchaseOrderItems(PurchaseOrder purchaseOrder) {
        return purchaseOrderItemDAO.findByPurchaseOrder(purchaseOrder);
    }

    public void editPurchaseOrder(
            int purchaseOrderId,
            PurchaseOrder updatedOrder,
            List<PurchaseOrderItem> updatedItems) {

        PurchaseOrder existing = purchaseOrderDAO.findById(purchaseOrderId);

        if (existing == null) {
            throw new RuntimeException("Purchase order not found");
        }

        if (existing.getStatus() != PurchaseOrderStatus.CREATED) {
            throw new RuntimeException(
                    "Only purchase orders that have not been received can be edited"
            );
        }

        if (updatedOrder == null || updatedOrder.getSupplier() == null) {
            throw new RuntimeException("Supplier is required");
        }

        if (updatedItems == null || updatedItems.isEmpty()) {
            throw new RuntimeException("At least one item is required");
        }

        for (PurchaseOrderItem item : updatedItems) {
            validateItem(item);

            if (!item.getItem().isTrackInventory()) {
                item.setQuantity(1);
            }

            item.setReceivedQuantity(0);
            item.setPurchaseOrder(existing);
        }

        existing.setSupplier(updatedOrder.getSupplier());
        purchaseOrderDAO.update(existing);

        // No receiving has occurred for CREATED orders, so replacing their
        // lines does not erase receiving history.
        purchaseOrderItemDAO.deleteByPurchaseOrder(purchaseOrderId);

        for (PurchaseOrderItem item : updatedItems) {
            item.setPurchaseOrder(existing);
            purchaseOrderItemDAO.save(item);
        }
    }

    public void receivePurchaseOrder(int purchaseOrderId) {
        PurchaseOrder order = purchaseOrderDAO.findById(purchaseOrderId);

        if (order == null) {
            throw new RuntimeException("Purchase order not found");
        }

        List<PurchaseOrderItem> lines = purchaseOrderItemDAO.findByPurchaseOrder(order);

        boolean hasReceivableGoods = false;

        for (PurchaseOrderItem line : lines) {
            Item item = line.getItem();

            if (item != null && item.isTrackInventory()
                    && line.getRemainingQuantity() > 0) {
                hasReceivableGoods = true;
                break;
            }
        }

        if (!hasReceivableGoods) {
            throw new RuntimeException(
                    "No outstanding inventory quantities remain to receive"
            );
        }

        // Compatibility method: receive all remaining inventory-tracked goods.
        for (PurchaseOrderItem line : lines) {
            Item item = line.getItem();

            if (item == null || !item.isTrackInventory()) {
                continue;
            }

            int remaining = line.getRemainingQuantity();

            if (remaining > 0) {
                inventoryService.stockIn(item, remaining);

                purchaseOrderItemDAO.updateReceivedQuantity(
                        line.getId(),
                        line.getRecievedQuantity() + remaining
                );
            }
        }

        refreshStatus(order, lines);
    }

    public void receivePurchaseOrder(
            int purchaseOrderId,
            List<ReceiveLine> receivedLines) {

        PurchaseOrder order = purchaseOrderDAO.findById(purchaseOrderId);

        if (order == null) {
            throw new RuntimeException("Purchase order not found");
        }

        if (receivedLines == null || receivedLines.isEmpty()) {
            throw new RuntimeException("Enter at least one quantity to receive");
        }

        List<PurchaseOrderItem> orderLines =
                purchaseOrderItemDAO.findByPurchaseOrder(order);

        // Validate all requested lines before changing inventory.
        for (ReceiveLine receiveLine : receivedLines) {
            PurchaseOrderItem matched = null;

            for (PurchaseOrderItem line : orderLines) {
                if (line.getId() == receiveLine.purchaseOrderItemId) {
                    matched = line;
                    break;
                }
            }

            if (matched == null) {
                throw new RuntimeException(
                        "Purchase order item not found: "
                                + receiveLine.purchaseOrderItemId
                );
            }

            Item item = matched.getItem();

            if (item == null || !item.isTrackInventory()) {
                throw new RuntimeException(
                        "Only inventory-tracked goods can be received"
                );
            }

            if (receiveLine.quantity <= 0) {
                throw new RuntimeException(
                        "Receive quantity must be greater than zero"
                );
            }

            if (receiveLine.quantity > matched.getRemainingQuantity()) {
                throw new RuntimeException(
                        "Receive quantity exceeds outstanding quantity for "
                                + item.getName()
                );
            }
        }

        // Validation is completed before inventory changes begin.
        for (ReceiveLine receiveLine : receivedLines) {
            for (PurchaseOrderItem line : orderLines) {
                if (line.getId() == receiveLine.purchaseOrderItemId) {
                    inventoryService.stockIn(
                            line.getItem(),
                            receiveLine.quantity
                    );

                    int updatedReceivedQuantity =
                            line.getRecievedQuantity() + receiveLine.quantity;

                    purchaseOrderItemDAO.updateReceivedQuantity(
                            line.getId(),
                            updatedReceivedQuantity
                    );

                    line.setReceivedQuantity(updatedReceivedQuantity);
                    break;
                }
            }
        }

        refreshStatus(order, orderLines);
    }

    private void refreshStatus(
            PurchaseOrder order,
            List<PurchaseOrderItem> lines) {

        boolean anyReceived = false;
        boolean allReceived = true;

        for (PurchaseOrderItem line : lines) {
            if (line.getItem() == null || !line.getItem().isTrackInventory()) {
                continue;
            }

            if (line.getRecievedQuantity() > 0) {
                anyReceived = true;
            }

            if (line.getRemainingQuantity() > 0) {
                allReceived = false;
            }
        }

        if (allReceived) {
            order.setStatus(PurchaseOrderStatus.RECEIVED);
        } else if (anyReceived) {
            order.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        } else {
            order.setStatus(PurchaseOrderStatus.CREATED);
        }

        purchaseOrderDAO.update(order);
    }

    private void validateItem(PurchaseOrderItem item) {
        if (item == null || item.getItem() == null) {
            throw new RuntimeException("Item is required");
        }

        if (item.getItem().isTrackInventory() && item.getQuantity() <= 0) {
            throw new RuntimeException(
                    "Quantity must be greater than zero for inventory-tracked goods"
            );
        }

        if (item.getPurchasePrice() == null
                || item.getPurchasePrice().signum() < 0) {
            throw new RuntimeException(
                    "Purchase price must be zero or greater"
            );
        }
    }

    public static class ReceiveLine {
        public int purchaseOrderItemId;
        public int quantity;

        public ReceiveLine() {
        }

        public ReceiveLine(int purchaseOrderItemId, int quantity) {
            this.purchaseOrderItemId = purchaseOrderItemId;
            this.quantity = quantity;
        }
    }
}