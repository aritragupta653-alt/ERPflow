package com.erpflow.model;

import java.math.BigDecimal;

public class SalesOrderItem {

    private int id;
    private SalesOrder salesOrder;
    private Item item;
    private int quantity;
    private int packedQuantity;
    private int shippedQuantity;
    private BigDecimal sellingPrice;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SalesOrder getSalesOrder() {
        return salesOrder;
    }

    public void setSalesOrder(SalesOrder salesOrder) {
        this.salesOrder = salesOrder;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPackedQuantity() {
        return packedQuantity;
    }

    public void setPackedQuantity(int packedQuantity) {
        this.packedQuantity = packedQuantity;
    }

    public int getShippedQuantity() {
        return shippedQuantity;
    }

    public void setShippedQuantity(int shippedQuantity) {
        this.shippedQuantity = shippedQuantity;
    }

    public int getRemainingToPack() {
        return Math.max(0, quantity - packedQuantity);
    }

    public int getRemainingToShip() {
        return Math.max(0, packedQuantity - shippedQuantity);
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }
}
