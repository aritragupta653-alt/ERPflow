package com.erpflow.dao;

import com.erpflow.model.Supplier;
import com.erpflow.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {


    // ========================================
    // SAVE SUPPLIER
    // ========================================

    public void save(Supplier supplier) {

        String sql =
                "INSERT INTO suppliers " +
                "(name, contact_person, phone, email, address) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            statement.setString(
                    1,
                    supplier.getName()
            );

            statement.setString(
                    2,
                    supplier.getContactPerson()
            );

            statement.setString(
                    3,
                    supplier.getPhone()
            );

            statement.setString(
                    4,
                    supplier.getEmail()
            );

            statement.setString(
                    5,
                    supplier.getAddress()
            );


            statement.executeUpdate();


            try (ResultSet rs =
                         statement.getGeneratedKeys()) {

                if (rs.next()) {

                    supplier.setId(
                            rs.getInt(1)
                    );
                }
            }


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to save supplier",
                    e
            );
        }
    }


    // ========================================
    // GET ALL SUPPLIERS
    // ========================================

    public List<Supplier> findAll() {

        String sql =
                "SELECT id, name, contact_person, " +
                "phone, email, address " +
                "FROM suppliers " +
                "ORDER BY id DESC";

        List<Supplier> suppliers =
                new ArrayList<>();


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet rs =
                        statement.executeQuery()
        ) {

            while (rs.next()) {

                suppliers.add(
                        mapSupplier(rs)
                );
            }

            return suppliers;


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch suppliers",
                    e
            );
        }
    }


    // ========================================
    // GET SUPPLIER BY ID
    // ========================================

    public Supplier findById(int id) {

        String sql =
                "SELECT id, name, contact_person, " +
                "phone, email, address " +
                "FROM suppliers " +
                "WHERE id = ?";


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    id
            );


            try (ResultSet rs =
                         statement.executeQuery()) {

                if (rs.next()) {

                    return mapSupplier(rs);
                }
            }


            return null;


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch supplier",
                    e
            );
        }
    }


    // ========================================
    // UPDATE SUPPLIER
    // ========================================

    public void update(
            Supplier supplier
    ) {

        String sql =
                "UPDATE suppliers " +
                "SET name = ?, " +
                "contact_person = ?, " +
                "phone = ?, " +
                "email = ?, " +
                "address = ? " +
                "WHERE id = ?";


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    supplier.getName()
            );

            statement.setString(
                    2,
                    supplier.getContactPerson()
            );

            statement.setString(
                    3,
                    supplier.getPhone()
            );

            statement.setString(
                    4,
                    supplier.getEmail()
            );

            statement.setString(
                    5,
                    supplier.getAddress()
            );

            statement.setInt(
                    6,
                    supplier.getId()
            );


            statement.executeUpdate();


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to update supplier",
                    e
            );
        }
    }


    // ========================================
    // DELETE SUPPLIER
    // ========================================

    public void delete(int id) {

        String sql =
                "DELETE FROM suppliers " +
                "WHERE id = ?";


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    id
            );

            statement.executeUpdate();


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to delete supplier",
                    e
            );
        }
    }


    // ========================================
    // MAP RESULT
    // ========================================

    private Supplier mapSupplier(
            ResultSet rs
    ) throws SQLException {

        Supplier supplier =
                new Supplier();


        supplier.setId(
                rs.getInt("id")
        );

        supplier.setName(
                rs.getString("name")
        );

        supplier.setContactPerson(
                rs.getString("contact_person")
        );

        supplier.setPhone(
                rs.getString("phone")
        );

        supplier.setEmail(
                rs.getString("email")
        );

        supplier.setAddress(
                rs.getString("address")
        );


        return supplier;
    }
}