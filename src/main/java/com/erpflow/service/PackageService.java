
package com.erpflow.service;

import com.erpflow.dao.PackageDAO;
import com.erpflow.dao.PackageItemDAO;
import com.erpflow.dao.SalesOrderItemDAO;

import com.erpflow.model.Item;
import com.erpflow.model.Package;
import com.erpflow.model.PackageItem;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;

import com.erpflow.model.enums.PackageStatus;
import com.erpflow.model.enums.SalesOrderStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackageService {

    private final PackageDAO packageDAO = new PackageDAO();
    private final PackageItemDAO packageItemDAO = new PackageItemDAO();
    private final SalesOrderItemDAO salesOrderItemDAO = new SalesOrderItemDAO();

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

        if (salesOrder == null) {
            throw new RuntimeException("Sales Order not found");
        }

        validateDimensions(weight, length, width, height);

        SalesOrderStatus orderStatus = salesOrder.getStatus();

        if (orderStatus == SalesOrderStatus.SHIPPED
                || orderStatus == SalesOrderStatus.COMPLETED
                || orderStatus == SalesOrderStatus.CANCELLED) {
            throw new RuntimeException(
                    "Cannot create a package for this Sales Order status"
            );
        }

        Map<Integer, SalesOrderItem> orderLines =
                getOrderLines(salesOrder);

        Map<Integer, Integer> alreadyPacked =
                getAlreadyPackagedQuantities(salesOrder.getId());

        Map<Integer, Integer> requested =
                validateAndAggregateItems(packageItems, orderLines);

        validateRemainingQuantities(requested, orderLines, alreadyPacked);

        setCanonicalItems(packageItems, orderLines);

        Package packageEntity = new Package();
        packageEntity.setSalesOrder(salesOrder);
        packageEntity.setStatus(PackageStatus.PACKING);
        packageEntity.setPackageDate(LocalDateTime.now());
        packageEntity.setWeight(weight);
        packageEntity.setLength(length);
        packageEntity.setWidth(width);
        packageEntity.setHeight(height);

        packageDAO.save(packageEntity);

        packageEntity.setPackageNumber(
                String.format("PKG-%06d", packageEntity.getId())
        );
        packageDAO.update(packageEntity);

        for (PackageItem packageItem : packageItems) {
            packageItem.setPackageEntity(packageEntity);
            packageItemDAO.save(packageItem);
        }

        packageEntity.setStatus(PackageStatus.PACKED);
        packageDAO.update(packageEntity);

        return packageEntity;
    }

    // =====================================================
    // EDIT PACKAGE
    // =====================================================

    public Package editPackage(
            int packageId,
            List<PackageItem> updatedItems,
            double weight,
            double length,
            double width,
            double height) {

        if (packageId <= 0) {
            throw new RuntimeException("Invalid package ID");
        }

        validateDimensions(weight, length, width, height);

        Package existingPackage = packageDAO.findById(packageId);

        if (existingPackage == null) {
            throw new RuntimeException("Package not found");
        }

        if (existingPackage.getStatus() != PackageStatus.PACKED) {
            throw new RuntimeException(
                    "Only packages with PACKED status can be edited"
            );
        }

        SalesOrder salesOrder = existingPackage.getSalesOrder();

        if (salesOrder == null) {
            throw new RuntimeException(
                    "The package is not associated with a Sales Order"
            );
        }

        SalesOrderStatus orderStatus = salesOrder.getStatus();

        if (orderStatus == SalesOrderStatus.SHIPPED
                || orderStatus == SalesOrderStatus.COMPLETED
                || orderStatus == SalesOrderStatus.CANCELLED) {
            throw new RuntimeException(
                    "Cannot edit a package for this Sales Order status"
            );
        }

        Map<Integer, SalesOrderItem> orderLines =
                getOrderLines(salesOrder);

        // Existing package quantities are included in the order's
        // packed totals. Remove this package's quantities before
        // validating the replacement quantities.
        Map<Integer, Integer> packedByLine =
                getAlreadyPackagedQuantities(salesOrder.getId());

        List<PackageItem> currentItems =
                packageItemDAO.findByPackage(existingPackage);

        for (PackageItem currentItem : currentItems) {
            Integer lineId = currentItem.getSalesOrderItemId();

            if (lineId != null) {
                int remaining =
                        packedByLine.getOrDefault(lineId, 0)
                                - currentItem.getQuantity();

                if (remaining <= 0) {
                    packedByLine.remove(lineId);
                } else {
                    packedByLine.put(lineId, remaining);
                }
            }
        }

        Map<Integer, Integer> requested =
                validateAndAggregateItems(updatedItems, orderLines);

        validateRemainingQuantities(requested, orderLines, packedByLine);

        setCanonicalItems(updatedItems, orderLines);

        // Update package dimensions.
        existingPackage.setWeight(weight);
        existingPackage.setLength(length);
        existingPackage.setWidth(width);
        existingPackage.setHeight(height);

        packageDAO.update(existingPackage);

        // Replace the package's contents only after all validations pass.
        packageItemDAO.deleteByPackageId(packageId);

        for (PackageItem packageItem : updatedItems) {
            packageItem.setPackageEntity(existingPackage);
            packageItemDAO.save(packageItem);
        }

        return existingPackage;
    }

    // =====================================================
    // VALIDATE DIMENSIONS
    // =====================================================

    private void validateDimensions(
            double weight,
            double length,
            double width,
            double height) {

        if (!Double.isFinite(weight)
                || !Double.isFinite(length)
                || !Double.isFinite(width)
                || !Double.isFinite(height)) {
            throw new RuntimeException(
                    "Package weight and dimensions must be valid numbers"
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
    }

    // =====================================================
    // GET ORDER LINES
    // =====================================================

    private Map<Integer, SalesOrderItem> getOrderLines(
            SalesOrder salesOrder) {

        List<SalesOrderItem> orderItems =
                salesOrderItemDAO.findBySalesOrder(salesOrder);

        if (orderItems == null || orderItems.isEmpty()) {
            throw new RuntimeException("Sales Order contains no items");
        }

        Map<Integer, SalesOrderItem> orderLines = new HashMap<>();

        for (SalesOrderItem orderItem : orderItems) {
            if (orderItem == null || orderItem.getId() <= 0) {
                throw new RuntimeException(
                        "Sales Order contains an invalid order line"
                );
            }

            orderLines.put(orderItem.getId(), orderItem);
        }

        return orderLines;
    }

    // =====================================================
    // VALIDATE AND AGGREGATE PACKAGE ITEMS
    // =====================================================

    private Map<Integer, Integer> validateAndAggregateItems(
            List<PackageItem> packageItems,
            Map<Integer, SalesOrderItem> orderLines) {

        if (packageItems == null || packageItems.isEmpty()) {
            throw new RuntimeException(
                    "Package must contain at least one item"
            );
        }

        Map<Integer, Integer> requestedQuantities = new HashMap<>();

        for (PackageItem packageItem : packageItems) {

            if (packageItem == null || packageItem.getItem() == null) {
                throw new RuntimeException("Package item is missing");
            }

            Integer lineId = packageItem.getSalesOrderItemId();

            if (lineId == null || lineId <= 0) {
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

            SalesOrderItem orderLine = orderLines.get(lineId);

            if (orderLine == null) {
                throw new RuntimeException(
                        "Sales order line does not belong to this Sales Order: "
                                + lineId
                );
            }

            Item actualItem = orderLine.getItem();

            if (actualItem == null) {
                throw new RuntimeException(
                        "Item not found in the Sales Order line"
                );
            }

            if (packageItem.getItem().getId() != actualItem.getId()) {
                throw new RuntimeException(
                        "Item does not match the selected Sales Order line"
                );
            }

            if (!"GOODS".equalsIgnoreCase(actualItem.getItemType())
                    || !actualItem.isTrackInventory()) {
                throw new RuntimeException(
                        "Only inventory-tracked goods can be added to packages"
                );
            }

            try {
                requestedQuantities.merge(
                        lineId,
                        quantity,
                        Math::addExact
                );
            } catch (ArithmeticException e) {
                throw new RuntimeException(
                        "Package quantity total is too large"
                );
            }
        }

        return requestedQuantities;
    }

    // =====================================================
    // VALIDATE ORDER QUANTITIES
    // =====================================================

    private void validateRemainingQuantities(
            Map<Integer, Integer> requested,
            Map<Integer, SalesOrderItem> orderLines,
            Map<Integer, Integer> alreadyPacked) {

        for (Map.Entry<Integer, Integer> entry : requested.entrySet()) {

            int lineId = entry.getKey();
            int requestedQuantity = entry.getValue();

            SalesOrderItem orderLine = orderLines.get(lineId);

            int orderedQuantity = orderLine.getQuantity();
            int previouslyPacked = alreadyPacked.getOrDefault(lineId, 0);

            if (orderedQuantity <= 0) {
                throw new RuntimeException(
                        "Sales Order line has an invalid ordered quantity: "
                                + lineId
                );
            }

            if (previouslyPacked < 0) {
                throw new RuntimeException(
                        "Invalid existing packed quantity for order line "
                                + lineId
                );
            }

            long newPackedTotal =
                    (long) previouslyPacked + requestedQuantity;

            if (newPackedTotal > orderedQuantity) {
                throw new RuntimeException(
                        "Package quantity exceeds the remaining quantity for "
                                + "Sales Order line " + lineId
                                + ". Ordered: " + orderedQuantity
                                + ", already packed: " + previouslyPacked
                                + ", requested: " + requestedQuantity
                );
            }
        }
    }

    // =====================================================
    // SET CANONICAL ITEM OBJECTS
    // =====================================================

    private void setCanonicalItems(
            List<PackageItem> packageItems,
            Map<Integer, SalesOrderItem> orderLines) {

        for (PackageItem packageItem : packageItems) {
            int lineId = packageItem.getSalesOrderItemId();

            SalesOrderItem orderLine = orderLines.get(lineId);

            packageItem.setItem(orderLine.getItem());
        }
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

    public List<Package> getPackagesBySalesOrder(int salesOrderId) {
        return packageDAO.findBySalesOrder(salesOrderId);
    }

    // =====================================================
    // GET PACKAGE ITEMS
    // =====================================================

    public List<PackageItem> getPackageItems(Package packageEntity) {
        return packageItemDAO.findByPackage(packageEntity);
    }

    // =====================================================
    // CALCULATE ALREADY PACKAGED QUANTITIES
    // =====================================================

    private Map<Integer, Integer> getAlreadyPackagedQuantities(
            int salesOrderId) {

        Map<Integer, Integer> result = new HashMap<>();

        List<Package> packages =
                packageDAO.findBySalesOrder(salesOrderId);

        for (Package pkg : packages) {

            if (pkg.getStatus() == PackageStatus.CANCELLED) {
                continue;
            }

            List<PackageItem> items =
                    packageItemDAO.findByPackage(pkg);

            for (PackageItem packageItem : items) {

                Integer lineId = packageItem.getSalesOrderItemId();

                if (lineId == null) {
                    continue;
                }

                try {
                    result.merge(
                            lineId,
                            packageItem.getQuantity(),
                            Math::addExact
                    );
                } catch (ArithmeticException e) {
                    throw new RuntimeException(
                            "Existing packed quantity total is too large"
                    );
                }
            }
        }

        return result;
    }
    public List<Package> getAllPackages(String status) {
    return packageDAO.findAll(status);
}
}