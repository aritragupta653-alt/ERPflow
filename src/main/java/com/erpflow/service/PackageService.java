
package com.erpflow.service;

import com.erpflow.dao.PackageDAO;
import com.erpflow.dao.PackageItemDAO;
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

        if (packageItems == null || packageItems.isEmpty()) {
            throw new RuntimeException(
                    "Package must contain at least one item"
            );
        }

        if (weight <= 0) {
            throw new RuntimeException(
                    "Package weight must be greater than zero"
            );
        }

        if (length <= 0 || width <= 0 || height <= 0) {
            throw new RuntimeException(
                    "Package dimensions must be greater than zero"
            );
        }


        // -------------------------
        // ORDER STATUS
        // -------------------------

        String status = salesOrder.getStatus();

        if ("SHIPPED".equals(status) ||
                "DELIVERED".equals(status)) {

            throw new RuntimeException(
                    "Cannot create package for a completed Sales Order"
            );
        }


        // -------------------------
        // GET ORDER LINES
        // -------------------------

        List<SalesOrderItem> orderItems =
                salesOrderItemDAO.findBySalesOrder(salesOrder);

        if (orderItems == null || orderItems.isEmpty()) {
            throw new RuntimeException(
                    "Sales Order contains no items"
            );
        }


        // Map sales order line ID -> order line.
        // This supports the same product appearing on
        // multiple distinct lines.

        Map<Integer, SalesOrderItem> orderLinesById =
                new HashMap<>();

        for (SalesOrderItem orderItem : orderItems) {

            orderLinesById.put(
                    orderItem.getId(),
                    orderItem
            );
        }


        // -------------------------
        // GET ALREADY PACKED QTY
        // -------------------------

        Map<Integer, Integer> alreadyPacked =
                getAlreadyPackagedQuantities(
                        salesOrder.getId()
                );


        // -------------------------
        // VALIDATE PACKAGE LINES
        // -------------------------

        // Aggregate quantities by sales order line ID
        // so duplicate entries for the same line in this
        // package are validated together.

        Map<Integer, Integer> requestedQuantities =
                new HashMap<>();

        Map<Integer, Item> actualItemsByLineId =
                new HashMap<>();

        for (PackageItem packageItem : packageItems) {

            if (packageItem == null || packageItem.getItem() == null) {
                throw new RuntimeException(
                        "Package item is missing"
                );
            }

            Integer salesOrderItemId =
                    packageItem.getSalesOrderItemId();

            if (salesOrderItemId == null) {
                throw new RuntimeException(
                        "Sales order line ID is required for every package item"
                );
            }

            int quantity = packageItem.getQuantity();

            if (quantity <= 0) {
                throw new RuntimeException(
                        "Package quantity must be greater than zero"
                );
            }


            // Find the exact order line.
            SalesOrderItem orderLine =
                    orderLinesById.get(salesOrderItemId);

            if (orderLine == null) {
                throw new RuntimeException(
                        "Sales order line does not belong to this Sales Order: "
                                + salesOrderItemId
                );
            }


            // Ensure the product ID matches the selected line.
            // Ensure the Sales Order line has a valid item.
Item actualItem = orderLine.getItem();

if (actualItem == null) {
    throw new RuntimeException(
            "Item not found in the Sales Order line"
    );
}

// Ensure the product ID matches the selected line.
int requestedItemId =
        packageItem.getItem().getId();

int orderItemId =
        actualItem.getId();

if (requestedItemId != orderItemId) {
    throw new RuntimeException(
            "Item does not match the selected Sales Order line"
    );
}

// Packages can contain only inventory-tracked GOODS.
// Services and non-tracked goods must be rejected.
if (!"GOODS".equalsIgnoreCase(actualItem.getItemType())
        || !actualItem.isTrackInventory()) {

    throw new RuntimeException(
            "Only inventory-tracked goods can be added to packages"
    );
}


            // Add this quantity to the requested total
            // for this particular order line.
            requestedQuantities.merge(
                    salesOrderItemId,
                    quantity,
                    Integer::sum
            );

            // Use the canonical item object from the order.
            actualItemsByLineId.put(
                    salesOrderItemId,
                    orderLine.getItem()
            );
        }


        // -------------------------
        // CHECK REMAINING QUANTITIES
        // -------------------------

        for (Map.Entry<Integer, Integer> entry :
                requestedQuantities.entrySet()) {

            int salesOrderItemId = entry.getKey();
            int requestedQuantity = entry.getValue();

            SalesOrderItem orderLine =
                    orderLinesById.get(salesOrderItemId);

            int orderedQuantity =
                    orderLine.getQuantity();

            int previouslyPacked =
                    alreadyPacked.getOrDefault(
                            salesOrderItemId,
                            0
                    );

            int newPackedTotal =
                    previouslyPacked + requestedQuantity;

            if (newPackedTotal > orderedQuantity) {
                throw new RuntimeException(
                        "Package quantity exceeds the remaining quantity for Sales Order line "
                                + salesOrderItemId
                                + ". Ordered: "
                                + orderedQuantity
                                + ", already packed: "
                                + previouslyPacked
                                + ", requested: "
                                + requestedQuantity
                );
            }
        }


        // -------------------------
        // SET CANONICAL ITEM OBJECTS
        // -------------------------

        for (PackageItem packageItem : packageItems) {

            int lineId =
                    packageItem.getSalesOrderItemId();

            packageItem.setItem(
                    actualItemsByLineId.get(lineId)
            );
        }


        // =====================================================
        // CREATE PACKAGE RECORD
        // =====================================================

        Package packageEntity = new Package();

        packageEntity.setSalesOrder(salesOrder);
        packageEntity.setStatus("PACKING");
        packageEntity.setPackageDate(LocalDateTime.now());

        packageEntity.setWeight(weight);
        packageEntity.setLength(length);
        packageEntity.setWidth(width);
        packageEntity.setHeight(height);


        // Save package first to generate its database ID.
        packageDAO.save(packageEntity);


        // Generate and save package number.
        packageEntity.setPackageNumber(
                String.format(
                        "PKG-%06d",
                        packageEntity.getId()
                )
        );

        packageDAO.update(packageEntity);


        // =====================================================
        // SAVE PACKAGE ITEMS
        // =====================================================

        for (PackageItem packageItem : packageItems) {

            packageItem.setPackageEntity(packageEntity);

            packageItemDAO.save(packageItem);
        }


        // =====================================================
        // MARK PACKAGE AS PACKED
        // =====================================================

        packageEntity.setStatus("PACKED");

        packageDAO.update(packageEntity);


        return packageEntity;
    }


    // =====================================================
    // GET ALL PACKAGES
    // =====================================================

    public List<Package> getAllPackages() {

        return packageDAO.findAll();
    }


    // =====================================================
    // GET PACKAGE BY ID
    // =====================================================

    public Package getPackageById(int id) {

        return packageDAO.findById(id);
    }


    // =====================================================
    // GET PACKAGES BY SALES ORDER
    // =====================================================

    public List<Package> getPackagesBySalesOrder(
            int salesOrderId) {

        return packageDAO.findBySalesOrder(salesOrderId);
    }


    // =====================================================
    // GET PACKAGE ITEMS
    // =====================================================

    public List<PackageItem> getPackageItems(
            Package packageEntity) {

        return packageItemDAO.findByPackage(packageEntity);
    }


    // =====================================================
    // CALCULATE ALREADY PACKAGED QUANTITIES
    // =====================================================

    private Map<Integer, Integer> getAlreadyPackagedQuantities(
            int salesOrderId) {

        // Key: sales order line ID
        // Value: total quantity packed for that line.

        Map<Integer, Integer> result =
                new HashMap<>();

        List<Package> packages =
                packageDAO.findBySalesOrder(salesOrderId);

        for (Package pkg : packages) {

            // Do not count cancelled packages.
            if ("CANCELLED".equals(pkg.getStatus())) {
                continue;
            }

            List<PackageItem> items =
                    packageItemDAO.findByPackage(pkg);

            for (PackageItem packageItem : items) {

                Integer lineId =
                        packageItem.getSalesOrderItemId();

                // Older package records may not have a line ID.
                // They cannot safely be attributed to a specific
                // line when duplicate products exist.
                if (lineId == null) {
                    continue;
                }

                result.merge(
                        lineId,
                        packageItem.getQuantity(),
                        Integer::sum
                );
            }
        }

        return result;
    }
}