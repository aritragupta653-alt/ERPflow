package com.erpflow.service;

import com.erpflow.dao.SupplierDAO;
import com.erpflow.model.Supplier;

import java.util.List;

public class SupplierService {

    private final SupplierDAO supplierDAO =
            new SupplierDAO();


    // ADD SUPPLIER

    public void addSupplier(
            Supplier supplier
    ) {

        if (supplier == null) {
            throw new RuntimeException(
                    "Supplier is required"
            );
        }

        if (supplier.getName() == null ||
                supplier.getName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Supplier name is required"
            );
        }


        supplierDAO.save(
                supplier
        );
    }


    // GET ALL

    public List<Supplier> getAllSuppliers() {

        return supplierDAO.findAll();
    }


    // GET BY ID

    public Supplier getSupplierById(
            int id
    ) {

        return supplierDAO.findById(id);
    }


    // UPDATE

    public void updateSupplier(
            Supplier supplier
    ) {

        if (supplier == null) {
            throw new RuntimeException(
                    "Supplier is required"
            );
        }

        supplierDAO.update(
                supplier
        );
    }


    // DELETE

    public void deleteSupplier(
            int id
    ) {

        supplierDAO.delete(id);
    }
}