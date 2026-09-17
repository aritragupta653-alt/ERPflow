package com.erpflow.dao;

import com.erpflow.model.Customer;
import com.erpflow.model.Package;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.Shipment;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShipmentDAO {

    public void save(Shipment shipment) {

        String sql = """
                INSERT INTO shipments
                (shipmentDate, status, package_id)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setTimestamp(
                    1,
                    Timestamp.valueOf(shipment.getShipmentDate())
            );

            statement.setString(2, shipment.getStatus());
            statement.setInt(3, shipment.getPackageEntity().getId());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    shipment.setId(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save shipment", e);
        }
    }

    public List<Shipment> findAll() {

        String sql = """
                SELECT
                    s.id AS shipment_id,
                    s.shipmentDate,
                    s.status AS shipment_status,
                    
                    p.id AS package_id,
                    p.packageDate,
                    p.status AS package_status,
                    p.weight,
                    
                    so.id AS sales_order_id,
                    so.orderDate,
                    so.status AS sales_order_status,
                    
                    c.id AS customer_id,
                    c.name AS customer_name,
                    c.email AS customer_email,
                    c.phone AS customer_phone,
                    c.address AS customer_address,
                    c.status AS customer_status
                    
                FROM shipments s
                
                JOIN packages p
                    ON s.package_id = p.id
                
                JOIN sales_orders so
                    ON p.sales_order_id = so.id
                
                JOIN customers c
                    ON so.customer_id = c.id
                
                ORDER BY s.shipmentDate DESC
                """;

        List<Shipment> shipments = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                shipments.add(mapShipment(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch shipments", e);
        }

        return shipments;
    }

    public Shipment findById(int id) {

        String sql = """
                SELECT
                    s.id AS shipment_id,
                    s.shipmentDate,
                    s.status AS shipment_status,
                    
                    p.id AS package_id,
                    p.packageDate,
                    p.status AS package_status,
                    p.weight,
                    
                    so.id AS sales_order_id,
                    so.orderDate,
                    so.status AS sales_order_status,
                    
                    c.id AS customer_id,
                    c.name AS customer_name,
                    c.email AS customer_email,
                    c.phone AS customer_phone,
                    c.address AS customer_address,
                    c.status AS customer_status
                    
                FROM shipments s
                
                JOIN packages p
                    ON s.package_id = p.id
                
                JOIN sales_orders so
                    ON p.sales_order_id = so.id
                
                JOIN customers c
                    ON so.customer_id = c.id
                
                WHERE s.id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return mapShipment(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch shipment", e);
        }

        return null;
    }

    private Shipment mapShipment(ResultSet rs) throws SQLException {

        // Customer
        Customer customer = new Customer();

        customer.setId(rs.getInt("customer_id"));
        customer.setName(rs.getString("customer_name"));
        customer.setEmail(rs.getString("customer_email"));
        customer.setPhone(rs.getString("customer_phone"));
        customer.setAddress(rs.getString("customer_address"));
        customer.setStatus(rs.getString("customer_status"));

        // Sales Order
        SalesOrder salesOrder = new SalesOrder();

        salesOrder.setId(rs.getInt("sales_order_id"));
        salesOrder.setOrderDate(
                rs.getTimestamp("orderDate").toLocalDateTime()
        );
        salesOrder.setStatus(
                rs.getString("sales_order_status")
        );
        salesOrder.setCustomer(customer);

        // Package
        Package packageEntity = new Package();

        packageEntity.setId(rs.getInt("package_id"));

        packageEntity.setPackageDate(
                rs.getTimestamp("packageDate").toLocalDateTime()
        );

        packageEntity.setStatus(
                rs.getString("package_status")
        );

        packageEntity.setWeight(
                rs.getDouble("weight")
        );

        packageEntity.setSalesOrder(salesOrder);

        // Shipment
        Shipment shipment = new Shipment();

        shipment.setId(rs.getInt("shipment_id"));

        shipment.setShipmentDate(
                rs.getTimestamp("shipmentDate").toLocalDateTime()
        );

        shipment.setStatus(
                rs.getString("shipment_status")
        );

        shipment.setPackageEntity(packageEntity);

        return shipment;
    }
}