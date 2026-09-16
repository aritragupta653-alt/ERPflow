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

        // SAVE MAIN PURCHASE ORDER

        purchaseOrderDAO.save(
                purchaseOrder
        );


        // SAVE ALL PURCHASE ORDER ITEMS

        for (
                PurchaseOrderItem item
                : purchaseOrderItems
        ) {

            // CONNECT ITEM TO PURCHASE ORDER

            item.setPurchaseOrder(
                    purchaseOrder
            );


            purchaseOrderItemDAO.save(
                    item
            );
        }
    }


    // GET ALL PURCHASE ORDERS

    public List<PurchaseOrder>
    getAllPurchaseOrders() {

        return purchaseOrderDAO.findAll();
    }


    // GET PURCHASE ORDER BY ID

    public PurchaseOrder getPurchaseOrderById(
            int id
    ) {

        return purchaseOrderDAO.findById(id);
    }


    // GET ITEMS OF PURCHASE ORDER

    public List<PurchaseOrderItem>
    getPurchaseOrderItems(
            PurchaseOrder purchaseOrder
    ) {

        return purchaseOrderItemDAO
                .findByPurchaseOrder(
                        purchaseOrder
                );
    }

    public void receivePurchaseOrder(
        int purchaseOrderId
) {

    // GET PURCHASE ORDER

    PurchaseOrder purchaseOrder =
            purchaseOrderDAO.findById(
                    purchaseOrderId
            );


    if (purchaseOrder == null) {

        throw new RuntimeException(
                "Purchase Order not found"
        );
    }


    // PREVENT RECEIVING TWICE

    if ("RECEIVED".equals(
            purchaseOrder.getStatus()
    )) {

        throw new RuntimeException(
                "Purchase Order already received"
        );
    }


    // GET ALL ITEMS IN THIS PURCHASE ORDER

    List<PurchaseOrderItem> purchaseOrderItems =
            purchaseOrderItemDAO
                    .findByPurchaseOrder(
                            purchaseOrder
                    );


    // PROCESS EVERY ITEM

    for (
            PurchaseOrderItem orderItem
            : purchaseOrderItems
    ) {

        // 1. STOCK IN

        inventoryService.stockIn(
                orderItem.getItem(),
                orderItem.getQuantity()
        );


       


    }


    // 3. UPDATE PURCHASE ORDER STATUS

    purchaseOrder.setStatus(
            "RECEIVED"
    );


    purchaseOrderDAO.update(
            purchaseOrder
    );
}

}