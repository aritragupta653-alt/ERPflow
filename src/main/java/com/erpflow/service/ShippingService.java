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

import java.time.LocalDate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShippingService {

    private final PackageDAO packageDAO = new PackageDAO();

    private final PackageItemDAO packageItemDAO = new PackageItemDAO();

    private final SalesOrderDAO salesOrderDAO = new SalesOrderDAO();

    private final SalesOrderItemDAO salesOrderItemDAO =
            new SalesOrderItemDAO();

    private final ShipmentDAO shipmentDAO = new ShipmentDAO();

    private final InventoryService inventoryService = new InventoryService();

    private final CarrierServiceDAO carrierServiceDAO =
            new CarrierServiceDAO();

    private final ShippingRateService shippingRateService =
            new ShippingRateService();

    // =====================================================
    // SHIP SELECTED PACKAGES
    // =====================================================

    public Shipment shipPackages(
        int salesOrderId,
        List<Integer> packageIds,
        int carrierServiceId,
        Shipment shipment) {

    // =====================================================
    // BASIC VALIDATION
    // =====================================================

    if (packageIds == null || packageIds.isEmpty()) {
        throw new RuntimeException(
                "Select at least one package"
        );
    }

    if (carrierServiceId <= 0) {
        throw new RuntimeException(
                "Carrier service is required"
        );
    }

    if (shipment == null) {
        throw new RuntimeException(
                "Shipment details are required"
        );
    }

    if (shipment.getShipmentDate() == null) {
        throw new RuntimeException(
                "Shipment date is required"
        );
    }

    // Manual shipment always starts as CREATED.
    if (shipment.getStatus() == null) {
        shipment.setStatus(
                ShipmentStatus.CREATED
        );
    }
    // =====================================================
// SHIPMENT STATUS
// =====================================================

if (shipment.getStatus() == null) {

    shipment.setStatus(
            ShipmentStatus.CREATED
    );
}

if (shipment.getStatus() != ShipmentStatus.CREATED &&
        shipment.getStatus() != ShipmentStatus.IN_TRANSIT) {

    throw new RuntimeException(
            "Shipment status must be CREATED or IN_TRANSIT"
    );
}


    // =====================================================
    // FIND SALES ORDER
    // =====================================================

    SalesOrder salesOrder =
            salesOrderDAO.findById(salesOrderId);

    if (salesOrder == null) {
        throw new RuntimeException(
                "Sales Order not found"
        );
    }


    // =====================================================
    // DUPLICATE PACKAGE CHECK
    // =====================================================

    Set<Integer> uniquePackageIds =
            new HashSet<>(packageIds);

    if (uniquePackageIds.size() != packageIds.size()) {
        throw new RuntimeException(
                "Duplicate package selected"
        );
    }


    // =====================================================
    // FIND CARRIER SERVICE
    // =====================================================

    CarrierService carrierService =
            carrierServiceDAO.findById(
                    carrierServiceId
            );

    if (carrierService == null) {
        throw new RuntimeException(
                "Carrier service not found"
        );
    }

    if (carrierService.getCarrier() == null) {
        throw new RuntimeException(
                "Carrier not found for selected service"
        );
    }


    // =====================================================
    // VALIDATE ALL PACKAGES
    // =====================================================

    for (Integer packageId : packageIds) {

        if (packageId == null || packageId <= 0) {
            throw new RuntimeException(
                    "Invalid package ID"
            );
        }


        Package pkg =
                packageDAO.findById(packageId);

        if (pkg == null) {
            throw new RuntimeException(
                    "Package not found: " + packageId
            );
        }


        // -------------------------------------------------
        // Package must belong to this Sales Order
        // -------------------------------------------------

        if (pkg.getSalesOrder() == null ||
                pkg.getSalesOrder().getId() != salesOrderId) {

            throw new RuntimeException(
                    "Package " + packageId +
                    " does not belong to Sales Order " +
                    salesOrderId
            );
        }


        // -------------------------------------------------
        // Only PACKED packages can be shipped
        // -------------------------------------------------

        if (pkg.getStatus() != PackageStatus.PACKED) {

            throw new RuntimeException(
                    "Package " + packageId +
                    " is not ready for shipping"
            );
        }


        // -------------------------------------------------
        // Package cannot already be shipped
        // -------------------------------------------------

        if (shipmentDAO.isPackageAlreadyShipped(
                packageId)) {

            throw new RuntimeException(
                    "Package " + packageId +
                    " has already been shipped"
            );
        }


        // -------------------------------------------------
        // Package date <= shipment date
        // -------------------------------------------------

        if (pkg.getPackageDate() != null &&
                pkg.getPackageDate().isAfter(
                        shipment.getShipmentDate()
                )) {

            throw new RuntimeException(
                    "Shipment date cannot be before package date for "
                    + pkg.getPackageNumber()
            );
        }


        // -------------------------------------------------
        // Package must contain items
        // -------------------------------------------------

        List<PackageItem> packageItems =
                packageItemDAO.findByPackage(pkg);

        if (packageItems == null ||
                packageItems.isEmpty()) {

            throw new RuntimeException(
                    "Package " + packageId +
                    " contains no items"
            );
        }
    }


    // =====================================================
    // CALCULATE SHIPPING RATE
    // =====================================================

    ShippingRateService.ShippingRate rate =
            shippingRateService.calculateRate(
                    packageIds,
                    carrierServiceId
            );


    // =====================================================
    // SET SHIPMENT DETAILS
    // =====================================================

    shipment.setCarrier(
            carrierService.getCarrier()
    );

    shipment.setCarrierService(
            carrierService
    );

    shipment.setShippingCharge(
            rate.getTotalCharge().doubleValue()
    );


    // =====================================================
    // ESTIMATED DELIVERY DATE
    // =====================================================

    int estimatedDays =
            carrierService.getEstimatedDays();

    if (shipment.getEstimatedDeliveryDate() == null &&
            estimatedDays > 0) {

        shipment.setEstimatedDeliveryDate(
                shipment.getShipmentDate()
                        .plusDays(estimatedDays)
        );
    }


    // =====================================================
    // DATE VALIDATION
    // =====================================================

    if (shipment.getEstimatedDeliveryDate() != null &&
            shipment.getEstimatedDeliveryDate()
                    .isBefore(
                            shipment.getShipmentDate()
                    )) {

        throw new RuntimeException(
                "Estimated delivery date cannot be before shipment date"
        );
    }


    // =====================================================
    // ACTUAL DELIVERY DATE
    // =====================================================

    // Manual creation must NOT mark the shipment
    // as delivered.

    shipment.setActualDeliveryDate(null);


    // =====================================================
    // CREATE SHIPMENT
    // =====================================================

    shipmentDAO.save(shipment);


    // =====================================================
    // GENERATE SHIPMENT NUMBER
    // =====================================================

    shipment.setShipmentNumber(
            String.format(
                    "SH-%06d",
                    shipment.getId()
            )
    );

    shipmentDAO.update(shipment);


    // =====================================================
    // SHIP INVENTORY + MARK PACKAGES
    // =====================================================

    for (Integer packageId : packageIds) {

        Package pkg =
                packageDAO.findById(packageId);

        List<PackageItem> packageItems =
                packageItemDAO.findByPackage(pkg);


        for (PackageItem packageItem :
                packageItems) {

            inventoryService.shipReservedStock(
                    packageItem.getItem(),
                    packageItem.getQuantity()
            );
        }


        // Package becomes SHIPPED
        pkg.setStatus(
                PackageStatus.SHIPPED
        );

        packageDAO.update(pkg);
    }


    // =====================================================
    // LINK PACKAGES TO SHIPMENT
    // =====================================================

    for (Integer packageId : packageIds) {

        shipmentDAO.addPackage(
                shipment.getId(),
                packageId
        );
    }


    // =====================================================
    // UPDATE SALES ORDER STATUS
    // =====================================================

    updateSalesOrderStatus(
            salesOrder
    );


    return shipment;
}

    // =====================================================
    // UPDATE SALES ORDER STATUS
    // =====================================================

    private void updateSalesOrderStatus(SalesOrder salesOrder) {

        List<SalesOrderItem> orderItems =
                salesOrderItemDAO.findBySalesOrder(salesOrder);

        boolean allShipped = true;
        boolean anyShipped = false;

        for (SalesOrderItem orderItem : orderItems) {

            int orderedQuantity = orderItem.getQuantity();

            int shippedQuantity = getShippedQuantity(
                    salesOrder.getId(),
                    orderItem.getItem().getId()
            );

            if (shippedQuantity > 0) {
                anyShipped = true;
            }

            if (shippedQuantity < orderedQuantity) {
                allShipped = false;
            }
        }

        if (allShipped) {
            salesOrder.setStatus(SalesOrderStatus.SHIPPED);
        } else if (anyShipped) {
            salesOrder.setStatus(SalesOrderStatus.PARTIALLY_SHIPPED);
        }

        salesOrderDAO.update(salesOrder);
    }

    // =====================================================
    // GET SHIPPED QUANTITY
    // =====================================================

    private int getShippedQuantity(int salesOrderId, int itemId) {

        List<Package> packages =
                packageDAO.findBySalesOrder(salesOrderId);

        int total = 0;

        for (Package pkg : packages) {

            if (pkg.getStatus() != PackageStatus.SHIPPED) {
                continue;
            }

            List<PackageItem> items =
                    packageItemDAO.findByPackage(pkg);

            for (PackageItem packageItem : items) {

                if (packageItem.getItem() != null &&
                        packageItem.getItem().getId() == itemId) {

                    total += packageItem.getQuantity();
                }
            }
        }

        return total;
    }
}