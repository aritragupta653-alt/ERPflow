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


    // Create Sales Order

    public void createSalesOrder(
            SalesOrder salesOrder,
            List<SalesOrderItem> salesOrderItems) {

        salesOrderDAO.save(salesOrder);

        for (SalesOrderItem item : salesOrderItems) {

            item.setSalesOrder(salesOrder);

            salesOrderItemDAO.save(item);
        }
    }


    // Get all Sales Orders

    public List<SalesOrder> getAllSalesOrders() {

        return salesOrderDAO.findAll();
    }


    // Get Sales Order by ID

    public SalesOrder getSalesOrderById(int id) {

        return salesOrderDAO.findById(id);
    }


    // Get items belonging to a Sales Order

    public List<SalesOrderItem> getSalesOrderItems(
            SalesOrder salesOrder) {

        return salesOrderItemDAO.findBySalesOrder(salesOrder);
    }


    // Complete Sales Order

    public void completeSalesOrder(int salesOrderId) {

        SalesOrder salesOrder =
                salesOrderDAO.findById(salesOrderId);

        if (salesOrder == null) {
            throw new RuntimeException(
                    "Sales Order not found"
            );
        }

        if ("COMPLETED".equals(salesOrder.getStatus())) {
            throw new RuntimeException(
                    "Sales Order already completed"
            );
        }

        List<SalesOrderItem> salesOrderItems =
                salesOrderItemDAO.findBySalesOrder(salesOrder);


        // Remove items from inventory

        for (SalesOrderItem orderItem : salesOrderItems) {

            inventoryService.stockOut(
                    orderItem.getItem(),
                    orderItem.getQuantity()
            );
        }


        // Update order status

        salesOrder.setStatus("COMPLETED");

        salesOrderDAO.update(salesOrder);
    }
}