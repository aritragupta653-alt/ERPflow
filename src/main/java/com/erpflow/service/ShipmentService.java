package com.erpflow.service;

import com.erpflow.dao.PackageDAO;
import com.erpflow.dao.ShipmentDAO;
import com.erpflow.model.Package;
import com.erpflow.model.Shipment;
import com.erpflow.model.enums.ShipmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShipmentService {

    private final ShipmentDAO shipmentDAO =
            new ShipmentDAO();

    private final PackageDAO packageDAO =
            new PackageDAO();


    // =====================================================
    // CREATE SHIPMENT
    // =====================================================

    public void createShipment(Shipment shipment) {

        if (shipment == null) {
            throw new IllegalArgumentException(
                    "Shipment cannot be null"
            );
        }

        if (shipment.getShipmentDate() == null) {
            throw new IllegalArgumentException(
                    "Shipment date is required"
            );
        }

        validateShipmentDates(shipment);

        if (shipment.getStatus() == null) {

            shipment.setStatus(
                    ShipmentStatus.CREATED
            );
        }

        validateCreationStatus(
                shipment.getStatus()
        );

        if (shipment.getShippingMethod() == null ||
                shipment.getShippingMethod().isBlank()) {

            shipment.setShippingMethod("MANUAL");
        }

        if (shipment.getShippingCharge() < 0) {

            throw new IllegalArgumentException(
                    "Shipping charge cannot be negative"
            );
        }

        shipmentDAO.save(shipment);

        if (shipment.getShipmentNumber() == null ||
                shipment.getShipmentNumber().isBlank()) {

            shipment.setShipmentNumber(
                    String.format(
                            "SH-%06d",
                            shipment.getId()
                    )
            );

            shipmentDAO.update(shipment);
        }
    }


    // =====================================================
    // GET ALL
    // =====================================================

    public List<Shipment> getAllShipments() {

        return shipmentDAO.findAll();
    }


    // =====================================================
    // GET BY ID
    // =====================================================

    public Shipment getShipmentById(int id) {

        if (id <= 0) {

            throw new IllegalArgumentException(
                    "Invalid shipment ID"
            );
        }

        return shipmentDAO.findById(id);
    }


    // =====================================================
    // UPDATE SHIPMENT
    // =====================================================

    public void updateShipment(
            Shipment updatedShipment) {
        

        if (updatedShipment == null ||
                updatedShipment.getId() <= 0) {

            throw new IllegalArgumentException(
                    "Invalid shipment"
            );
        }

        Shipment existing =
                shipmentDAO.findById(
                        updatedShipment.getId()
                );

        if (existing == null) {

            throw new IllegalArgumentException(
                    "Shipment not found"
            );
        }

        // ---------------------------------------------
        // Delivered shipment is completely immutable
        // ---------------------------------------------

        if (existing.getStatus() ==
                ShipmentStatus.DELIVERED) {

            throw new IllegalStateException(
                    "Delivered shipments cannot be edited."
            );
        }

        // ---------------------------------------------
        // Shipping charge
        // ---------------------------------------------

        if (updatedShipment.getShippingCharge() < 0) {

            throw new IllegalArgumentException(
                    "Shipping charge cannot be negative"
            );
        }

        // ---------------------------------------------
        // Preserve shipment number
        // ---------------------------------------------

        updatedShipment.setShipmentNumber(
                existing.getShipmentNumber()
        );

        // ---------------------------------------------
        // Preserve omitted values
        // ---------------------------------------------

        if (updatedShipment.getShipmentDate() == null) {

            updatedShipment.setShipmentDate(
                    existing.getShipmentDate()
            );
        }

        if (updatedShipment.getStatus() == null) {

            updatedShipment.setStatus(
                    existing.getStatus()
            );
        }

        if (updatedShipment.getShippingMethod() == null ||
                updatedShipment.getShippingMethod().isBlank()) {

            updatedShipment.setShippingMethod(
                    existing.getShippingMethod()
            );
        }

        if (updatedShipment.getCarrier() == null) {

            updatedShipment.setCarrier(
                    existing.getCarrier()
            );
        }

        if (updatedShipment.getCarrierService() == null) {

            updatedShipment.setCarrierService(
                    existing.getCarrierService()
            );
        }

        if (updatedShipment.getTrackingNumber() == null) {

            updatedShipment.setTrackingNumber(
                    existing.getTrackingNumber()
            );
        }

        if (updatedShipment.getTrackingUrl() == null) {

            updatedShipment.setTrackingUrl(
                    existing.getTrackingUrl()
            );
        }

        if (updatedShipment.getDispatchAddress() == null) {

            updatedShipment.setDispatchAddress(
                    existing.getDispatchAddress()
            );
        }

        if (updatedShipment.getDestinationAddress() == null) {

            updatedShipment.setDestinationAddress(
                    existing.getDestinationAddress()
            );
        }

        if (updatedShipment.getEstimatedDeliveryDate() == null) {

            updatedShipment.setEstimatedDeliveryDate(
                    existing.getEstimatedDeliveryDate()
            );
        }

        if (updatedShipment.getActualDeliveryDate() == null) {

            updatedShipment.setActualDeliveryDate(
                    existing.getActualDeliveryDate()
            );
        }

        if (updatedShipment.getNotes() == null) {

            updatedShipment.setNotes(
                    existing.getNotes()
            );
        }

        // Package assignments are managed separately.
        updatedShipment.setPackages(
                existing.getPackages()
        );
        validateShipmentAgainstSalesOrderDate(
        updatedShipment
);

        // ---------------------------------------------
        // Validate final object
        // ---------------------------------------------

        validateShipmentDates(
                updatedShipment
        );

        validateStatusTransition(
                existing.getStatus(),
                updatedShipment.getStatus()
        );

        shipmentDAO.update(
                updatedShipment
        );
    }


    // =====================================================
    // MARK AS DELIVERED
    // =====================================================

    public void markShipmentDelivered(
            int shipmentId,
            LocalDate actualDeliveryDate) {

        if (shipmentId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid shipment ID"
            );
        }

        if (actualDeliveryDate == null) {

            throw new IllegalArgumentException(
                    "Actual delivery date is required"
            );
        }

        Shipment shipment =
                shipmentDAO.findById(shipmentId);

        if (shipment == null) {

            throw new IllegalArgumentException(
                    "Shipment not found"
            );
        }

        if (shipment.getStatus() ==
                ShipmentStatus.DELIVERED) {

            throw new IllegalStateException(
                    "Shipment is already marked as delivered"
            );
        }

        if (shipment.getShipmentDate() != null &&
                actualDeliveryDate.isBefore(
                        shipment.getShipmentDate()
                )) {

            throw new IllegalArgumentException(
                    "Actual delivery date cannot be before shipment date"
            );
        }

        if (shipment.getEstimatedDeliveryDate() != null &&
                actualDeliveryDate.isBefore(
                        shipment.getShipmentDate()
                )) {

            throw new IllegalArgumentException(
                    "Actual delivery date cannot be before shipment date"
            );
        }

        shipment.setStatus(
                ShipmentStatus.DELIVERED
        );

        shipment.setActualDeliveryDate(
                actualDeliveryDate
        );

        shipmentDAO.update(shipment);
    }


    // =====================================================
    // ADD PACKAGE
    // =====================================================

    public void addPackageToShipment(
            int shipmentId,
            int packageId) {

        if (shipmentId <= 0 ||
                packageId <= 0) {

            throw new IllegalArgumentException(
                    "Shipment ID and package ID must be valid"
            );
        }

        Shipment shipment =
                shipmentDAO.findById(shipmentId);

        if (shipment == null) {

            throw new IllegalArgumentException(
                    "Shipment not found"
            );
        }

        if (shipment.getStatus() ==
                ShipmentStatus.DELIVERED) {

            throw new IllegalStateException(
                    "Packages cannot be changed after delivery."
            );
        }

        Package pkg =
                packageDAO.findById(packageId);

        if (pkg == null) {

            throw new IllegalArgumentException(
                    "Package not found"
            );
        }

        if (pkg.getSalesOrder() == null) {

            throw new IllegalArgumentException(
                    "Package is not linked to a Sales Order"
            );
        }

        if (shipment.getPackages() != null &&
                shipment.getPackages()
                        .stream()
                        .anyMatch(
                                p -> p.getId() == packageId
                        )) {

            throw new IllegalArgumentException(
                    "Package is already assigned to this shipment"
            );
        }

        if (shipmentDAO.isPackageAlreadyShipped(
                packageId)) {

            throw new IllegalArgumentException(
                    "Package is already assigned to another shipment"
            );
        }

        if (!"PACKED".equalsIgnoreCase(
                String.valueOf(pkg.getStatus())
        )) {

            throw new IllegalArgumentException(
                    "Only PACKED packages can be shipped"
            );
        }

        validatePackageDateAgainstShipment(
                pkg,
                shipment.getShipmentDate()
        );

        shipmentDAO.addPackage(
                shipmentId,
                packageId
        );
    }


    // =====================================================
    // REPLACE PACKAGE ASSIGNMENTS
    // =====================================================

    public void updateShipmentPackages(
            int shipmentId,
            List<Integer> packageIds) {

        if (shipmentId <= 0) {

            throw new IllegalArgumentException(
                    "Invalid shipment ID"
            );
        }

        if (packageIds == null) {

            throw new IllegalArgumentException(
                    "Package IDs are required"
            );
        }

        Shipment shipment =
                shipmentDAO.findById(shipmentId);

        if (shipment == null) {

            throw new IllegalArgumentException(
                    "Shipment not found"
            );
        }

        if (shipment.getStatus() ==
                ShipmentStatus.DELIVERED) {

            throw new IllegalStateException(
                    "Packages cannot be changed after delivery."
            );
        }

        Set<Integer> uniqueIds =
                new HashSet<>(packageIds);

        if (uniqueIds.size() != packageIds.size()) {

            throw new IllegalArgumentException(
                    "Duplicate package selected"
            );
        }

        for (Integer packageId : packageIds) {

            if (packageId == null ||
                    packageId <= 0) {

                throw new IllegalArgumentException(
                        "Invalid package ID"
                );
            }

            Package pkg =
                    packageDAO.findById(packageId);

            if (pkg == null) {

                throw new IllegalArgumentException(
                        "Package not found: " +
                                packageId
                );
            }

            if (pkg.getSalesOrder() == null) {

                throw new IllegalArgumentException(
                        "Package " + packageId +
                                " is not linked to a Sales Order"
                );
            }

            // Only PACKED packages can be assigned.
            if (!"PACKED".equalsIgnoreCase(
                    String.valueOf(pkg.getStatus())
            )) {

                throw new IllegalArgumentException(
                        "Package " + packageId +
                                " is not ready for shipping"
                );
            }

            // Package cannot belong to another shipment.
            if (shipmentDAO.isPackageAlreadyShipped(
                    packageId)) {

                // It is okay only if it already belongs
                // to THIS shipment.
                boolean alreadyInThisShipment =
                        shipment.getPackages() != null &&
                        shipment.getPackages()
                                .stream()
                                .anyMatch(
                                        p -> p.getId() == packageId
                                );

                if (!alreadyInThisShipment) {

                    throw new IllegalArgumentException(
                            "Package " + packageId +
                                    " is already assigned to another shipment"
                    );
                }
            }

            validatePackageDateAgainstShipment(
                    pkg,
                    shipment.getShipmentDate()
            );
        }

        shipmentDAO.replacePackages(
                shipmentId,
                packageIds
        );
    }


    // =====================================================
    // DATE VALIDATION
    // =====================================================

    private void validateShipmentDates(
            Shipment shipment) {

        LocalDate shipmentDate =
                shipment.getShipmentDate();

        LocalDate estimatedDate =
                shipment.getEstimatedDeliveryDate();

        LocalDate actualDate =
                shipment.getActualDeliveryDate();

        if (shipmentDate == null) {

            throw new IllegalArgumentException(
                    "Shipment date is required"
            );
        }

        if (estimatedDate != null &&
                estimatedDate.isBefore(shipmentDate)) {

            throw new IllegalArgumentException(
                    "Estimated delivery date cannot be before shipment date"
            );
        }

        if (actualDate != null &&
                actualDate.isBefore(shipmentDate)) {

            throw new IllegalArgumentException(
                    "Actual delivery date cannot be before shipment date"
            );
        }

        if (shipment.getStatus() ==
                ShipmentStatus.DELIVERED &&
                actualDate == null) {

            throw new IllegalArgumentException(
                    "Delivered shipment must have an actual delivery date"
            );
        }

        if (shipment.getStatus() !=
                ShipmentStatus.DELIVERED &&
                actualDate != null) {

            throw new IllegalArgumentException(
                    "Actual delivery date can only be set when shipment is DELIVERED"
            );
        }
    }


    // =====================================================
    // PACKAGE DATE VALIDATION
    // =====================================================

    private void validatePackageDateAgainstShipment(
            Package pkg,
            LocalDate shipmentDate) {

        if (shipmentDate == null) {

            throw new IllegalArgumentException(
                    "Shipment date is required"
            );
        }

        if (pkg.getPackageDate() != null &&
                pkg.getPackageDate().isAfter(
                        shipmentDate
                )) {

            throw new IllegalArgumentException(
                    "Shipment date cannot be before package date for package "
                            + pkg.getPackageNumber()
            );
        }
    }


    // =====================================================
    // CREATION STATUS VALIDATION
    // =====================================================

    private void validateCreationStatus(
            ShipmentStatus status) {

        if (status == ShipmentStatus.DELIVERED) {

            throw new IllegalArgumentException(
                    "Shipment cannot be created directly as DELIVERED. "
                            + "Use Mark as Delivered with an actual delivery date."
            );
        }
    }


    // =====================================================
    // STATUS TRANSITION VALIDATION
    // =====================================================

    private void validateStatusTransition(
            ShipmentStatus oldStatus,
            ShipmentStatus newStatus) {

        if (oldStatus == null ||
                newStatus == null) {

            return;
        }

        if (oldStatus == ShipmentStatus.DELIVERED &&
                newStatus != ShipmentStatus.DELIVERED) {

            throw new IllegalStateException(
                    "Delivered shipment cannot change status."
            );
        }

        if (oldStatus == ShipmentStatus.CREATED &&
                newStatus == ShipmentStatus.CANCELLED) {

            return;
        }

        if (oldStatus == ShipmentStatus.CREATED &&
                (newStatus == ShipmentStatus.IN_TRANSIT ||
                        newStatus == ShipmentStatus.CREATED)) {

            return;
        }

        if (oldStatus == ShipmentStatus.IN_TRANSIT &&
                (newStatus == ShipmentStatus.IN_TRANSIT ||
                        newStatus == ShipmentStatus.DELIVERED ||
                        newStatus == ShipmentStatus.CANCELLED)) {

            return;
        }

        if (oldStatus == newStatus) {
            return;
        }

        
    }
    private void validateShipmentAgainstSalesOrderDate(
        Shipment shipment) {

    if (shipment.getPackages() == null ||
            shipment.getPackages().isEmpty()) {
        return;
    }

    LocalDate shipmentDate =
            shipment.getShipmentDate();

    if (shipmentDate == null) {
        return;
    }

    for (Package pkg : shipment.getPackages()) {

        if (pkg == null) {
    continue;
}

if (pkg.getSalesOrder() == null) {
    throw new IllegalArgumentException(
            "Cannot validate shipment date: "
                    + "Sales Order is not loaded for package "
                    + pkg.getPackageNumber()
    );
}

        if (pkg.getSalesOrder().getOrderDate() == null) {
            continue;
        }

        LocalDateTime salesOrderDateTime =
                pkg.getSalesOrder().getOrderDate();

        LocalDate salesOrderDate =
                salesOrderDateTime.toLocalDate();

        if (shipmentDate.isBefore(salesOrderDate)) {

            throw new IllegalArgumentException(
                    "Shipment date cannot be before Sales Order date. "
                            + "Sales Order Date: "
                            + salesOrderDate
                            + ", Shipment Date: "
                            + shipmentDate
            );
        }
    }
}
}

