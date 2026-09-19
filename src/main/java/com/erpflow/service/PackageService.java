package com.erpflow.service;

import com.erpflow.dao.PackageDAO;
import com.erpflow.dao.PackageItemDAO;
import com.erpflow.dao.SalesOrderDAO;
import com.erpflow.dao.SalesOrderItemDAO;

import com.erpflow.model.Item;
import com.erpflow.model.Package;
import com.erpflow.model.PackageItem;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PackageService {

    private final PackageDAO packageDAO =
            new PackageDAO();

    private final PackageItemDAO packageItemDAO =
            new PackageItemDAO();

    private final SalesOrderDAO salesOrderDAO =
            new SalesOrderDAO();

    private final SalesOrderItemDAO salesOrderItemDAO =
            new SalesOrderItemDAO();


    // =====================================================
    // CREATE PACKAGE
    // =====================================================

    public Package createPackage(
            SalesOrder salesOrder,
            List<PackageItem> packageItems,
            double weight,
            double length,
            double width,
            double height) {

        // -------------------------
        // BASIC VALIDATION
        // -------------------------

        if (salesOrder == null) {

            throw new RuntimeException(
                    "Sales Order not found"
            );
        }

        if (packageItems == null ||
                packageItems.isEmpty()) {

            throw new RuntimeException(
                    "Package must contain at least one item"
            );
        }

        if (weight <= 0) {

            throw new RuntimeException(
                    "Package weight must be greater than zero"
            );
        }

        if (length <= 0 ||
                width <= 0 ||
                height <= 0) {

            throw new RuntimeException(
                    "Package dimensions must be greater than zero"
            );
        }


        // -------------------------
        // ORDER STATUS
        // -------------------------

        String status =
                salesOrder.getStatus();

        if ("SHIPPED".equals(status) ||
                "DELIVERED".equals(status)) {

            throw new RuntimeException(
                    "Cannot create package for a completed Sales Order"
            );
        }


        // -------------------------
        // GET ORDER ITEMS
        // -------------------------

        List<SalesOrderItem> orderItems =
                salesOrderItemDAO.findBySalesOrder(
                        salesOrder
                );

        if (orderItems == null ||
                orderItems.isEmpty()) {

            throw new RuntimeException(
                    "Sales Order contains no items"
            );
        }


        // Map item ID → ordered quantity

        Map<Integer, Integer> orderedQuantities =
                new HashMap<>();

        Map<Integer, Item> orderItemObjects =
                new HashMap<>();

        for (SalesOrderItem orderItem :
                orderItems) {

            int itemId =
                    orderItem.getItem().getId();

            orderedQuantities.put(
                    itemId,
                    orderItem.getQuantity()
            );

            orderItemObjects.put(
                    itemId,
                    orderItem.getItem()
            );
        }


        // -------------------------
        // CHECK DUPLICATE ITEMS
        // -------------------------

        Set<Integer> packageItemIds =
                new HashSet<>();

        for (PackageItem packageItem :
                packageItems) {

            if (packageItem.getItem() == null) {

                throw new RuntimeException(
                        "Package item is missing"
                );
            }

            int itemId =
                    packageItem.getItem().getId();

            if (!packageItemIds.add(itemId)) {

                throw new RuntimeException(
                        "The same item cannot be added twice to one package"
                );
            }
        }


        // -------------------------
        // EXISTING PACKAGED QTY
        // -------------------------

        Map<Integer, Integer> alreadyPackaged =
                getAlreadyPackagedQuantities(
                        salesOrder.getId()
                );


        // -------------------------
        // VALIDATE EACH ITEM
        // -------------------------

        for (PackageItem packageItem :
                packageItems) {

            int itemId =
                    packageItem.getItem().getId();

            int quantity =
                    packageItem.getQuantity();


            if (quantity <= 0) {

                throw new RuntimeException(
                        "Package quantity must be greater than zero"
                );
            }


            if (!orderedQuantities.containsKey(
                    itemId)) {

                throw new RuntimeException(
                        "Item does not belong to Sales Order"
                );
            }


            int orderedQuantity =
                    orderedQuantities.get(itemId);

            int previousPackageQuantity =
                    alreadyPackaged.getOrDefault(
                            itemId,
                            0
                    );

            int newTotal =
                    previousPackageQuantity
                            + quantity;


            if (newTotal > orderedQuantity) {

                throw new RuntimeException(
                        "Package quantity exceeds remaining Sales Order quantity for item "
                                + itemId
                );
            }


            // Replace item with the actual
            // item object from the order.

            packageItem.setItem(
                    orderItemObjects.get(itemId)
            );
        }


        // =====================================================
        // CREATE PACKAGE
        // =====================================================

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

        packageEntity.setLength(
                length
        );

        packageEntity.setWidth(
                width
        );

        packageEntity.setHeight(
                height
        );


        // Save package

        packageDAO.save(
                packageEntity
        );


        // Generate package number

        packageEntity.setPackageNumber(
                String.format(
                        "PKG-%06d",
                        packageEntity.getId()
                )
        );

        packageDAO.update(
                packageEntity
        );


        // =====================================================
        // SAVE PACKAGE ITEMS
        // =====================================================

        for (PackageItem packageItem :
                packageItems) {

            packageItem.setPackageEntity(
                    packageEntity
            );

            packageItemDAO.save(
                    packageItem
            );
        }


        // =====================================================
        // PACKING COMPLETE
        // =====================================================

        packageEntity.setStatus(
                "PACKED"
        );

        packageDAO.update(
                packageEntity
        );


        // Return completed package

        return packageEntity;
    }


    // =====================================================
    // GET ALL
    // =====================================================

    public List<Package> getAllPackages() {

        return packageDAO.findAll();
    }


    // =====================================================
    // GET BY ID
    // =====================================================

    public Package getPackageById(int id) {

        return packageDAO.findById(id);
    }


    // =====================================================
    // GET BY SALES ORDER
    // =====================================================

    public List<Package> getPackagesBySalesOrder(
            int salesOrderId) {

        return packageDAO.findBySalesOrder(
                salesOrderId
        );
    }


    // =====================================================
    // GET PACKAGE ITEMS
    // =====================================================

    public List<PackageItem> getPackageItems(
            Package packageEntity) {

        return packageItemDAO.findByPackage(
                packageEntity
        );
    }


    // =====================================================
    // CALCULATE ALREADY PACKAGED QUANTITIES
    // =====================================================

    private Map<Integer, Integer>
    getAlreadyPackagedQuantities(
            int salesOrderId) {

        Map<Integer, Integer> result =
                new HashMap<>();

        List<Package> packages =
                packageDAO.findBySalesOrder(
                        salesOrderId
                );

        for (Package pkg : packages) {

            // Do not count cancelled packages
            if ("CANCELLED".equals(
                    pkg.getStatus())) {

                continue;
            }

            List<PackageItem> items =
                    packageItemDAO.findByPackage(
                            pkg
                    );

            for (PackageItem packageItem :
                    items) {

                int itemId =
                        packageItem
                                .getItem()
                                .getId();

                int current =
                        result.getOrDefault(
                                itemId,
                                0
                        );

                result.put(
                        itemId,
                        current
                                + packageItem.getQuantity()
                );
            }
        }

        return result;
    }
}