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
import com.erpflow.model.enums.PackageStatus;
import com.erpflow.model.enums.SalesOrderStatus;
import com.erpflow.util.TransactionManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackageService {

    private final PackageDAO packageDAO = new PackageDAO();
    private final PackageItemDAO packageItemDAO = new PackageItemDAO();
    private final SalesOrderDAO salesOrderDAO = new SalesOrderDAO();
    private final SalesOrderItemDAO salesOrderItemDAO = new SalesOrderItemDAO();

    /**
     * Creates a package and updates sales_order_items.packed_quantity in one
     * transaction. The request only needs IDs and package data; the service
     * reloads authoritative order/item state inside the transaction.
     */
    public Package createPackage(
            int salesOrderId,
            List<PackageItem> packageItems,
            double weight,
            double length,
            double width,
            double height,
            LocalDate packageDate) {

        validateDimensions(weight, length, width, height);
        validatePackageDate(packageDate);

        return TransactionManager.execute(() -> {
            SalesOrder salesOrder = salesOrderDAO.findById(salesOrderId);
            validateSalesOrderForPacking(salesOrder);

            List<SalesOrderItem> orderLines =
                    salesOrderItemDAO.findForUpdateBySalesOrder(salesOrderId);

            Map<Integer, SalesOrderItem> orderLineMap = indexOrderLines(orderLines);
            Map<Integer, Integer> requested =
                    validateAndAggregateItems(packageItems, orderLineMap);

            Map<Integer, Integer> packedDeltas = new HashMap<>();
            for (Map.Entry<Integer, Integer> entry : requested.entrySet()) {
                int lineId = entry.getKey();
                int quantity = entry.getValue();
                SalesOrderItem line = orderLineMap.get(lineId);

                validatePackableLine(line);

                if ((long) line.getPackedQuantity() + quantity > line.getQuantity()) {
                    throw new IllegalArgumentException(
                            "Package quantity exceeds remaining quantity for Sales Order line "
                                    + lineId + ". Ordered: " + line.getQuantity()
                                    + ", already packed: " + line.getPackedQuantity()
                                    + ", requested: " + quantity);
                }

                packedDeltas.put(lineId, quantity);
            }

            validatePackageDateAgainstSalesOrder(packageDate, salesOrder);

            setCanonicalItems(packageItems, orderLineMap);

            Package packageEntity = new Package();
            packageEntity.setSalesOrder(salesOrder);
            packageEntity.setStatus(PackageStatus.PACKING );
            packageEntity.setPackageDate(packageDate);
            packageEntity.setWeight(weight);
            packageEntity.setLength(length);
            packageEntity.setWidth(width);
            packageEntity.setHeight(height);

            packageDAO.save(packageEntity);
            packageEntity.setPackageNumber(
                    String.format("PKG-%06d", packageEntity.getId()));
            packageDAO.update(packageEntity);

            for (PackageItem packageItem : packageItems) {
                packageItem.setPackageEntity(packageEntity);
            }
            packageItemDAO.saveBatch(packageItems);

            salesOrderItemDAO.adjustPackedQuantities(packedDeltas);

            packageEntity.setStatus(PackageStatus.PACKED);
            packageDAO.update(packageEntity);

            updateSalesOrderPackingStatus(salesOrder, orderLines);
            return packageEntity;
        });
    }

    /**
     * Replaces the contents of an unshipped PACKED package. The package's old
     * packed quantities are removed and the new quantities are applied inside
     * the same transaction.
     */
    public Package editPackage(
            int packageId,
            List<PackageItem> updatedItems,
            double weight,
            double length,
            double width,
            double height) {

        validateDimensions(weight, length, width, height);

        return TransactionManager.execute(() -> {
            Package existingPackage = packageDAO.findById(packageId);
            if (existingPackage == null) {
                throw new IllegalArgumentException("Package not found");
            }

            if (existingPackage.getStatus() != PackageStatus.PACKED) {
                throw new IllegalStateException(
                        "Only PACKED packages that have not been shipped can be edited");
            }

            SalesOrder salesOrder = existingPackage.getSalesOrder();
            if (salesOrder == null) {
                throw new IllegalStateException("Package is not linked to a Sales Order");
            }

            validateSalesOrderForPacking(salesOrder);

            List<SalesOrderItem> orderLines =
                    salesOrderItemDAO.findForUpdateBySalesOrder(salesOrder.getId());
            Map<Integer, SalesOrderItem> orderLineMap = indexOrderLines(orderLines);

            List<PackageItem> currentItems =
                    packageItemDAO.findByPackageId(packageId);

            Map<Integer, Integer> oldQuantities = aggregatePackageItems(currentItems);
            Map<Integer, Integer> requested =
                    validateAndAggregateItems(updatedItems, orderLineMap);

            Map<Integer, Integer> deltas = new HashMap<>();
            for (Integer lineId : unionKeys(oldQuantities, requested)) {
                int oldQuantity = oldQuantities.getOrDefault(lineId, 0);
                int newQuantity = requested.getOrDefault(lineId, 0);
                int delta = newQuantity - oldQuantity;

                if (delta != 0) {
                    SalesOrderItem line = orderLineMap.get(lineId);
                    validatePackableLine(line);

                    long newTotal = (long) line.getPackedQuantity() + delta;
                    if (newTotal < line.getShippedQuantity() || newTotal > line.getQuantity()) {
                        throw new IllegalArgumentException(
                                "Package edit would exceed fulfillment quantity for Sales Order line "
                                        + lineId);
                    }
                    deltas.put(lineId, delta);
                }
            }

            setCanonicalItems(updatedItems, orderLineMap);

            existingPackage.setWeight(weight);
            existingPackage.setLength(length);
            existingPackage.setWidth(width);
            existingPackage.setHeight(height);

            packageDAO.update(existingPackage);
            packageItemDAO.deleteByPackageId(packageId);

            for (PackageItem item : updatedItems) {
                item.setPackageEntity(existingPackage);
            }
            packageItemDAO.saveBatch(updatedItems);

            salesOrderItemDAO.adjustPackedQuantities(deltas);
            updateSalesOrderPackingStatus(salesOrder, orderLines);

            return existingPackage;
        });
    }

    public List<Package> getAllPackages() {
        return packageDAO.findAll();
    }

    public List<Package> getAllPackages(String status) {
        return packageDAO.findAll(status);
    }

    public Package getPackageById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid package ID");
        }
        return packageDAO.findById(id);
    }

    public List<Package> getPackagesBySalesOrder(int salesOrderId) {
        if (salesOrderId <= 0) {
            throw new IllegalArgumentException("Invalid Sales Order ID");
        }
        return packageDAO.findBySalesOrder(salesOrderId);
    }

    public List<PackageItem> getPackageItems(Package packageEntity) {
        if (packageEntity == null || packageEntity.getId() <= 0) {
            throw new IllegalArgumentException("Valid package is required");
        }
        return packageItemDAO.findByPackageId(packageEntity.getId());
    }

    public List<PackageItem> getPackageItemsByPackageIds(List<Integer> packageIds) {
        return packageItemDAO.findByPackageIds(packageIds);
    }

    private void validateSalesOrderForPacking(SalesOrder salesOrder) {
        if (salesOrder == null) {
            throw new IllegalArgumentException("Sales Order not found");
        }

        SalesOrderStatus status = salesOrder.getStatus();
        if (status == SalesOrderStatus.SHIPPED
                || status == SalesOrderStatus.COMPLETED
                || status == SalesOrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cannot create or edit a package for Sales Order status " + status);
        }
    }

    private void validatePackageDate(LocalDate packageDate) {
        if (packageDate == null) {
            throw new IllegalArgumentException("Package date is required");
        }
    }

    private void validatePackageDateAgainstSalesOrder(
            LocalDate packageDate,
            SalesOrder salesOrder) {

        if (salesOrder.getOrderDate() != null
                && packageDate.isBefore(salesOrder.getOrderDate().toLocalDate())) {
            throw new IllegalArgumentException(
                    "Package date cannot be before Sales Order date");
        }
    }

    private void validateDimensions(
            double weight,
            double length,
            double width,
            double height) {

        if (!Double.isFinite(weight) || !Double.isFinite(length)
                || !Double.isFinite(width) || !Double.isFinite(height)) {
            throw new IllegalArgumentException(
                    "Package weight and dimensions must be valid numbers");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Package weight must be greater than zero");
        }
        if (length <= 0 || width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "Package dimensions must be greater than zero");
        }
    }

    private Map<Integer, SalesOrderItem> indexOrderLines(List<SalesOrderItem> orderLines) {
        if (orderLines == null || orderLines.isEmpty()) {
            throw new IllegalStateException("Sales Order contains no items");
        }

        Map<Integer, SalesOrderItem> result = new HashMap<>();
        for (SalesOrderItem line : orderLines) {
            if (line == null || line.getId() <= 0 || line.getItem() == null) {
                throw new IllegalStateException("Sales Order contains an invalid item line");
            }
            result.put(line.getId(), line);
        }
        return result;
    }

    private Map<Integer, Integer> validateAndAggregateItems(
            List<PackageItem> packageItems,
            Map<Integer, SalesOrderItem> orderLines) {

        if (packageItems == null || packageItems.isEmpty()) {
            throw new IllegalArgumentException("Package must contain at least one item");
        }

        Map<Integer, Integer> requested = new HashMap<>();

        for (PackageItem packageItem : packageItems) {
            if (packageItem == null || packageItem.getItem() == null) {
                throw new IllegalArgumentException("Package item is missing");
            }

            Integer lineId = packageItem.getSalesOrderItemId();
            if (lineId == null || lineId <= 0) {
                throw new IllegalArgumentException(
                        "Sales order line ID is required for every package item");
            }

            if (packageItem.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                        "Package quantity must be greater than zero");
            }

            SalesOrderItem line = orderLines.get(lineId);
            if (line == null) {
                throw new IllegalArgumentException(
                        "Sales order line does not belong to this Sales Order: " + lineId);
            }

            if (line.getItem().getId() != packageItem.getItem().getId()) {
                throw new IllegalArgumentException(
                        "Item does not match the selected Sales Order line");
            }

            validatePackableLine(line);

            requested.merge(lineId, packageItem.getQuantity(), Math::addExact);
        }

        return requested;
    }

    private void validatePackableLine(SalesOrderItem line) {
        if (line == null || line.getItem() == null) {
            throw new IllegalArgumentException("Invalid Sales Order line");
        }
        if (line.getQuantity() <= 0) {
            throw new IllegalArgumentException(
                    "Sales Order line has an invalid ordered quantity: " + line.getId());
        }
        if (line.getPackedQuantity() < 0 || line.getShippedQuantity() < 0) {
            throw new IllegalStateException(
                    "Sales Order line contains invalid fulfillment quantities: " + line.getId());
        }
        if (line.getShippedQuantity() > line.getPackedQuantity()) {
            throw new IllegalStateException(
                    "Shipped quantity cannot exceed packed quantity for line " + line.getId());
        }
        if (!"GOODS".equalsIgnoreCase(line.getItem().getItemType())
                || !line.getItem().isTrackInventory()) {
            throw new IllegalArgumentException(
                    "Only inventory-tracked goods can be added to packages");
        }
    }

    private void setCanonicalItems(
            List<PackageItem> packageItems,
            Map<Integer, SalesOrderItem> orderLines) {

        for (PackageItem packageItem : packageItems) {
            packageItem.setItem(
                    orderLines.get(packageItem.getSalesOrderItemId()).getItem());
        }
    }

    private Map<Integer, Integer> aggregatePackageItems(List<PackageItem> items) {
        Map<Integer, Integer> result = new HashMap<>();
        for (PackageItem item : items) {
            if (item.getSalesOrderItemId() == null || item.getSalesOrderItemId() <= 0) {
                throw new IllegalStateException(
                        "Package contains an item without a Sales Order line reference");
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalStateException("Package contains an invalid quantity");
            }
            result.merge(item.getSalesOrderItemId(), item.getQuantity(), Math::addExact);
        }
        return result;
    }

    private List<Integer> unionKeys(
            Map<Integer, Integer> first,
            Map<Integer, Integer> second) {

        java.util.Set<Integer> keys = new java.util.HashSet<>(first.keySet());
        keys.addAll(second.keySet());
        return new ArrayList<>(keys);
    }

    private void updateSalesOrderPackingStatus(
            SalesOrder salesOrder,
            List<SalesOrderItem> lockedLines) {
        // SalesOrderStatus intentionally remains unchanged while packing.
        // Fulfillment state is represented by packed_quantity/shipped_quantity
        // on sales_order_items. ShipmentService/ShippingService owns the
        // PARTIALLY_SHIPPED/SHIPPED status transitions.
    }
}
