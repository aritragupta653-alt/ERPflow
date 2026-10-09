
package com.erpflow.model;

import java.math.BigDecimal;

/**
 * Represents one component used to build a composite item.
 *
 * The component's item details are included so the backend can provide
 * everything the Composite Items and Assembly screens need.
 */
public class ItemComponent {

    private int compositeItemId;
    private int componentItemId;
    private int quantity;

    private String componentName;
    private String componentSku;
    private String componentItemType;
    private BigDecimal componentSellingPrice;

    private int inHandQuantity;
    private int committedQuantity;
    private int availableQuantity;

    public ItemComponent() {
    }

    public int getCompositeItemId() {
        return compositeItemId;
    }

    public void setCompositeItemId(int compositeItemId) {
        this.compositeItemId = compositeItemId;
    }

    public int getComponentItemId() {
        return componentItemId;
    }

    public void setComponentItemId(int componentItemId) {
        this.componentItemId = componentItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentSku() {
        return componentSku;
    }

    public void setComponentSku(String componentSku) {
        this.componentSku = componentSku;
    }

    public String getComponentItemType() {
        return componentItemType;
    }

    public void setComponentItemType(String componentItemType) {
        this.componentItemType = componentItemType;
    }

    public BigDecimal getComponentSellingPrice() {
        return componentSellingPrice;
    }

    public void setComponentSellingPrice(BigDecimal componentSellingPrice) {
        this.componentSellingPrice = componentSellingPrice;
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
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
}
