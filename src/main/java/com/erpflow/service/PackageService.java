package com.erpflow.service;

import com.erpflow.dao.PackageDAO;
import com.erpflow.dao.PackageItemDAO;
import com.erpflow.model.Package;
import com.erpflow.model.PackageItem;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;

import java.time.LocalDateTime;
import java.util.List;

public class PackageService {

    private final PackageDAO packageDAO =
            new PackageDAO();

    private final PackageItemDAO packageItemDAO =
            new PackageItemDAO();


    // CREATE PACKAGE

    public void createPackage(
            SalesOrder salesOrder,
            List<PackageItem> packageItems,
            double weight) {

        if (salesOrder == null) {

            throw new RuntimeException(
                    "Sales Order not found"
            );
        }


        // Only CREATED orders can be packaged

        if (!"CREATED".equals(
                salesOrder.getStatus())) {

            throw new RuntimeException(
                    "Only CREATED Sales Orders can be packaged"
            );
        }


        if (packageItems == null ||
                packageItems.isEmpty()) {

            throw new RuntimeException(
                    "Package must contain at least one item"
            );
        }


        if (weight < 0) {

            throw new RuntimeException(
                    "Package weight cannot be negative"
            );
        }


        // Validate package quantities

        List<SalesOrderItem> salesOrderItems =
                new SalesOrderService()
                        .getSalesOrderItems(
                                salesOrder
                        );


        for (PackageItem packageItem :
                packageItems) {

            boolean validItem = false;

            for (SalesOrderItem orderItem :
                    salesOrderItems) {

                if (orderItem.getItem().getId()
                        == packageItem.getItem().getId()) {

                    validItem = true;

                    if (packageItem.getQuantity()
                            > orderItem.getQuantity()) {

                        throw new RuntimeException(
                                "Package quantity cannot exceed Sales Order quantity"
                        );
                    }

                    break;
                }
            }


            if (!validItem) {

                throw new RuntimeException(
                        "Item does not belong to Sales Order"
                );
            }


            if (packageItem.getQuantity() <= 0) {

                throw new RuntimeException(
                        "Package quantity must be greater than zero"
                );
            }
        }


        // Create Package

        Package packageEntity =
                new Package();

        packageEntity.setSalesOrder(
                salesOrder
        );

        packageEntity.setStatus(
                "PACKING"
        );

        packageEntity.setPackageDate(
                LocalDateTime.now()
        );

        packageEntity.setWeight(
                weight
        );


        // Save Package

        packageDAO.save(
                packageEntity
        );


        // Save Package Items

        for (PackageItem packageItem :
                packageItems) {

            packageItem.setPackageEntity(
                    packageEntity
            );

            packageItemDAO.save(
                    packageItem
            );
        }


        // Packaging completed

        packageEntity.setStatus(
                "PACKED"
        );

        packageDAO.update(
                packageEntity
        );
    }


    // GET ALL PACKAGES

    public List<Package> getAllPackages() {

        return packageDAO.findAll();
    }


    // GET PACKAGE BY ID

    public Package getPackageById(int id) {

        return packageDAO.findById(id);
    }


    // GET PACKAGES BY SALES ORDER

    public List<Package> getPackagesBySalesOrder(
            SalesOrder salesOrder) {

        return packageDAO.findBySalesOrder(
                salesOrder
        );
    }


    // GET ITEMS INSIDE PACKAGE

    public List<PackageItem> getPackageItems(
            Package packageEntity) {

        return packageItemDAO.findByPackage(
                packageEntity
        );
    }
}