
package com.erpflow.service;

import com.erpflow.dao.ItemComponentDAO;
import com.erpflow.dao.ItemDAO;
import com.erpflow.model.Item;
import com.erpflow.model.ItemComponent;
import com.erpflow.model.enums.ItemStatus;
import com.erpflow.util.TransactionManager;

import java.math.BigDecimal;
import java.util.List;

public class CompositeItemService {

    private final ItemDAO itemDAO = new ItemDAO();
    private final ItemComponentDAO itemComponentDAO =
            new ItemComponentDAO();
    private final ItemComponentService itemComponentService =
            new ItemComponentService();

    // =========================================================
    // CREATE COMPOSITE ITEM
    // Creates the item and its component associations atomically.
    // =========================================================

    public Item createCompositeItem(
            Item composite,
            List<ItemComponent> requestedComponents) {

        validateCompositeDetails(composite);

        return TransactionManager.execute(() -> {

            // Validate and load component details from the database.
            List<ItemComponent> components =
                    itemComponentService.validateComponents(
                            requestedComponents
                    );

            BigDecimal sellingPrice = BigDecimal.ZERO;
            BigDecimal purchasePrice = BigDecimal.ZERO;

            for (ItemComponent component : components) {

                Item sourceItem = itemDAO.findById(
                        component.getComponentItemId()
                );

                if (sourceItem == null) {
                    throw new IllegalArgumentException(
                            "Component item not found: "
                                    + component.getComponentItemId()
                    );
                }

                BigDecimal quantity =
                        BigDecimal.valueOf(component.getQuantity());

                sellingPrice = sellingPrice.add(
                        sourceItem.getSellingPrice().multiply(quantity)
                );

                purchasePrice = purchasePrice.add(
                        sourceItem.getPurchasePrice().multiply(quantity)
                );
            }

            // Derive both prices from the selected components.
            composite.setItemType("COMPOSITE");
            composite.setSellingPrice(sellingPrice);
            composite.setPurchasePrice(purchasePrice);

            if (composite.getStatus() == null) {
                composite.setStatus(ItemStatus.ACTIVE);
            }

            if (composite.getReorderLevel() == null) {
                composite.setReorderLevel(0);
            }

            // New composite inventory starts at zero.
            // Tracking remains controlled by the user's selection.
            composite.setInHandQuantity(0);
            composite.setCommittedQuantity(0);

            // Save the parent first to obtain its generated item_id.
            itemDAO.save(composite);

            if (composite.getId() <= 0) {
                throw new IllegalStateException(
                        "Composite item was saved without a valid ID"
                );
            }

            // Save all component associations.
            for (ItemComponent component : components) {

                component.setCompositeItemId(composite.getId());

                itemComponentDAO.insertComponent(component);
            }

            return composite;
        });
    }

    // =========================================================
    // GET COMPOSITE COMPONENTS
    // =========================================================

    public List<ItemComponent> getComponents(int compositeItemId) {
        return itemComponentService.getComponents(compositeItemId);
    }

    // =========================================================
    // GET ITEMS AVAILABLE AS COMPONENTS
    // =========================================================

    public List<ItemComponent> getEligibleComponents() {
        return itemComponentService.getEligibleComponents();
    }

    // =========================================================
    // VALIDATE COMPOSITE DETAILS
    // =========================================================

    private void validateCompositeDetails(Item composite) {

        if (composite == null) {
            throw new IllegalArgumentException(
                    "Composite item details are required"
            );
        }

        if (composite.getName() == null
                || composite.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Composite item name is required"
            );
        }

        if (composite.getSku() == null
                || composite.getSku().isBlank()) {
            throw new IllegalArgumentException(
                    "Composite item SKU is required"
            );
        }

        if (composite.getName().length() > 255) {
            throw new IllegalArgumentException(
                    "Composite item name is too long"
            );
        }

        if (composite.getSku().length() > 100) {
            throw new IllegalArgumentException(
                    "Composite item SKU is too long"
            );
        }

        // The service calculates these values itself.
        composite.setItemType("COMPOSITE");
    }
}
