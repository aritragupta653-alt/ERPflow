package com.erpflow.model;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory")
public class Inventory {

   
    private int id;

   
    private Item item;

    
    private int quantity;

    
    private int committedQuantity;

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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getCommittedQuantity() {
        return committedQuantity;
    }

    public void setCommittedQuantity(int committedQuantity) {
        this.committedQuantity = committedQuantity;
    }
}