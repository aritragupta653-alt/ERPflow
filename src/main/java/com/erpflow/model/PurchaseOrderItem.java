
package com.erpflow.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItem {

    private int id;
    private PurchaseOrder purchaseOrder;
    private Item item;
    private int quantity;
    private BigDecimal purchasePrice;
    private int recievedQuantity;

    public PurchaseOrderItem() {
    }

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

    public int getRecievedQuantity() {
        return recievedQuantity;
    }

    public void setReceivedQuantity(int recievedQuantity) {
        this.recievedQuantity = recievedQuantity;
    }

    public int getRemainingQuantity() {
        return Math.max(0, quantity - recievedQuantity);
    }
}