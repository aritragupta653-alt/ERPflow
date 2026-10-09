
package com.erpflow.service;

import com.erpflow.dao.ItemComponentDAO;
import com.erpflow.dao.ItemDAO;
import com.erpflow.model.Item;
import com.erpflow.model.ItemComponent;
import com.erpflow.model.enums.ItemStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemComponentService {

    private final ItemComponentDAO itemComponentDAO =
            new ItemComponentDAO();

    private final ItemDAO itemDAO = new ItemDAO();

    // =========================================================
    // GET COMPONENTS FOR A COMPOSITE ITEM
    // =========================================================

    public List<ItemComponent> getComponents(int compositeItemId) {

        if (compositeItemId <= 0) {
            throw new IllegalArgumentException(
                    "A valid composite item ID is required"
            );
        }

        Item composite = itemDAO.findById(compositeItemId);

        if (composite == null) {
            throw new IllegalArgumentException(
                    "Composite item not found"
            );
        }

        if (!"COMPOSITE".equalsIgnoreCase(composite.getItemType())) {
            throw new IllegalArgumentException(
                    "The selected item is not a composite item"
            );
        }

        return itemComponentDAO.findByCompositeItemId(compositeItemId);
    }

    // =========================================================
    // GET ITEMS THAT CAN BE USED AS COMPONENTS
    // =========================================================

    public List<ItemComponent> getEligibleComponents() {

        List<ItemComponent> eligible =
                itemComponentDAO.findEligibleComponents();

        List<ItemComponent> activeComponents = new ArrayList<>();

        for (ItemComponent component : eligible) {

            Item item = itemDAO.findById(
                    component.getComponentItemId()
            );

            if (item != null
                    && item.getStatus() == ItemStatus.ACTIVE
                    && isAllowedComponentType(item.getItemType())) {

                activeComponents.add(component);
            }
        }

        return activeComponents;
    }

    // =========================================================
    // VALIDATE COMPONENTS BEFORE CREATING A COMPOSITE
    //
    // The incoming objects need only:
    //   componentItemId
    //   quantity
    //
    // Name, SKU, type, and price are loaded from the database.
    // =========================================================

    public List<ItemComponent> validateComponents(
            List<ItemComponent> requestedComponents) {

        if (requestedComponents == null
                || requestedComponents.isEmpty()) {

            throw new IllegalArgumentException(
                    "At least one component is required"
            );
        }

        List<ItemComponent> validatedComponents =
                new ArrayList<>();

        Set<Integer> componentIds = new HashSet<>();

        for (ItemComponent requested : requestedComponents) {

            if (requested == null) {
                throw new IllegalArgumentException(
                        "Component details cannot be empty"
                );
            }

            int componentId = requested.getComponentItemId();
            int quantity = requested.getQuantity();

            if (componentId <= 0) {
                throw new IllegalArgumentException(
                        "Every component must have a valid item ID"
                );
            }

            if (quantity <= 0) {
                throw new IllegalArgumentException(
                        "Component quantity must be greater than zero"
                );
            }

            if (!componentIds.add(componentId)) {
                throw new IllegalArgumentException(
                        "Duplicate component selected: item ID "
                                + componentId
                );
            }

            Item item = itemDAO.findById(componentId);

            if (item == null) {
                throw new IllegalArgumentException(
                        "Component item not found: " + componentId
                );
            }

            if (item.getStatus() != ItemStatus.ACTIVE) {
                throw new IllegalArgumentException(
                        "Inactive items cannot be used as components: "
                                + item.getName()
                );
            }

            if (!isAllowedComponentType(item.getItemType())) {
                throw new IllegalArgumentException(
                        "Only GOODS and SERVICE items can be components: "
                                + item.getName()
                );
            }

            if (item.getSellingPrice() == null
                    || item.getSellingPrice().signum() < 0) {

                throw new IllegalArgumentException(
                        "Invalid selling price for component: "
                                + item.getName()
                );
            }

            ItemComponent validated = new ItemComponent();

            validated.setComponentItemId(item.getId());
            validated.setQuantity(quantity);
            validated.setComponentName(item.getName());
            validated.setComponentSku(item.getSku());
            validated.setComponentItemType(item.getItemType());
            validated.setComponentSellingPrice(item.getSellingPrice());

            validated.setInHandQuantity(item.getInHandQuantity());
            validated.setCommittedQuantity(item.getCommittedQuantity());
            validated.setAvailableQuantity(item.getAvailableQuantity());

            validatedComponents.add(validated);
        }

        return validatedComponents;
    }

    // =========================================================
    // CALCULATE COMPOSITE SELLING PRICE
    //
    // Total = sum(component selling price * component quantity)
    // =========================================================

    public BigDecimal calculateCompositeSellingPrice(
            List<ItemComponent> components) {

        List<ItemComponent> validated =
                validateComponents(components);

        BigDecimal total = BigDecimal.ZERO;

        for (ItemComponent component : validated) {

            BigDecimal lineTotal =
                    component.getComponentSellingPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            component.getQuantity()
                                    )
                            );

            total = total.add(lineTotal);
        }

        return total;
    }

    // =========================================================
    // COMPONENT TYPE VALIDATION
    // =========================================================

    private boolean isAllowedComponentType(String itemType) {

        return itemType != null
                && (
                    "GOODS".equalsIgnoreCase(itemType)
                    || "SERVICE".equalsIgnoreCase(itemType)
                );
    }
}
