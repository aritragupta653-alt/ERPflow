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

        supplierDAO.save(supplier);
    }


    // GET ALL SUPPLIERS

    public List<Supplier> getAllSuppliers() {

        return supplierDAO.findAll();
    }


    // GET SUPPLIER BY ID

    public Supplier getSupplierById(
            int id
    ) {

        return supplierDAO.findById(id);
    }


    // UPDATE SUPPLIER

    public void updateSupplier(
            Supplier supplier
    ) {

        supplierDAO.update(supplier);
    }


    // DELETE SUPPLIER

    public void deleteSupplier(
            int id
    ) {

        supplierDAO.delete(id);
    }
}