
package com.erpflow.service;

import com.erpflow.dao.ItemComponentDAO;
import com.erpflow.dao.ItemDAO;
import com.erpflow.model.Item;
import com.erpflow.model.ItemComponent;
import com.erpflow.util.TransactionManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AssemblyService {

    private final ItemDAO itemDAO = new ItemDAO();

    private final ItemComponentDAO itemComponentDAO =
            new ItemComponentDAO();

    // =========================================================
    // ASSEMBLE COMPOSITE ITEMS
    // =========================================================

    public void assemble(int compositeItemId, int productionQuantity) {

        if (compositeItemId <= 0) {
            throw new IllegalArgumentException(
                    "A valid composite item ID is required"
            );
        }

        if (productionQuantity <= 0) {
            throw new IllegalArgumentException(
                    "Production quantity must be greater than zero"
            );
        }

        TransactionManager.execute(() -> {

            // -------------------------------------------------
            // 1. Validate the composite item.
            // -------------------------------------------------

            Item composite = itemDAO.findById(compositeItemId);

            if (composite == null) {
                throw new IllegalArgumentException(
                        "Composite item not found"
                );
            }

            if (!"COMPOSITE".equalsIgnoreCase(
                    composite.getItemType())) {

                throw new IllegalArgumentException(
                        "The selected item is not a composite item"
                );
            }

            if (composite.getStatus() == null
                    || !"ACTIVE".equalsIgnoreCase(
                            composite.getStatus().name())) {

                throw new IllegalArgumentException(
                        "Inactive composite items cannot be assembled"
                );
            }

            // -------------------------------------------------
            // 2. Load component associations.
            // -------------------------------------------------

            List<ItemComponent> components =
                    itemComponentDAO.findByCompositeItemId(
                            compositeItemId
                    );

            if (components.isEmpty()) {
                throw new IllegalArgumentException(
                        "This composite item has no components"
                );
            }

            // Defensive validation. Each component should appear
            // only once in the association table.
            Set<Integer> componentIds = new HashSet<>();

            for (ItemComponent component : components) {

                if (component.getQuantity() <= 0) {
                    throw new IllegalStateException(
                            "Invalid component quantity for item "
                                    + component.getComponentItemId()
                    );
                }

                if (!componentIds.add(
                        component.getComponentItemId())) {

                    throw new IllegalStateException(
                            "Duplicate component association detected: "
                                    + component.getComponentItemId()
                    );
                }
            }

            // -------------------------------------------------
            // 3. Validate and consume tracked GOODS stock.
            //
            // required quantity =
            // component quantity per composite * production quantity
            //
            // SERVICE components do not consume stock.
            // Untracked GOODS do not consume stock either.
            // -------------------------------------------------

            for (ItemComponent component : components) {

                int requiredQuantity;

                try {
                    requiredQuantity = Math.multiplyExact(
                            component.getQuantity(),
                            productionQuantity
                    );
                } catch (ArithmeticException e) {
                    throw new IllegalArgumentException(
                            "Required quantity is too large for component "
                                    + component.getComponentItemId(),
                            e
                    );
                }

                int componentItemId =
                        component.getComponentItemId();

                // Reload item details inside the transaction.
                Item componentItem =
                        itemDAO.findById(componentItemId);

                if (componentItem == null) {
                    throw new IllegalStateException(
                            "Component item not found: "
                                    + componentItemId
                    );
                }

                if (componentItem.getStatus() == null
                        || !"ACTIVE".equalsIgnoreCase(
                                componentItem.getStatus().name())) {

                    throw new IllegalArgumentException(
                            "Inactive component cannot be assembled: "
                                    + componentItem.getName()
                    );
                }

                String itemType = componentItem.getItemType();

                if (itemType == null
                        || (!"GOODS".equalsIgnoreCase(itemType)
                        && !"SERVICE".equalsIgnoreCase(itemType))) {

                    throw new IllegalArgumentException(
                            "Only GOODS and SERVICE items are supported "
                                    + "as components: "
                                    + componentItem.getName()
                    );
                }

                // Services do not use physical inventory.
                if ("SERVICE".equalsIgnoreCase(itemType)) {
                    continue;
                }

                // Untracked goods do not change stock.
                if (!componentItem.isTrackInventory()) {
                    continue;
                }

                // The DAO performs an atomic conditional update:
                // stock is deducted only when sufficient available
                // quantity exists.
                boolean consumed =
                        itemComponentDAO.consumeTrackedGoodsStock(
                                componentItemId,
                                requiredQuantity
                        );

                if (!consumed) {
                    throw new IllegalStateException(
                            "Insufficient available stock for component '"
                                    + componentItem.getName()
                                    + "'. Required: "
                                    + requiredQuantity
                                    + ", available: "
                                    + componentItem.getAvailableQuantity()
                    );
                }
            }

            // -------------------------------------------------
            // 4. Increase composite stock only when tracking
            // is enabled for the composite item.
            // -------------------------------------------------

            if (composite.isTrackInventory()) {

                boolean increased =
                        itemComponentDAO.increaseCompositeStock(
                                compositeItemId,
                                productionQuantity
                        );

                if (!increased) {
                    throw new IllegalStateException(
                            "Could not update composite inventory"
                    );
                }
            }

            // TransactionManager commits only if all operations
            // above succeed. Any exception triggers rollback.
            return null;
        });
    }
}
