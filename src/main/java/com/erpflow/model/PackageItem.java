
package com.erpflow.model;

public class PackageItem {

    private int id;

    private Package packageEntity;

    private Item item;

    private int quantity;

    // ID of the specific sales order line this
    // package item belongs to.
    // Nullable because older package records may
    // not have a sales order line assigned.
    private Integer salesOrderItemId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Package getPackageEntity() {
        return packageEntity;
    }

    public void setPackageEntity(Package packageEntity) {
        this.packageEntity = packageEntity;
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

    public Integer getSalesOrderItemId() {
        return salesOrderItemId;
    }

    public void setSalesOrderItemId(Integer salesOrderItemId) {
        this.salesOrderItemId = salesOrderItemId;
    }
}