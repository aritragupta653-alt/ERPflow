package com.erpflow.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "items")
public class Item {

    private int id;

    private String name;

    private String sku;

    private String description;

    private BigDecimal purchasePrice;

    private BigDecimal sellingPrice;

    private Integer reorderLevel;

    private String status;

    private String itemType = "GOODS";
    private boolean trackInventory = true;

    // Required by Hibernate
    public Item() {
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }
    public String getStatus() {
    return status;
}

public void setStatus(String status) {
    this.status = status;
}

public String getItemType() {
    return itemType;
}

public void setItemType(String itemType) {
    this.itemType = itemType;
}

public boolean isTrackInventory() {
    return trackInventory;
}

public void setTrackInventory(boolean trackInventory) {
    this.trackInventory = trackInventory;
}
}