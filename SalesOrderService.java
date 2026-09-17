package com.erpflow.service;

import com.erpflow.dao.SalesOrderDAO;
import com.erpflow.dao.SalesOrderItemDAO;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;

import java.util.List;

public class SalesOrderService {

    private final SalesOrderDAO salesOrderDAO =
            new SalesOrderDAO();

    private final SalesOrderItemDAO salesOrderItemDAO =
            new SalesOrderItemDAO();

    private final InventoryService inventoryService =
            new InventoryService();


    // CREATE SALES ORDER

    public void createSalesOrder(
            SalesOrder salesOrder,
            List<SalesOrderItem> salesOrderItems
    ) {

        if (salesOrderItems == null ||
                salesOrderItems.isEmpty()) {

            throw new RuntimeException(
                    "Sales Order must contain at least one item"
            );
        }


        // RESERVE STOCK

        for (SalesOrderItem item : salesOrderItems) {

            inventoryService.reserveStock(
                    item.getItem(),
                    item.getQuantity()
            );
        }


        // SAVE SALES ORDER

        salesOrderDAO.save(
                salesOrder
        );


        // SAVE SALES ORDER ITEMS

        for (SalesOrderItem item : salesOrderItems) {

            item.setSalesOrder(
                    salesOrder
            );

            salesOrderItemDAO.save(
                    item
            );
        }
    }


    // GET ALL SALES ORDERS

    public List<SalesOrder> getAllSalesOrders() {

        return salesOrderDAO.findAll();
    }


    // GET SALES ORDER BY ID

    public SalesOrder getSalesOrderById(
            int id
    ) {

        return salesOrderDAO.findById(id);
    }


    // GET ITEMS OF SALES ORDER

    public List<SalesOrderItem> getSalesOrderItems(
            SalesOrder salesOrder
    ) {

        return salesOrderItemDAO.findBySalesOrder(
                salesOrder
        );
    }
}
