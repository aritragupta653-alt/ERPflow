package com.erpflow.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private int id;


    // WHICH ITEM?

    @ManyToOne
    @JoinColumn(
            name = "item_id",
            nullable = false
    )
    private Item item;


    // STOCK_IN OR STOCK_OUT

    @Column(nullable = false)
    private String type;


    // HOW MANY ITEMS?

    @Column(nullable = false)
    private int quantity;


    // WHEN DID IT HAPPEN?

    @Column(nullable = false)
    private LocalDateTime transactionDate;


    // GETTERS AND SETTERS


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public Item getItem() {
        return item;
    }


    public void setItem(Item item) {
        this.item = item;
    }


    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }


    public int getQuantity() {
        return quantity;
    }


    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }


    public void setTransactionDate(
            LocalDateTime transactionDate
    ) {
        this.transactionDate = transactionDate;
    }
}