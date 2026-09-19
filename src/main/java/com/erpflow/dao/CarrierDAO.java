package com.erpflow.dao;

import com.erpflow.model.Carrier;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarrierDAO {

    public void save(Carrier carrier) {

        String sql = """
                INSERT INTO carriers (name, code, status)
                VALUES (?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, carrier.getName());
            ps.setString(2, carrier.getCode());
            ps.setString(3, carrier.getStatus());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    carrier.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save carrier", e);
        }
    }

    public List<Carrier> findAll() {

        List<Carrier> carriers = new ArrayList<>();

        String sql = """
                SELECT id, name, code, status
                FROM carriers
                ORDER BY id
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Carrier carrier = new Carrier();

                carrier.setId(rs.getInt("id"));
                carrier.setName(rs.getString("name"));
                carrier.setCode(rs.getString("code"));
                carrier.setStatus(rs.getString("status"));

                carriers.add(carrier);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch carriers", e);
        }

        return carriers;
    }

    public Carrier findById(int id) {

        String sql = """
                SELECT id, name, code, status
                FROM carriers
                WHERE id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Carrier carrier = new Carrier();

                    carrier.setId(rs.getInt("id"));
                    carrier.setName(rs.getString("name"));
                    carrier.setCode(rs.getString("code"));
                    carrier.setStatus(rs.getString("status"));

                    return carrier;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find carrier", e);
        }

        return null;
    }
}