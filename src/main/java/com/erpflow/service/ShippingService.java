package com.erpflow.service;

import com.erpflow.dao.CarrierServiceDAO;
import com.erpflow.dao.PackageDAO;
import com.erpflow.dao.PackageItemDAO;
import com.erpflow.dao.SalesOrderDAO;
import com.erpflow.dao.SalesOrderItemDAO;
import com.erpflow.dao.ShipmentDAO;
import com.erpflow.model.CarrierService;
import com.erpflow.model.Package;
import com.erpflow.model.PackageItem;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;
import com.erpflow.model.Shipment;
import com.erpflow.model.enums.PackageStatus;
import com.erpflow.model.enums.SalesOrderStatus;
import com.erpflow.model.enums.ShipmentStatus;
import com.erpflow.util.TransactionManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShippingService {

    private static final String DEFAULT_DISPATCH_ADDRESS =
            "ERPFlow Warehouse, Chennai, Tamil Nadu, India";

    private final PackageDAO packageDAO = new PackageDAO();
    private final PackageItemDAO packageItemDAO = new PackageItemDAO();
    private final SalesOrderDAO salesOrderDAO = new SalesOrderDAO();
    private final SalesOrderItemDAO salesOrderItemDAO = new SalesOrderItemDAO();
    private final ShipmentDAO shipmentDAO = new ShipmentDAO();
    private final CarrierServiceDAO carrierServiceDAO = new CarrierServiceDAO();
    private final InventoryService inventoryService = new InventoryService();
    private final ShippingRateService shippingRateService = new ShippingRateService();

    /**
     * Ships selected packages as one atomic fulfillment operation.
     *
     * Query pattern inside the transaction:
     * 1) Sales order
     * 2) Locked sales-order lines
     * 3) Carrier service
     * 4) All packages in one IN query
     * 5) All package items in one IN query
     * followed by batched writes.
     */
    public Shipment shipPackages(
            int salesOrderId,
            List<Integer> packageIds,
            int carrierServiceId,
            Shipment shipment) {

        validateRequest(salesOrderId, packageIds, carrierServiceId, shipment);

        return TransactionManager.execute(() -> {
            SalesOrder salesOrder = salesOrderDAO.findById(salesOrderId);
            if (salesOrder == null) {
                throw new IllegalArgumentException("Sales Order not found");
            }

            if (salesOrder.getStatus() == SalesOrderStatus.CANCELLED
                    || salesOrder.getStatus() == SalesOrderStatus.COMPLETED
                    || salesOrder.getStatus() == SalesOrderStatus.SHIPPED) {
                throw new IllegalStateException(
                        "Cannot ship a Sales Order with status " + salesOrder.getStatus());
            }

            List<SalesOrderItem> orderLines =
                    salesOrderItemDAO.findForUpdateBySalesOrder(salesOrderId);
            Map<Integer, SalesOrderItem> orderLineMap = new HashMap<>();
            for (SalesOrderItem line : orderLines) {
                orderLineMap.put(line.getId(), line);
            }

            CarrierService carrierService = carrierServiceDAO.findById(carrierServiceId);
            if (carrierService == null) {
                throw new IllegalArgumentException("Carrier service not found");
            }
            if (carrierService.getCarrier() == null) {
                throw new IllegalStateException("Carrier not found for selected service");
            }

            List<Package> packages = packageDAO.findByIds(packageIds);
            if (packages.size() != packageIds.size()) {
                throw new IllegalArgumentException("One or more selected packages were not found");
            }

            Map<Integer, Package> packageById = new HashMap<>();
            for (Package pkg : packages) {
                packageById.put(pkg.getId(), pkg);
            }

            Set<Integer> alreadyShipped =
                    shipmentDAO.findAlreadyShippedPackageIds(packageIds);
            if (!alreadyShipped.isEmpty()) {
                throw new IllegalStateException(
                        "Package " + alreadyShipped.iterator().next()
                                + " has already been shipped");
            }

            List<PackageItem> packageItems =
                    packageItemDAO.findByPackageIds(packageIds);

            if (packageItems.isEmpty()) {
                throw new IllegalArgumentException("Selected packages contain no items");
            }

            Map<Integer, List<PackageItem>> itemsByPackage = new HashMap<>();
            for (PackageItem packageItem : packageItems) {
                int packageId = packageItem.getPackageEntity().getId();
                itemsByPackage
                        .computeIfAbsent(packageId, key -> new ArrayList<>())
                        .add(packageItem);
            }

            Map<Integer, Integer> shippedDeltas = new HashMap<>();

            for (Integer packageId : packageIds) {
                Package pkg = packageById.get(packageId);

                if (pkg.getSalesOrder() == null
                        || pkg.getSalesOrder().getId() != salesOrderId) {
                    throw new IllegalArgumentException(
                            "Package " + packageId
                                    + " does not belong to Sales Order " + salesOrderId);
                }

                if (pkg.getStatus() != PackageStatus.PACKED) {
                    throw new IllegalStateException(
                            "Package " + packageId + " is not ready for shipping");
                }

                if (pkg.getPackageDate() != null
                        && pkg.getPackageDate().isAfter(shipment.getShipmentDate())) {
                    throw new IllegalArgumentException(
                            "Shipment date cannot be before package date for "
                                    + pkg.getPackageNumber());
                }

                List<PackageItem> items = itemsByPackage.get(packageId);
                if (items == null || items.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Package " + packageId + " contains no items");
                }

                for (PackageItem packageItem : items) {
                    Integer lineId = packageItem.getSalesOrderItemId();
                    if (lineId == null || lineId <= 0) {
                        throw new IllegalStateException(
                                "Package " + pkg.getPackageNumber()
                                        + " contains an item without a Sales Order line reference");
                    }

                    SalesOrderItem line = orderLineMap.get(lineId);
                    if (line == null) {
                        throw new IllegalStateException(
                                "Package item references an invalid Sales Order line: " + lineId);
                    }

                    if (line.getItem() == null
                            || line.getItem().getId() != packageItem.getItem().getId()) {
                        throw new IllegalStateException(
                                "Package item does not match its Sales Order line");
                    }

                    if (packageItem.getQuantity() <= 0) {
                        throw new IllegalStateException("Package contains an invalid quantity");
                    }

                    shippedDeltas.merge(
                            lineId,
                            packageItem.getQuantity(),
                            Math::addExact);
                }
            }

            for (Map.Entry<Integer, Integer> entry : shippedDeltas.entrySet()) {
                SalesOrderItem line = orderLineMap.get(entry.getKey());
                int newShipped = line.getShippedQuantity() + entry.getValue();

                if (newShipped > line.getPackedQuantity()) {
                    throw new IllegalStateException(
                            "Cannot ship more than packed quantity for Sales Order line "
                                    + line.getId()
                                    + ". Packed: " + line.getPackedQuantity()
                                    + ", already shipped: " + line.getShippedQuantity()
                                    + ", requested: " + entry.getValue());
                }
            }

            ShippingRateService.ShippingRate rate =
                    shippingRateService.calculateRateForPackages(packages, carrierService);

            if (shipment.getShippingMethod() == null
                    || shipment.getShippingMethod().isBlank()) {
                shipment.setShippingMethod(carrierService.getName());
            }

            shipment.setCarrier(carrierService.getCarrier());
            shipment.setCarrierService(carrierService);
            shipment.setShippingCharge(rate.getTotalCharge().doubleValue());

            if (shipment.getDispatchAddress() == null
                    || shipment.getDispatchAddress().isBlank()) {
                shipment.setDispatchAddress(DEFAULT_DISPATCH_ADDRESS);
            }

            if (shipment.getDestinationAddress() == null
                    || shipment.getDestinationAddress().isBlank()) {
                if (salesOrder.getCustomer() == null
                        || salesOrder.getCustomer().getAddress() == null
                        || salesOrder.getCustomer().getAddress().isBlank()) {
                    throw new IllegalArgumentException(
                            "The customer has no shipping address. Update the customer details before shipping.");
                }
                shipment.setDestinationAddress(salesOrder.getCustomer().getAddress());
            }

            if (shipment.getEstimatedDeliveryDate() == null
                    && carrierService.getEstimatedDays() > 0) {
                shipment.setEstimatedDeliveryDate(
                        shipment.getShipmentDate().plusDays(carrierService.getEstimatedDays()));
            }

            if (shipment.getEstimatedDeliveryDate() != null
                    && shipment.getEstimatedDeliveryDate().isBefore(shipment.getShipmentDate())) {
                throw new IllegalArgumentException(
                        "Estimated delivery date cannot be before shipment date");
            }

            shipment.setActualDeliveryDate(null);
            shipmentDAO.save(shipment);
            shipment.setShipmentNumber(String.format("SH-%06d", shipment.getId()));
            shipmentDAO.update(shipment);

            // One batch for the entire shipment.
            shipmentDAO.addPackages(shipment.getId(), packageIds);
            salesOrderItemDAO.incrementShippedQuantities(shippedDeltas);

            // Shipping consumes physical stock and releases the reservation.
            // This is part of the same transaction as shipment creation.
            for (Map.Entry<Integer, Integer> entry : shippedDeltas.entrySet()) {
                SalesOrderItem line = orderLineMap.get(entry.getKey());
                if (line == null || line.getItem() == null) {
                    throw new IllegalStateException(
                            "Could not resolve the item for Sales Order line " + entry.getKey());
                }

                if ("GOODS".equalsIgnoreCase(line.getItem().getItemType())
                        && line.getItem().isTrackInventory()) {
                    inventoryService.shipReservedStock(
                            line.getItem(),
                            entry.getValue());
                }
            }

            packageDAO.updateStatusBatch(packageIds, PackageStatus.SHIPPED);

            // Update the in-memory locked lines so no second SELECT is needed.
            for (Map.Entry<Integer, Integer> entry : shippedDeltas.entrySet()) {
                SalesOrderItem line = orderLineMap.get(entry.getKey());
                line.setShippedQuantity(
                        line.getShippedQuantity() + entry.getValue());
            }

            boolean allShipped = !orderLines.isEmpty();
            boolean anyShipped = false;
            for (SalesOrderItem line : orderLines) {
                if (line.getItem() == null
                        || !"GOODS".equalsIgnoreCase(line.getItem().getItemType())
                        || !line.getItem().isTrackInventory()) {
                    continue;
                }

                if (line.getShippedQuantity() > 0) {
                    anyShipped = true;
                }
                if (line.getShippedQuantity() < line.getQuantity()) {
                    allShipped = false;
                }
            }

            if (allShipped) {
                salesOrder.setStatus(SalesOrderStatus.SHIPPED);
                salesOrderDAO.update(salesOrder);
            } else if (anyShipped) {
                salesOrder.setStatus(SalesOrderStatus.PARTIALLY_SHIPPED);
                salesOrderDAO.update(salesOrder);
            }

            return shipment;
        });
    }

    private void validateRequest(
            int salesOrderId,
            List<Integer> packageIds,
            int carrierServiceId,
            Shipment shipment) {

        if (salesOrderId <= 0) {
            throw new IllegalArgumentException("A valid Sales Order ID is required");
        }
        if (packageIds == null || packageIds.isEmpty()) {
            throw new IllegalArgumentException("Select at least one package");
        }
        if (carrierServiceId <= 0) {
            throw new IllegalArgumentException("Carrier service is required");
        }
        if (shipment == null) {
            throw new IllegalArgumentException("Shipment details are required");
        }
        if (shipment.getShipmentDate() == null) {
            throw new IllegalArgumentException("Shipment date is required");
        }

        if (shipment.getStatus() == null) {
            shipment.setStatus(ShipmentStatus.CREATED);
        }

        if (shipment.getStatus() != ShipmentStatus.CREATED
                && shipment.getStatus() != ShipmentStatus.IN_TRANSIT) {
            throw new IllegalArgumentException(
                    "Shipment status must be CREATED or IN_TRANSIT");
        }

        Set<Integer> unique = new HashSet<>();
        for (Integer packageId : packageIds) {
            if (packageId == null || packageId <= 0 || !unique.add(packageId)) {
                throw new IllegalArgumentException("Package IDs must be positive and unique");
            }
        }
    }
}
