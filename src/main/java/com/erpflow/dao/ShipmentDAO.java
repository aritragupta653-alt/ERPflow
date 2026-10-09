package com.erpflow.dao;

import com.erpflow.model.Carrier;
import com.erpflow.model.CarrierService;
import com.erpflow.model.Customer;
import com.erpflow.model.Package;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.Shipment;
import com.erpflow.model.enums.CarrierStatus;
import com.erpflow.model.enums.CustomerStatus;
import com.erpflow.model.enums.PackageStatus;
import com.erpflow.model.enums.SalesOrderStatus;
import com.erpflow.model.enums.ShipmentStatus;
import com.erpflow.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShipmentDAO {

    public void save(Shipment shipment) {
        String sql = """
                INSERT INTO shipments
                    (shipment_number, shipmentDate, status, shipping_method,
                     carrier_id, carrier_service_id, tracking_number,
                     tracking_url, shipping_charge, dispatch_address,
                     destination_address, estimated_delivery_date,
                     actual_delivery_date, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            bindShipment(statement, shipment);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    shipment.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save shipment", e);
        }
    }

    public void update(Shipment shipment) {
        String sql = """
                UPDATE shipments
                SET shipment_number = ?,
                    shipmentDate = ?,
                    status = ?,
                    shipping_method = ?,
                    carrier_id = ?,
                    carrier_service_id = ?,
                    tracking_number = ?,
                    tracking_url = ?,
                    shipping_charge = ?,
                    dispatch_address = ?,
                    destination_address = ?,
                    estimated_delivery_date = ?,
                    actual_delivery_date = ?,
                    notes = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            bindShipment(statement, shipment);
            statement.setInt(15, shipment.getId());

            if (statement.executeUpdate() == 0) {
                throw new RuntimeException("Shipment was not found for update");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update shipment", e);
        }
    }

    private void bindShipment(PreparedStatement statement, Shipment shipment)
            throws SQLException {
        statement.setString(1, shipment.getShipmentNumber());
        statement.setDate(2, shipment.getShipmentDate() == null
                ? null : Date.valueOf(shipment.getShipmentDate()));
        statement.setString(3, shipment.getStatus() == null
                ? null : shipment.getStatus().name());
        statement.setString(4, shipment.getShippingMethod());

        if (shipment.getCarrier() == null) {
            statement.setNull(5, java.sql.Types.INTEGER);
        } else {
            statement.setInt(5, shipment.getCarrier().getId());
        }

        if (shipment.getCarrierService() == null) {
            statement.setNull(6, java.sql.Types.INTEGER);
        } else {
            statement.setInt(6, shipment.getCarrierService().getId());
        }

        statement.setString(7, shipment.getTrackingNumber());
        statement.setString(8, shipment.getTrackingUrl());
        statement.setDouble(9, shipment.getShippingCharge());
        statement.setString(10, shipment.getDispatchAddress());
        statement.setString(11, shipment.getDestinationAddress());
        statement.setDate(12, shipment.getEstimatedDeliveryDate() == null
                ? null : Date.valueOf(shipment.getEstimatedDeliveryDate()));
        statement.setDate(13, shipment.getActualDeliveryDate() == null
                ? null : Date.valueOf(shipment.getActualDeliveryDate()));
        statement.setString(14, shipment.getNotes());
    }

    public List<Shipment> findAll() {
        return findAll("ALL");
    }

    public List<Shipment> findAll(String status) {
        String normalized = status == null || status.isBlank()
                ? "ALL" : status.trim().toUpperCase();

        if (!"ALL".equals(normalized)) {
            try {
                ShipmentStatus.valueOf(normalized);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid shipment status: " + status);
            }
        }

        String sql = shipmentSelect()
                + " WHERE (? = 'ALL' OR s.status = ?)"
                + " ORDER BY s.shipmentDate DESC, s.id DESC";

        List<Shipment> shipments = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, normalized);
            statement.setString(2, normalized);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    shipments.add(mapShipment(rs));
                }
            }

            if (!shipments.isEmpty()) {
                java.util.Map<Integer, List<Package>> packagesByShipment =
                        findPackagesForShipments(connection, shipments);

                for (Shipment shipment : shipments) {
                    shipment.setPackages(
                            packagesByShipment.getOrDefault(
                                    shipment.getId(), new ArrayList<>()));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch shipments", e);
        }

        return shipments;
    }

    public Shipment findById(int id) {
        String sql = shipmentSelect() + " WHERE s.id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Shipment shipment = mapShipment(rs);
                shipment.setPackages(findPackagesForShipment(connection, id));
                return shipment;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch shipment", e);
        }
    }

    public void addPackage(int shipmentId, int packageId) {
        addPackages(shipmentId, List.of(packageId));
    }

    public void addPackages(int shipmentId, List<Integer> packageIds) {
        if (packageIds == null || packageIds.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO shipment_packages (shipment_id, package_id) VALUES (?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (Integer packageId : packageIds) {
                statement.setInt(1, shipmentId);
                statement.setInt(2, packageId);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to link packages to shipment", e);
        }
    }

    public boolean isPackageAlreadyShipped(int packageId) {
        String sql = "SELECT 1 FROM shipment_packages WHERE package_id = ? LIMIT 1";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, packageId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check package shipment status", e);
        }
    }

    public Set<Integer> findAlreadyShippedPackageIds(List<Integer> packageIds) {
        if (packageIds == null || packageIds.isEmpty()) {
            return new HashSet<>();
        }

        String placeholders = String.join(",", Collections.nCopies(packageIds.size(), "?"));
        String sql = "SELECT package_id FROM shipment_packages WHERE package_id IN ("
                + placeholders + ")";

        Set<Integer> result = new HashSet<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < packageIds.size(); i++) {
                statement.setInt(i + 1, packageIds.get(i));
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt("package_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check package shipment status", e);
        }

        return result;
    }

    public void replacePackages(int shipmentId, List<Integer> packageIds) {
        String deleteSql = "DELETE FROM shipment_packages WHERE shipment_id = ?";
        String insertSql = "INSERT INTO shipment_packages (shipment_id, package_id) VALUES (?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement delete = connection.prepareStatement(deleteSql);
             PreparedStatement insert = connection.prepareStatement(insertSql)) {

            delete.setInt(1, shipmentId);
            delete.executeUpdate();

            if (packageIds != null) {
                for (Integer packageId : packageIds) {
                    insert.setInt(1, shipmentId);
                    insert.setInt(2, packageId);
                    insert.addBatch();
                }
                insert.executeBatch();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update shipment packages", e);
        }
    }

    private String shipmentSelect() {
        return """
                SELECT
                    s.id,
                    s.shipment_number,
                    s.shipmentDate,
                    s.status,
                    s.shipping_method,
                    s.tracking_number,
                    s.tracking_url,
                    s.shipping_charge,
                    s.dispatch_address,
                    s.destination_address,
                    s.estimated_delivery_date,
                    s.actual_delivery_date,
                    s.notes,
                    c.id AS carrier_id,
                    c.name AS carrier_name,
                    c.code AS carrier_code,
                    c.status AS carrier_status,
                    cs.id AS service_id,
                    cs.name AS service_name,
                    cs.estimated_days,
                    cs.base_charge AS service_base_charge,
                    cs.charge_per_kg AS service_charge_per_kg
                FROM shipments s
                LEFT JOIN carriers c ON s.carrier_id = c.id
                LEFT JOIN carrier_services cs ON s.carrier_service_id = cs.id
                """;
    }

    private java.util.Map<Integer, List<Package>> findPackagesForShipments(
            Connection connection,
            List<Shipment> shipments) throws SQLException {

        String placeholders = String.join(",",
                Collections.nCopies(shipments.size(), "?"));

        String sql = """
                SELECT
                    sp.shipment_id,
                    p.id,
                    p.package_number,
                    p.status,
                    p.packageDate,
                    p.weight,
                    p.length,
                    p.width,
                    p.height,
                    so.id AS sales_order_id,
                    so.orderDate AS sales_order_date,
                    so.status AS sales_order_status,
                    c.id AS customer_id,
                    c.name AS customer_name,
                    c.email AS customer_email,
                    c.address AS customer_address
                FROM shipment_packages sp
                JOIN packages p ON sp.package_id = p.id
                LEFT JOIN sales_orders so ON p.sales_order_id = so.id
                LEFT JOIN customers c ON so.customer_id = c.id
                WHERE sp.shipment_id IN (%s)
                ORDER BY sp.shipment_id, p.id
                """.formatted(placeholders);

        java.util.Map<Integer, List<Package>> result = new java.util.HashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < shipments.size(); i++) {
                statement.setInt(i + 1, shipments.get(i).getId());
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Package pkg = mapPackageFromShipmentRow(rs);
                    result.computeIfAbsent(
                            rs.getInt("shipment_id"),
                            key -> new ArrayList<>()).add(pkg);
                }
            }
        }

        return result;
    }

    private Package mapPackageFromShipmentRow(ResultSet rs) throws SQLException {
        Package pkg = new Package();
        pkg.setId(rs.getInt("id"));
        pkg.setPackageNumber(rs.getString("package_number"));
        String status = rs.getString("status");
        pkg.setStatus(status == null ? null : PackageStatus.valueOf(status));

        Date packageDate = rs.getDate("packageDate");
        if (packageDate != null) {
            pkg.setPackageDate(packageDate.toLocalDate());
        }

        pkg.setWeight(rs.getDouble("weight"));
        pkg.setLength(rs.getDouble("length"));
        pkg.setWidth(rs.getDouble("width"));
        pkg.setHeight(rs.getDouble("height"));

        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setId(rs.getInt("sales_order_id"));
        Timestamp orderDate = rs.getTimestamp("sales_order_date");
        if (orderDate != null) {
            salesOrder.setOrderDate(orderDate.toLocalDateTime());
        }

        String orderStatus = rs.getString("sales_order_status");
        if (orderStatus != null) {
            salesOrder.setStatus(SalesOrderStatus.valueOf(orderStatus));
        }

        Customer customer = new Customer();
        customer.setId(rs.getInt("customer_id"));
        customer.setName(rs.getString("customer_name"));
        customer.setEmail(rs.getString("customer_email"));
        customer.setAddress(rs.getString("customer_address"));
        salesOrder.setCustomer(customer);
        pkg.setSalesOrder(salesOrder);
        return pkg;
    }

    private List<Package> findPackagesForShipment(
            Connection connection,
            int shipmentId) throws SQLException {

        String sql = """
                SELECT
                    p.id,
                    p.package_number,
                    p.status,
                    p.packageDate,
                    p.weight,
                    p.length,
                    p.width,
                    p.height,
                    so.id AS sales_order_id,
                    so.orderDate AS sales_order_date,
                    so.status AS sales_order_status,
                    c.id AS customer_id,
                    c.name AS customer_name,
                    c.email AS customer_email,
                    c.address AS customer_address
                FROM shipment_packages sp
                JOIN packages p ON sp.package_id = p.id
                LEFT JOIN sales_orders so ON p.sales_order_id = so.id
                LEFT JOIN customers c ON so.customer_id = c.id
                WHERE sp.shipment_id = ?
                ORDER BY p.id
                """;

        List<Package> packages = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shipmentId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Package pkg = new Package();
                    pkg.setId(rs.getInt("id"));
                    pkg.setPackageNumber(rs.getString("package_number"));
                    String status = rs.getString("status");
                    pkg.setStatus(status == null ? null : PackageStatus.valueOf(status));

                    Date packageDate = rs.getDate("packageDate");
                    if (packageDate != null) {
                        pkg.setPackageDate(packageDate.toLocalDate());
                    }

                    pkg.setWeight(rs.getDouble("weight"));
                    pkg.setLength(rs.getDouble("length"));
                    pkg.setWidth(rs.getDouble("width"));
                    pkg.setHeight(rs.getDouble("height"));

                    SalesOrder salesOrder = new SalesOrder();
                    salesOrder.setId(rs.getInt("sales_order_id"));
                    java.sql.Timestamp orderDate = rs.getTimestamp("sales_order_date");
                    if (orderDate != null) {
                        salesOrder.setOrderDate(orderDate.toLocalDateTime());
                    }

                    String orderStatus = rs.getString("sales_order_status");
                    if (orderStatus != null) {
                        salesOrder.setStatus(SalesOrderStatus.valueOf(orderStatus));
                    }

                    Customer customer = new Customer();
                    customer.setId(rs.getInt("customer_id"));
                    customer.setName(rs.getString("customer_name"));
                    customer.setEmail(rs.getString("customer_email"));
                    customer.setAddress(rs.getString("customer_address"));
                    salesOrder.setCustomer(customer);
                    pkg.setSalesOrder(salesOrder);

                    packages.add(pkg);
                }
            }
        }

        return packages;
    }

    private Shipment mapShipment(ResultSet rs) throws SQLException {
        Shipment shipment = new Shipment();
        shipment.setId(rs.getInt("id"));
        shipment.setShipmentNumber(rs.getString("shipment_number"));

        Date shipmentDate = rs.getDate("shipmentDate");
        if (shipmentDate != null) {
            shipment.setShipmentDate(shipmentDate.toLocalDate());
        }

        String status = rs.getString("status");
        shipment.setStatus(status == null ? null : ShipmentStatus.valueOf(status));
        shipment.setShippingMethod(rs.getString("shipping_method"));
        shipment.setTrackingNumber(rs.getString("tracking_number"));
        shipment.setTrackingUrl(rs.getString("tracking_url"));
        shipment.setShippingCharge(rs.getDouble("shipping_charge"));
        shipment.setDispatchAddress(rs.getString("dispatch_address"));
        shipment.setDestinationAddress(rs.getString("destination_address"));

        Date estimated = rs.getDate("estimated_delivery_date");
        if (estimated != null) {
            shipment.setEstimatedDeliveryDate(estimated.toLocalDate());
        }

        Date actual = rs.getDate("actual_delivery_date");
        if (actual != null) {
            shipment.setActualDeliveryDate(actual.toLocalDate());
        }

        shipment.setNotes(rs.getString("notes"));

        int carrierId = rs.getInt("carrier_id");
        if (!rs.wasNull()) {
            Carrier carrier = new Carrier();
            carrier.setId(carrierId);
            carrier.setName(rs.getString("carrier_name"));
            carrier.setCode(rs.getString("carrier_code"));
            String carrierStatus = rs.getString("carrier_status");
            if (carrierStatus != null) {
                carrier.setStatus(CarrierStatus.valueOf(carrierStatus));
            }
            shipment.setCarrier(carrier);
        }

        int serviceId = rs.getInt("service_id");
        if (!rs.wasNull()) {
            CarrierService service = new CarrierService();
            service.setId(serviceId);
            service.setName(rs.getString("service_name"));
            service.setEstimatedDays(rs.getInt("estimated_days"));
            service.setBaseCharge(rs.getBigDecimal("service_base_charge"));
            service.setChargePerKg(rs.getBigDecimal("service_charge_per_kg"));
            service.setCarrier(shipment.getCarrier());
            shipment.setCarrierService(service);
        }

        return shipment;
    }
}
