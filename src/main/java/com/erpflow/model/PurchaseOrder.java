
package com.erpflow.model;

import com.erpflow.model.enums.PurchaseOrderStatus;
import java.time.LocalDateTime;

public class PurchaseOrder {

    private int id;
    private Supplier supplier;
    private PurchaseOrderStatus status;
    private LocalDateTime orderDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public PurchaseOrderStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseOrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
}