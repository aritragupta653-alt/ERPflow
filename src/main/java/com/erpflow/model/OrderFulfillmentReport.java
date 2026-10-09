package com.erpflow.model;

public class OrderFulfillmentReport {

    private int orderId;
    private String orderDate;
    private String customerName;
    private String orderStatus;

    private int itemId;
    private String itemName;
    private String sku;

    private int orderedQuantity;
    private int packedQuantity;
    private int shippedQuantity;
    private int pendingQuantity;

    private double fulfillmentPercentage;


    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }


    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }


    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }


    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }


    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }


    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }


    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }


    public int getOrderedQuantity() {
        return orderedQuantity;
    }

    public void setOrderedQuantity(int orderedQuantity) {
        this.orderedQuantity = orderedQuantity;
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


    public int getPendingQuantity() {
        return pendingQuantity;
    }

    public void setPendingQuantity(int pendingQuantity) {
        this.pendingQuantity = pendingQuantity;
    }


    public double getFulfillmentPercentage() {
        return fulfillmentPercentage;
    }

    public void setFulfillmentPercentage(
            double fulfillmentPercentage) {

        this.fulfillmentPercentage =
                fulfillmentPercentage;
    }
}