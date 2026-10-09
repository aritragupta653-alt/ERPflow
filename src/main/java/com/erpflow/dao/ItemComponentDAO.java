
package com.erpflow.dao;

import com.erpflow.model.ItemComponent;
import com.erpflow.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemComponentDAO {

    // =========================================================
    // FIND COMPONENTS FOR A COMPOSITE ITEM
    // =========================================================

    public List<ItemComponent> findByCompositeItemId(int compositeItemId) {

        String sql = """
                SELECT
                    ic.composite_item_id,
                    ic.component_item_id,
                    ic.quantity,
                    i.name AS component_name,
                    i.sku AS component_sku,
                    i.item_type AS component_item_type,
                    i.selling_price AS component_selling_price,
                    COALESCE(i.in_hand_quantity, 0) AS in_hand_quantity,
                    COALESCE(i.committed_quantity, 0) AS committed_quantity,
                    (
                        COALESCE(i.in_hand_quantity, 0)
                        - COALESCE(i.committed_quantity, 0)
                    ) AS available_quantity
                FROM item_components ic
                INNER JOIN items i
                    ON i.item_id = ic.component_item_id
                WHERE ic.composite_item_id = ?
                ORDER BY i.name
                """;

        List<ItemComponent> components = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, compositeItemId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    components.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching components for composite item "
                            + compositeItemId, e
            );
        }

        return components;
    }

    // =========================================================
    // FIND ELIGIBLE COMPONENT ITEMS
    // =========================================================

    public List<ItemComponent> findEligibleComponents() {

        String sql = """
                SELECT
                    i.item_id AS component_item_id,
                    i.name AS component_name,
                    i.sku AS component_sku,
                    i.item_type AS component_item_type,
                    i.selling_price AS component_selling_price,
                    COALESCE(i.in_hand_quantity, 0) AS in_hand_quantity,
                    COALESCE(i.committed_quantity, 0) AS committed_quantity,
                    (
                        COALESCE(i.in_hand_quantity, 0)
                        - COALESCE(i.committed_quantity, 0)
                    ) AS available_quantity
                FROM items i
                WHERE UPPER(TRIM(i.item_type)) IN ('GOODS', 'SERVICE')
                  AND UPPER(TRIM(i.status)) = 'ACTIVE'
                ORDER BY i.name
                """;

        List<ItemComponent> components = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                components.add(mapEligibleRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error fetching eligible component items", e
            );
        }

        return components;
    }

    // =========================================================
    // INSERT ONE COMPONENT ASSOCIATION
    // Call inside the service's transaction when creating
    // or replacing a composite item's components.
    // =========================================================

    public void insertComponent(ItemComponent component) {

        String sql = """
                INSERT INTO item_components
                    (composite_item_id, component_item_id, quantity)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, component.getCompositeItemId());
            statement.setInt(2, component.getComponentItemId());
            statement.setInt(3, component.getQuantity());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error saving component item "
                            + component.getComponentItemId()
                            + " for composite item "
                            + component.getCompositeItemId(),
                    e
            );
        }
    }

    // =========================================================
    // DELETE ALL COMPONENT ASSOCIATIONS
    // The caller should insert the replacement list in the
    // same transaction.
    // =========================================================

    public void deleteByCompositeItemId(int compositeItemId) {

        String sql = """
                DELETE FROM item_components
                WHERE composite_item_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, compositeItemId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error deleting components for composite item "
                            + compositeItemId, e
            );
        }
    }

    // =========================================================
    // CONSUME TRACKED GOODS STOCK
    //
    // requiredQuantity is the total needed for this assembly:
    // component quantity per composite * production quantity.
    //
    // Returns false if the item is not tracked GOODS or if
    // available stock is insufficient.
    // =========================================================

    public boolean consumeTrackedGoodsStock(
            int componentItemId,
            int requiredQuantity) {

        if (componentItemId <= 0 || requiredQuantity <= 0) {
            throw new IllegalArgumentException(
                    "A valid component ID and positive quantity are required"
            );
        }

        String sql = """
                UPDATE items
                SET in_hand_quantity = in_hand_quantity - ?
                WHERE item_id = ?
                  AND UPPER(TRIM(item_type)) = 'GOODS'
                  AND track_inventory = 1
                  AND (
                      COALESCE(in_hand_quantity, 0)
                      - COALESCE(committed_quantity, 0)
                  ) >= ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, requiredQuantity);
            statement.setInt(2, componentItemId);
            statement.setInt(3, requiredQuantity);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error consuming stock for component item "
                            + componentItemId, e
            );
        }
    }

    // =========================================================
    // INCREASE COMPOSITE STOCK
    //
    // Increases stock only if this is a tracked COMPOSITE item.
    // Returns false when the item is not configured for tracking.
    // =========================================================

    public boolean increaseCompositeStock(
            int compositeItemId,
            int productionQuantity) {

        if (compositeItemId <= 0 || productionQuantity <= 0) {
            throw new IllegalArgumentException(
                    "A valid composite ID and positive production quantity "
                            + "are required"
            );
        }

        String sql = """
                UPDATE items
                SET in_hand_quantity =
                    COALESCE(in_hand_quantity, 0) + ?
                WHERE item_id = ?
                  AND UPPER(TRIM(item_type)) = 'COMPOSITE'
                  AND track_inventory = 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, productionQuantity);
            statement.setInt(2, compositeItemId);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error increasing stock for composite item "
                            + compositeItemId, e
            );
        }
    }

    // =========================================================
    // RESULT SET MAPPERS
    // =========================================================

    private ItemComponent mapRow(ResultSet rs) throws SQLException {

        ItemComponent component = new ItemComponent();

        component.setCompositeItemId(
                rs.getInt("composite_item_id")
        );
        component.setComponentItemId(
                rs.getInt("component_item_id")
        );
        component.setQuantity(
                rs.getInt("quantity")
        );
        component.setComponentName(
                rs.getString("component_name")
        );
        component.setComponentSku(
                rs.getString("component_sku")
        );
        component.setComponentItemType(
                rs.getString("component_item_type")
        );
        component.setComponentSellingPrice(
                rs.getBigDecimal("component_selling_price")
        );
        component.setInHandQuantity(
                rs.getInt("in_hand_quantity")
        );
        component.setCommittedQuantity(
                rs.getInt("committed_quantity")
        );
        component.setAvailableQuantity(
                rs.getInt("available_quantity")
        );

        return component;
    }

    private ItemComponent mapEligibleRow(ResultSet rs)
            throws SQLException {

        ItemComponent component = new ItemComponent();

        component.setComponentItemId(
                rs.getInt("component_item_id")
        );
        component.setComponentName(
                rs.getString("component_name")
        );
        component.setComponentSku(
                rs.getString("component_sku")
        );
        component.setComponentItemType(
                rs.getString("component_item_type")
        );
        component.setComponentSellingPrice(
                rs.getBigDecimal("component_selling_price")
        );
        component.setInHandQuantity(
                rs.getInt("in_hand_quantity")
        );
        component.setCommittedQuantity(
                rs.getInt("committed_quantity")
        );
        component.setAvailableQuantity(
                rs.getInt("available_quantity")
        );

        return component;
    }
}
