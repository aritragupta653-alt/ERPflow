package com.erpflow.dao;

import com.erpflow.model.Shipment;
import com.erpflow.model.Package;
import com.erpflow.model.Carrier;
import com.erpflow.model.CarrierService;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.Customer;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShipmentDAO {

    // =========================
    // SAVE SHIPMENT
    // =========================

    public void save(Shipment shipment) {

        String sql = """
                INSERT INTO shipments
                (
                    shipment_number,
                    shipmentDate,
                    status,
                    shipping_method,
                    carrier_id,
                    carrier_service_id,
                    tracking_number,
                    tracking_url,
                    shipping_charge,
                    dispatch_address,
                    destination_address,
                    estimated_delivery_date,
                    actual_delivery_date,
                    notes
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setString(1, shipment.getShipmentNumber());
            statement.setTimestamp(
                    2,
                    Timestamp.valueOf(shipment.getShipmentDate())
            );
            statement.setString(3, shipment.getStatus());
            statement.setString(4, shipment.getShippingMethod());

            if (shipment.getCarrier() != null) {
                statement.setInt(5, shipment.getCarrier().getId());
            } else {
                statement.setNull(5, Types.INTEGER);
            }

            if (shipment.getCarrierService() != null) {
                statement.setInt(
                        6,
                        shipment.getCarrierService().getId()
                );
            } else {
                statement.setNull(6, Types.INTEGER);
            }

            statement.setString(7, shipment.getTrackingNumber());
            statement.setString(8, shipment.getTrackingUrl());
            statement.setDouble(9, shipment.getShippingCharge());
            statement.setString(10, shipment.getDispatchAddress());
            statement.setString(11, shipment.getDestinationAddress());

            if (shipment.getEstimatedDeliveryDate() != null) {
                statement.setDate(
                        12,
                        Date.valueOf(
                                shipment.getEstimatedDeliveryDate()
                        )
                );
            } else {
                statement.setNull(12, Types.DATE);
            }

            if (shipment.getActualDeliveryDate() != null) {
                statement.setDate(
                        13,
                        Date.valueOf(
                                shipment.getActualDeliveryDate()
                        )
                );
            } else {
                statement.setNull(13, Types.DATE);
            }

            statement.setString(14, shipment.getNotes());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {

                if (keys.next()) {
                    shipment.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to save shipment",
                    e
            );
        }
    }


    // =========================
    // UPDATE SHIPMENT
    // =========================

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
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, shipment.getShipmentNumber());

            statement.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            shipment.getShipmentDate()
                    )
            );

            statement.setString(3, shipment.getStatus());
            statement.setString(4, shipment.getShippingMethod());

            if (shipment.getCarrier() != null) {
                statement.setInt(
                        5,
                        shipment.getCarrier().getId()
                );
            } else {
                statement.setNull(5, Types.INTEGER);
            }

            if (shipment.getCarrierService() != null) {
                statement.setInt(
                        6,
                        shipment.getCarrierService().getId()
                );
            } else {
                statement.setNull(6, Types.INTEGER);
            }

            statement.setString(7, shipment.getTrackingNumber());
            statement.setString(8, shipment.getTrackingUrl());
            statement.setDouble(9, shipment.getShippingCharge());
            statement.setString(10, shipment.getDispatchAddress());
            statement.setString(11, shipment.getDestinationAddress());

            if (shipment.getEstimatedDeliveryDate() != null) {
                statement.setDate(
                        12,
                        Date.valueOf(
                                shipment.getEstimatedDeliveryDate()
                        )
                );
            } else {
                statement.setNull(12, Types.DATE);
            }

            if (shipment.getActualDeliveryDate() != null) {
                statement.setDate(
                        13,
                        Date.valueOf(
                                shipment.getActualDeliveryDate()
                        )
                );
            } else {
                statement.setNull(13, Types.DATE);
            }

            statement.setString(14, shipment.getNotes());
            statement.setInt(15, shipment.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update shipment",
                    e
            );
        }
    }


    // =========================
    // FIND ALL
    // =========================

    public List<Shipment> findAll() {

        String sql = """
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
                LEFT JOIN carriers c
                    ON s.carrier_id = c.id
                LEFT JOIN carrier_services cs
                    ON s.carrier_service_id = cs.id
                ORDER BY s.shipmentDate DESC
                """;

        List<Shipment> shipments = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {

                Shipment shipment = mapShipment(rs);

                shipment.setPackages(
                        findPackagesForShipment(
                                connection,
                                shipment.getId()
                        )
                );

                shipments.add(shipment);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch shipments",
                    e
            );
        }

        return shipments;
    }


    // =========================
    // FIND BY ID
    // =========================

    public Shipment findById(int id) {

        String sql = """
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
                LEFT JOIN carriers c
                    ON s.carrier_id = c.id
                LEFT JOIN carrier_services cs
                    ON s.carrier_service_id = cs.id
                WHERE s.id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                Shipment shipment = mapShipment(rs);

                shipment.setPackages(
                        findPackagesForShipment(
                                connection,
                                id
                        )
                );

                return shipment;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Failed to fetch shipment: " + e.getMessage(), e
            );
        }
    }


    // =========================
    // LINK PACKAGE TO SHIPMENT
    // =========================

    public void addPackage(
            int shipmentId,
            int packageId) {

        String sql = """
                INSERT INTO shipment_packages
                (
                    shipment_id,
                    package_id
                )
                VALUES (?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, shipmentId);
            statement.setInt(2, packageId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to link package to shipment",
                    e
            );
        }
    }


    // =========================
    // CHECK PACKAGE ALREADY SHIPPED
    // =========================

    public boolean isPackageAlreadyShipped(int packageId) {

        String sql = """
                SELECT COUNT(*)
                FROM shipment_packages
                WHERE package_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, packageId);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to check package shipment status",
                    e
            );
        }

        return false;
    }


    // =========================
    // GET PACKAGES OF SHIPMENT
    // =========================

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
                p.sales_order_id,

                so.id AS sales_order_id,
                so.status AS sales_order_status,

                c.id AS customer_id,
                c.name AS customer_name,
                c.email AS customer_email

            FROM shipment_packages sp

            JOIN packages p
                ON sp.package_id = p.id

            LEFT JOIN sales_orders so
                ON p.sales_order_id = so.id

            LEFT JOIN customers c
                ON so.customer_id = c.id

            WHERE sp.shipment_id = ?

            ORDER BY p.id
            """;

    List<Package> packages = new ArrayList<>();

    try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setInt(1, shipmentId);

        try (ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {

                Package pkg = new Package();

                // =========================
                // PACKAGE
                // =========================

                pkg.setId(
                        rs.getInt("id")
                );

                pkg.setPackageNumber(
                        rs.getString("package_number")
                );

                pkg.setStatus(
                        rs.getString("status")
                );

                Timestamp timestamp =
                        rs.getTimestamp("packageDate");

                if (timestamp != null) {
                    pkg.setPackageDate(
                            timestamp.toLocalDateTime()
                    );
                }

                pkg.setWeight(
                        rs.getDouble("weight")
                );

                pkg.setLength(
                        rs.getDouble("length")
                );

                pkg.setWidth(
                        rs.getDouble("width")
                );

                pkg.setHeight(
                        rs.getDouble("height")
                );


                // =========================
                // SALES ORDER
                // =========================

                int salesOrderId =
                        rs.getInt("sales_order_id");

                if (!rs.wasNull()) {

                    SalesOrder salesOrder =
                            new SalesOrder();

                    salesOrder.setId(
                            salesOrderId
                    );

                    

                    salesOrder.setStatus(
                            rs.getString("sales_order_status")
                    );


                    // =========================
                    // CUSTOMER
                    // =========================

                    int customerId =
                            rs.getInt("customer_id");

                    if (!rs.wasNull()) {

                        Customer customer =
                                new Customer();

                        customer.setId(customerId);

                        customer.setName(
                                rs.getString("customer_name")
                        );

                        customer.setEmail(
                                rs.getString("customer_email")
                        );

                        salesOrder.setCustomer(customer);
                    }

                    pkg.setSalesOrder(salesOrder);
                }

                packages.add(pkg);
            }
        }
    }

    return packages;
}


    // =========================
    // MAP SHIPMENT
    // =========================

    private Shipment mapShipment(ResultSet rs)
            throws SQLException {

        Shipment shipment = new Shipment();

        shipment.setId(
                rs.getInt("id")
        );

        shipment.setShipmentNumber(
                rs.getString("shipment_number")
        );

        Timestamp shipmentTimestamp =
                rs.getTimestamp("shipmentDate");

        if (shipmentTimestamp != null) {
            shipment.setShipmentDate(
                    shipmentTimestamp.toLocalDateTime()
            );
        }

        shipment.setStatus(
                rs.getString("status")
        );

        shipment.setShippingMethod(
                rs.getString("shipping_method")
        );

        shipment.setTrackingNumber(
                rs.getString("tracking_number")
        );

        shipment.setTrackingUrl(
                rs.getString("tracking_url")
        );

        shipment.setShippingCharge(
                rs.getDouble("shipping_charge")
        );

        shipment.setDispatchAddress(
                rs.getString("dispatch_address")
        );

        shipment.setDestinationAddress(
                rs.getString("destination_address")
        );

        Date estimatedDate =
                rs.getDate("estimated_delivery_date");

        if (estimatedDate != null) {
            shipment.setEstimatedDeliveryDate(
                    estimatedDate.toLocalDate()
            );
        }

        Date actualDate =
                rs.getDate("actual_delivery_date");

        if (actualDate != null) {
            shipment.setActualDeliveryDate(
                    actualDate.toLocalDate()
            );
        }

        shipment.setNotes(
                rs.getString("notes")
        );


        // Carrier

        int carrierId =
                rs.getInt("carrier_id");

        if (!rs.wasNull()) {

            Carrier carrier = new Carrier();

            carrier.setId(carrierId);
            carrier.setName(
                    rs.getString("carrier_name")
            );
            carrier.setCode(
                    rs.getString("carrier_code")
            );
            carrier.setStatus(
        rs.getString("carrier_status")
);

            shipment.setCarrier(carrier);
        }


        // Carrier Service

        int serviceId =
                rs.getInt("service_id");

        if (!rs.wasNull()) {

            CarrierService service =
                    new CarrierService();

            service.setId(serviceId);

            service.setName(
                    rs.getString("service_name")
            );

            service.setEstimatedDays(
        rs.getInt("estimated_days")
);

service.setBaseCharge(
        rs.getBigDecimal("service_base_charge")
);

service.setChargePerKg(
        rs.getBigDecimal("service_charge_per_kg")
);

// Attach carrier to the service as well
if (shipment.getCarrier() != null) {
    service.setCarrier(
            shipment.getCarrier()
    );
}

shipment.setCarrierService(service);
        }

        return shipment;
    }
}