
package com.erpflow.model;

import com.erpflow.model.enums.ItemStatus;
import java.math.BigDecimal;

public class Item {

    private int id;
    private String name;
    private String sku;
    private String description;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private Integer reorderLevel;
    private ItemStatus status;
    private String itemType = "GOODS";
    private boolean trackInventory = true;
    private double length;
    private double width;
    private double height;
    private int inHandQuantity;
    private int committedQuantity;
    private Integer maxStockQuantity;

    public Item() {
    }

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

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
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
    public double getLength() {
    return length;
    }

    public void setLength(double length) {
    this.length = length;
    }

    public double getWidth() {
    return width;
    }

    public void setWidth(double width) {
    this.width = width;
    }

    public double getHeight() {
    return height;
    }

    public void setHeight(double height) {
    this.height = height;
    }
    public int getInHandQuantity() {
    return inHandQuantity;
}

public void setInHandQuantity(int inHandQuantity) {
    this.inHandQuantity = inHandQuantity;
}

public int getCommittedQuantity() {
    return committedQuantity;
}

public void setCommittedQuantity(int committedQuantity) {
    this.committedQuantity = committedQuantity;
}
public int getAvailableQuantity() {
    return inHandQuantity - committedQuantity;
}

public Integer getMaxStockQuantity() {
    return maxStockQuantity;
}

public void setMaxStockQuantity(Integer maxStockQuantity) {
    this.maxStockQuantity = maxStockQuantity;
}
}