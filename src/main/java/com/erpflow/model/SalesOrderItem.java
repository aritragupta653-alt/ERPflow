package com.erpflow.model;

import java.math.BigDecimal;

public class SalesOrderItem {

    private int id;
    private SalesOrder salesOrder;
    private Item item;
    private int quantity;
    private BigDecimal sellingPrice;


    // =========================
    // ID
    // =========================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    // =========================
    // SALES ORDER
    // =========================

    public SalesOrder getSalesOrder() {
        return salesOrder;
    }

    public void setSalesOrder(SalesOrder salesOrder) {
        this.salesOrder = salesOrder;
    }


    // =========================
    // ITEM
    // =========================

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }


    // =========================
    // QUANTITY
    // =========================

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    // =========================
    // SELLING PRICE
    // =========================

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }
}