package com.erpflow.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private int id;


    // WHICH PURCHASE ORDER?

    @ManyToOne
    @JoinColumn(
            name = "purchase_order_id",
            nullable = false
    )
    private PurchaseOrder purchaseOrder;


    // WHICH ITEM?

    @ManyToOne
    @JoinColumn(
            name = "item_id",
            nullable = false
    )
    private Item item;


    // HOW MANY?

    @Column(nullable = false)
    private int quantity;


    // PURCHASE PRICE AT THE TIME OF ORDER

    @Column(
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal purchasePrice;


    // GETTERS AND SETTERS


    public int getId() {

        return id;
    }


    public void setId(int id) {

        this.id = id;
    }


    public PurchaseOrder getPurchaseOrder() {

        return purchaseOrder;
    }


    public void setPurchaseOrder(
            PurchaseOrder purchaseOrder
    ) {

        this.purchaseOrder = purchaseOrder;
    }


    public Item getItem() {

        return item;
    }


    public void setItem(
            Item item
    ) {

        this.item = item;
    }


    public int getQuantity() {

        return quantity;
    }


    public void setQuantity(
            int quantity
    ) {

        this.quantity = quantity;
    }


    public BigDecimal getPurchasePrice() {

        return purchasePrice;
    }


    public void setPurchasePrice(
            BigDecimal purchasePrice
    ) {

        this.purchasePrice = purchasePrice;
    }
}