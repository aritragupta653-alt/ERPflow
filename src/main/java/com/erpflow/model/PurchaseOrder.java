package com.erpflow.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private int id;


    // SUPPLIER FOR THIS PURCHASE ORDER

    @ManyToOne
    @JoinColumn(
            name = "supplier_id",
            nullable = false
    )
    private Supplier supplier;


    // STATUS OF PURCHASE ORDER

    @Column(nullable = false)
    private String status;


    // WHEN WAS THE ORDER CREATED?

    @Column(nullable = false)
    private LocalDateTime orderDate;


    // GETTERS AND SETTERS


    public int getId() {

        return id;
    }


    public void setId(int id) {

        this.id = id;
    }


    public Supplier getSupplier() {

        return supplier;
    }


    public void setSupplier(
            Supplier supplier
    ) {

        this.supplier = supplier;
    }


    public String getStatus() {

        return status;
    }


    public void setStatus(
            String status
    ) {

        this.status = status;
    }


    public LocalDateTime getOrderDate() {

        return orderDate;
    }


    public void setOrderDate(
            LocalDateTime orderDate
    ) {

        this.orderDate = orderDate;
    }
}