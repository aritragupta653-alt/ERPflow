package com.erpflow.model;

import java.time.LocalDateTime;

public class Package {

    private int id;
    private SalesOrder salesOrder;
    private String status;
    private LocalDateTime packageDate;
    private double weight;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPackageDate() {
        return packageDate;
    }

    public void setPackageDate(LocalDateTime packageDate) {
        this.packageDate = packageDate;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}