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

    public void createSalesOrder(
            SalesOrder salesOrder,
            List<SalesOrderItem> salesOrderItems) {

        if (salesOrderItems == null ||
                salesOrderItems.isEmpty()) {

            throw new RuntimeException(
                    "Sales Order must contain at least one item"
            );
        }

        // Reserve stock first
        for (SalesOrderItem item : salesOrderItems) {

            inventoryService.reserveStock(
                    item.getItem(),
                    item.getQuantity()
            );
        }

        // Save sales order
        salesOrderDAO.save(salesOrder);

        // Save order items
        for (SalesOrderItem item : salesOrderItems) {

            item.setSalesOrder(salesOrder);

            salesOrderItemDAO.save(item);
        }
    }

    public List<SalesOrder> getAllSalesOrders() {

        return salesOrderDAO.findAll();
    }

    public SalesOrder getSalesOrderById(int id) {

        return salesOrderDAO.findById(id);
    }

    public List<SalesOrderItem> getSalesOrderItems(
            SalesOrder salesOrder) {

        return salesOrderItemDAO.findBySalesOrder(
                salesOrder
        );
    }
}