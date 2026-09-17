package com.erpflow.service;

import com.erpflow.dao.PurchaseOrderDAO;
import com.erpflow.dao.PurchaseOrderItemDAO;
import com.erpflow.model.PurchaseOrder;
import com.erpflow.model.PurchaseOrderItem;

import java.util.List;

public class PurchaseOrderService {

    private final PurchaseOrderDAO purchaseOrderDAO =
            new PurchaseOrderDAO();

    private final PurchaseOrderItemDAO purchaseOrderItemDAO =
            new PurchaseOrderItemDAO();

    private final InventoryService inventoryService =
            new InventoryService();


    // CREATE PURCHASE ORDER

    public void createPurchaseOrder(
            PurchaseOrder purchaseOrder,
            List<PurchaseOrderItem> purchaseOrderItems
    ) {

        if (purchaseOrder == null) {
            throw new RuntimeException(
                    "Purchase Order is required"
            );
        }

        if (purchaseOrder.getSupplier() == null) {
            throw new RuntimeException(
                    "Supplier is required"
            );
        }

        if (purchaseOrderItems == null ||
                purchaseOrderItems.isEmpty()) {

            throw new RuntimeException(
                    "At least one item is required"
            );
        }


        purchaseOrderDAO.save(
                purchaseOrder
        );


        for (
                PurchaseOrderItem item
                : purchaseOrderItems
        ) {

            item.setPurchaseOrder(
                    purchaseOrder
            );

            purchaseOrderItemDAO.save(
                    item
            );
        }
    }


    // GET ALL PURCHASE ORDERS

    public List<PurchaseOrder> getAllPurchaseOrders() {

        return purchaseOrderDAO.findAll();
    }


    // GET PURCHASE ORDER BY ID

    public PurchaseOrder getPurchaseOrderById(
            int id
    ) {

        return purchaseOrderDAO.findById(id);
    }


    // GET PURCHASE ORDER ITEMS

    public List<PurchaseOrderItem>
    getPurchaseOrderItems(
            PurchaseOrder purchaseOrder
    ) {

        return purchaseOrderItemDAO
                .findByPurchaseOrder(
                        purchaseOrder
                );
    }


    // RECEIVE PURCHASE ORDER

    public void receivePurchaseOrder(
            int purchaseOrderId
    ) {

        PurchaseOrder purchaseOrder =
                purchaseOrderDAO.findById(
                        purchaseOrderId
                );


        if (purchaseOrder == null) {

            throw new RuntimeException(
                    "Purchase Order not found"
            );
        }


        if ("RECEIVED".equals(
                purchaseOrder.getStatus()
        )) {

            throw new RuntimeException(
                    "Purchase Order already received"
            );
        }


        List<PurchaseOrderItem>
                purchaseOrderItems =
                purchaseOrderItemDAO
                        .findByPurchaseOrder(
                                purchaseOrder
                        );


        for (
                PurchaseOrderItem orderItem
                : purchaseOrderItems
        ) {

            inventoryService.stockIn(
                    orderItem.getItem(),
                    orderItem.getQuantity()
            );
        }


        purchaseOrder.setStatus(
                "RECEIVED"
        );


        purchaseOrderDAO.update(
                purchaseOrder
        );
    }
}