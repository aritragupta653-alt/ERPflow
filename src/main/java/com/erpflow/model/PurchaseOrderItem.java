package com.erpflow.model;

import java.math.BigDecimal;

public class PurchaseOrderItem {

    private int id;
    private PurchaseOrder purchaseOrder;
    private Item item;
    private int quantity;
    private BigDecimal purchasePrice;

    private int recievedQuantity ;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
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

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public void setReceivedQuantity(int q){
        this.recievedQuantity = q;
    }
    public int getRecievedQuantity(){
        return recievedQuantity;
    }
}