package com.erpflow.service;

import com.erpflow.dao.SalesOrderDAO;
import com.erpflow.dao.SalesOrderItemDAO;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;
import com.erpflow.model.Item;

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

        if (salesOrder == null) {

            throw new RuntimeException(
                    "Sales order cannot be null"
            );
        }


        if (
                salesOrderItems == null ||
                salesOrderItems.isEmpty()
        ) {

            throw new RuntimeException(
                    "Sales Order must contain at least one item"
            );
        }


        // =====================================================
        // TAX RATE
        // =====================================================

        BigDecimal taxRate =
                salesOrder.getTaxRate();


        if (taxRate == null) {

            taxRate =
                    BigDecimal.ZERO;

        }


        if (
                taxRate.compareTo(BigDecimal.ZERO) < 0 ||
                taxRate.compareTo(
                        new BigDecimal("100")
                ) > 0
        ) {

            throw new RuntimeException(
                    "Tax rate must be between 0 and 100"
            );
        }


        taxRate =
                taxRate.setScale(
                        2,
                        RoundingMode.HALF_UP
                );


        // =====================================================
        // VALIDATE ITEMS + CALCULATE SUBTOTAL
        // =====================================================
        

        BigDecimal subtotal =
                BigDecimal.ZERO;


        Set<Integer> itemIds =
                new HashSet<>();


        for (
                SalesOrderItem orderItem :
                salesOrderItems
        ) {

            if (orderItem.getItem() == null) {

                throw new RuntimeException(
                        "Item cannot be null"
                );
            }


            if (
                    orderItem.getQuantity() <= 0
            ) {

                throw new RuntimeException(
                        "Quantity must be greater than 0"
                );
            }


            if (
                    orderItem.getSellingPrice() == null
            ) {

                throw new RuntimeException(
                        "Selling price is required"
                );
            }


            if (
                    orderItem.getSellingPrice()
                            .compareTo(
                                    BigDecimal.ZERO
                            ) < 0
            ) {

                throw new RuntimeException(
                        "Selling price cannot be negative"
                );
            }


            // Prevent duplicate item rows

           

            BigDecimal lineTotal =
                    orderItem
                            .getSellingPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            orderItem.getQuantity()
                                    )
                            );


            subtotal =
                    subtotal.add(lineTotal);
        }


        subtotal =
                subtotal.setScale(
                        2,
                        RoundingMode.HALF_UP
                );


        // =====================================================
        // CALCULATE TAX
        // =====================================================

        BigDecimal taxAmount =
                subtotal
                        .multiply(taxRate)
                        .divide(
                                new BigDecimal("100"),
                                2,
                                RoundingMode.HALF_UP
                        );


        // =====================================================
        // CALCULATE GRAND TOTAL
        // =====================================================

        BigDecimal totalAmount =
                subtotal.add(taxAmount);


        totalAmount =
                totalAmount.setScale(
                        2,
                        RoundingMode.HALF_UP
                );


     

        salesOrder.setTaxRate(
                taxRate
        );

        salesOrder.setSubtotal(
                subtotal
        );

        salesOrder.setTaxAmount(
                taxAmount
        );

        salesOrder.setTotalAmount(
                totalAmount
        );


        // =====================================================
        // RESERVE STOCK
        // =====================================================

        // =====================================================
// RESERVE STOCK - GOODS ONLY
// =====================================================


 // RESERVE STOCK ONLY FOR TRACKED ITEMS

for (SalesOrderItem orderItem : salesOrderItems) {

    Item item = orderItem.getItem();

    if (item == null) {
        throw new RuntimeException(
                "Item is required for Sales Order"
        );
    }

    if (item.isTrackInventory()) {

        inventoryService.reserveStock(
                item,
                orderItem.getQuantity()
        );
    }
}


        // =====================================================
        // SAVE SALES ORDER
        // =====================================================

        salesOrderDAO.save(
                salesOrder
        );


        // =====================================================
        // SAVE ORDER ITEMS
        // =====================================================

        for (
                SalesOrderItem orderItem :
                salesOrderItems
        ) {

            orderItem.setSalesOrder(
                    salesOrder
            );


            salesOrderItemDAO.save(
                    orderItem
            );
        }
    }


    // =========================================================
    // GET ALL
    // =========================================================

    public List<SalesOrder> getAllSalesOrders() {

        return salesOrderDAO.findAll();
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    public SalesOrder getSalesOrderById(
            int id
    ) {

        return salesOrderDAO.findById(id);
    }


    // =========================================================
    // GET ITEMS
    // =========================================================

    public List<SalesOrderItem> getSalesOrderItems(
            SalesOrder salesOrder
    ) {

        return salesOrderItemDAO.findBySalesOrder(
                salesOrder
        );
    }
}