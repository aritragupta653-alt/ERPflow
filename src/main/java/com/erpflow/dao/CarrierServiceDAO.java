package com.erpflow.dao;

import com.erpflow.model.Carrier;
import com.erpflow.model.CarrierService;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarrierServiceDAO {

    public void save(CarrierService service) {

        String sql = """
                INSERT INTO carrier_services
                (carrier_id, name, estimated_days, base_charge, charge_per_kg)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, service.getCarrier().getId());
            ps.setString(2, service.getName());
            ps.setInt(3, service.getEstimatedDays());
            ps.setBigDecimal(4, service.getBaseCharge());
            ps.setBigDecimal(5, service.getChargePerKg());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    service.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to save carrier service", e);
        }
    }

    public List<CarrierService> findByCarrier(int carrierId) {

        List<CarrierService> services = new ArrayList<>();

        String sql = """
                SELECT
                    cs.id,
                    cs.name,
                    cs.estimated_days,
                    cs.base_charge,
                    cs.charge_per_kg,
                    c.id AS carrier_id,
                    c.name AS carrier_name,
                    c.code AS carrier_code,
                    c.status AS carrier_status
                FROM carrier_services cs
                JOIN carriers c
                    ON cs.carrier_id = c.id
                WHERE cs.carrier_id = ?
                ORDER BY cs.id
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, carrierId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Carrier carrier = new Carrier();

                    carrier.setId(rs.getInt("carrier_id"));
                    carrier.setName(rs.getString("carrier_name"));
                    carrier.setCode(rs.getString("carrier_code"));
                    carrier.setStatus(rs.getString("carrier_status"));

                    CarrierService service = new CarrierService();

                    service.setId(rs.getInt("id"));
                    service.setCarrier(carrier);
                    service.setName(rs.getString("name"));
                    service.setEstimatedDays(
                            rs.getInt("estimated_days"));
                    service.setBaseCharge(
                            rs.getBigDecimal("base_charge"));
                    service.setChargePerKg(
                            rs.getBigDecimal("charge_per_kg"));

                    services.add(service);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch carrier services", e);
        }

        return services;
    }

    public CarrierService findById(int id) {

        String sql = """
                SELECT
                    cs.id,
                    cs.name,
                    cs.estimated_days,
                    cs.base_charge,
                    cs.charge_per_kg,
                    c.id AS carrier_id,
                    c.name AS carrier_name,
                    c.code AS carrier_code,
                    c.status AS carrier_status
                FROM carrier_services cs
                JOIN carriers c
                    ON cs.carrier_id = c.id
                WHERE cs.id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Carrier carrier = new Carrier();

                    carrier.setId(rs.getInt("carrier_id"));
                    carrier.setName(rs.getString("carrier_name"));
                    carrier.setCode(rs.getString("carrier_code"));
                    carrier.setStatus(rs.getString("carrier_status"));

                    CarrierService service = new CarrierService();

                    service.setId(rs.getInt("id"));
                    service.setCarrier(carrier);
                    service.setName(rs.getString("name"));
                    service.setEstimatedDays(
                            rs.getInt("estimated_days"));
                    service.setBaseCharge(
                            rs.getBigDecimal("base_charge"));
                    service.setChargePerKg(
                            rs.getBigDecimal("charge_per_kg"));

                    return service;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find carrier service", e);
        }

        return null;
    }
}