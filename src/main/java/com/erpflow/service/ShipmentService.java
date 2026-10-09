package com.erpflow.service;

import com.erpflow.dao.PackageDAO;
import com.erpflow.dao.ShipmentDAO;
import com.erpflow.model.Package;
import com.erpflow.model.Shipment;
import com.erpflow.model.enums.ShipmentStatus;
import com.erpflow.util.TransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShipmentService {

    private final ShipmentDAO shipmentDAO = new ShipmentDAO();
    private final PackageDAO packageDAO = new PackageDAO();

    public void createShipment(Shipment shipment) {
        validateCreationRequest(shipment);

        TransactionManager.execute(() -> {
            shipmentDAO.save(shipment);

            if (shipment.getShipmentNumber() == null
                    || shipment.getShipmentNumber().isBlank()) {
                shipment.setShipmentNumber(
                        String.format("SH-%06d", shipment.getId()));
                shipmentDAO.update(shipment);
            }
            return null;
        });
    }

    public List<Shipment> getAllShipments() {
        return shipmentDAO.findAll();
    }

    public Shipment getShipmentById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid shipment ID");
        }
        return shipmentDAO.findById(id);
    }

    public void updateShipment(Shipment updatedShipment) {
        if (updatedShipment == null || updatedShipment.getId() <= 0) {
            throw new IllegalArgumentException("Invalid shipment");
        }

        TransactionManager.execute(() -> {
            Shipment existing = shipmentDAO.findById(updatedShipment.getId());
            if (existing == null) {
                throw new IllegalArgumentException("Shipment not found");
            }
            if (existing.getStatus() == ShipmentStatus.DELIVERED) {
                throw new IllegalStateException("Delivered shipments cannot be edited.");
            }

            if (updatedShipment.getShippingCharge() < 0) {
                throw new IllegalArgumentException("Shipping charge cannot be negative");
            }

            mergeShipmentFields(existing, updatedShipment);
            validateShipmentAgainstSalesOrderDate(updatedShipment);
            validateShipmentDates(updatedShipment);
            validateStatusTransition(existing.getStatus(), updatedShipment.getStatus());

            shipmentDAO.update(updatedShipment);
            return null;
        });
    }

    public void markShipmentDelivered(
            int shipmentId,
            LocalDate actualDeliveryDate) {

        if (shipmentId <= 0) {
            throw new IllegalArgumentException("Invalid shipment ID");
        }
        if (actualDeliveryDate == null) {
            throw new IllegalArgumentException("Actual delivery date is required");
        }

        TransactionManager.execute(() -> {
            Shipment shipment = shipmentDAO.findById(shipmentId);
            if (shipment == null) {
                throw new IllegalArgumentException("Shipment not found");
            }
            if (shipment.getStatus() == ShipmentStatus.DELIVERED) {
                throw new IllegalStateException("Shipment is already marked as delivered");
            }
            if (shipment.getShipmentDate() != null
                    && actualDeliveryDate.isBefore(shipment.getShipmentDate())) {
                throw new IllegalArgumentException(
                        "Actual delivery date cannot be before shipment date");
            }

            shipment.setStatus(ShipmentStatus.DELIVERED);
            shipment.setActualDeliveryDate(actualDeliveryDate);
            validateShipmentDates(shipment);
            shipmentDAO.update(shipment);
            return null;
        });
    }

    public void addPackageToShipment(int shipmentId, int packageId) {
        if (shipmentId <= 0 || packageId <= 0) {
            throw new IllegalArgumentException("Shipment ID and package ID must be valid");
        }

        TransactionManager.execute(() -> {
            Shipment shipment = shipmentDAO.findById(shipmentId);
            if (shipment == null) {
                throw new IllegalArgumentException("Shipment not found");
            }
            if (shipment.getStatus() == ShipmentStatus.DELIVERED) {
                throw new IllegalStateException("Packages cannot be changed after delivery.");
            }

            Package pkg = packageDAO.findById(packageId);
            validatePackageForShipment(pkg, shipment);

            if (shipment.getPackages() != null
                    && shipment.getPackages().stream().anyMatch(p -> p.getId() == packageId)) {
                throw new IllegalArgumentException("Package is already assigned to this shipment");
            }

            if (shipmentDAO.isPackageAlreadyShipped(packageId)) {
                throw new IllegalArgumentException(
                        "Package is already assigned to another shipment");
            }

            shipmentDAO.addPackage(shipmentId, packageId);
            return null;
        });
    }

    public void updateShipmentPackages(
            int shipmentId,
            List<Integer> packageIds) {

        if (shipmentId <= 0) {
            throw new IllegalArgumentException("Invalid shipment ID");
        }
        if (packageIds == null) {
            throw new IllegalArgumentException("Package IDs are required");
        }

        TransactionManager.execute(() -> {
            Shipment shipment = shipmentDAO.findById(shipmentId);
            if (shipment == null) {
                throw new IllegalArgumentException("Shipment not found");
            }
            if (shipment.getStatus() == ShipmentStatus.DELIVERED) {
                throw new IllegalStateException(
                        "Packages cannot be changed after delivery.");
            }

            Set<Integer> uniqueIds = new HashSet<>(packageIds);
            if (uniqueIds.size() != packageIds.size()) {
                throw new IllegalArgumentException("Duplicate package selected");
            }

            List<Package> packages = packageDAO.findByIds(packageIds);
            if (packages.size() != packageIds.size()) {
                throw new IllegalArgumentException("One or more packages were not found");
            }

            Set<Integer> alreadyAssigned = shipmentDAO.findAlreadyShippedPackageIds(packageIds);
            Set<Integer> currentPackageIds = new HashSet<>();
            if (shipment.getPackages() != null) {
                shipment.getPackages().forEach(p -> currentPackageIds.add(p.getId()));
            }

            for (Package pkg : packages) {
                validatePackageForShipment(pkg, shipment);
                if (alreadyAssigned.contains(pkg.getId())
                        && !currentPackageIds.contains(pkg.getId())) {
                    throw new IllegalArgumentException(
                            "Package " + pkg.getId()
                                    + " is already assigned to another shipment");
                }
            }

            shipmentDAO.replacePackages(shipmentId, packageIds);
            return null;
        });
    }

    private void validateCreationRequest(Shipment shipment) {
        if (shipment == null) {
            throw new IllegalArgumentException("Shipment cannot be null");
        }
        if (shipment.getShipmentDate() == null) {
            throw new IllegalArgumentException("Shipment date is required");
        }
        if (shipment.getStatus() == null) {
            shipment.setStatus(ShipmentStatus.CREATED);
        }
        validateCreationStatus(shipment.getStatus());
        if (shipment.getShippingMethod() == null || shipment.getShippingMethod().isBlank()) {
            shipment.setShippingMethod("MANUAL");
        }
        if (shipment.getShippingCharge() < 0) {
            throw new IllegalArgumentException("Shipping charge cannot be negative");
        }
        validateShipmentDates(shipment);
    }

    private void mergeShipmentFields(Shipment existing, Shipment updated) {
        updated.setShipmentNumber(existing.getShipmentNumber());

        if (updated.getShipmentDate() == null) updated.setShipmentDate(existing.getShipmentDate());
        if (updated.getStatus() == null) updated.setStatus(existing.getStatus());
        if (updated.getShippingMethod() == null || updated.getShippingMethod().isBlank()) {
            updated.setShippingMethod(existing.getShippingMethod());
        }
        if (updated.getCarrier() == null) updated.setCarrier(existing.getCarrier());
        if (updated.getCarrierService() == null) updated.setCarrierService(existing.getCarrierService());
        if (updated.getTrackingNumber() == null) updated.setTrackingNumber(existing.getTrackingNumber());
        if (updated.getTrackingUrl() == null) updated.setTrackingUrl(existing.getTrackingUrl());
        if (updated.getDispatchAddress() == null) updated.setDispatchAddress(existing.getDispatchAddress());
        if (updated.getDestinationAddress() == null) updated.setDestinationAddress(existing.getDestinationAddress());
        if (updated.getEstimatedDeliveryDate() == null) updated.setEstimatedDeliveryDate(existing.getEstimatedDeliveryDate());
        if (updated.getActualDeliveryDate() == null) updated.setActualDeliveryDate(existing.getActualDeliveryDate());
        if (updated.getNotes() == null) updated.setNotes(existing.getNotes());

        updated.setPackages(existing.getPackages());
    }

    private void validatePackageForShipment(Package pkg, Shipment shipment) {
        if (pkg == null) {
            throw new IllegalArgumentException("Package not found");
        }
        if (pkg.getSalesOrder() == null) {
            throw new IllegalArgumentException("Package is not linked to a Sales Order");
        }
        if (pkg.getStatus() == null || !"PACKED".equalsIgnoreCase(pkg.getStatus().name())) {
            throw new IllegalArgumentException("Only PACKED packages can be assigned to a shipment");
        }
        validatePackageDateAgainstShipment(pkg, shipment.getShipmentDate());
    }

    private void validateShipmentDates(Shipment shipment) {
        LocalDate shipmentDate = shipment.getShipmentDate();
        LocalDate estimatedDate = shipment.getEstimatedDeliveryDate();
        LocalDate actualDate = shipment.getActualDeliveryDate();

        if (shipmentDate == null) {
            throw new IllegalArgumentException("Shipment date is required");
        }
        if (estimatedDate != null && estimatedDate.isBefore(shipmentDate)) {
            throw new IllegalArgumentException(
                    "Estimated delivery date cannot be before shipment date");
        }
        if (actualDate != null && actualDate.isBefore(shipmentDate)) {
            throw new IllegalArgumentException(
                    "Actual delivery date cannot be before shipment date");
        }
        if (shipment.getStatus() == ShipmentStatus.DELIVERED && actualDate == null) {
            throw new IllegalArgumentException(
                    "Delivered shipment must have an actual delivery date");
        }
        if (shipment.getStatus() != ShipmentStatus.DELIVERED && actualDate != null) {
            throw new IllegalArgumentException(
                    "Actual delivery date can only be set when shipment is DELIVERED");
        }
    }

    private void validatePackageDateAgainstShipment(
            Package pkg,
            LocalDate shipmentDate) {
        if (shipmentDate == null) {
            throw new IllegalArgumentException("Shipment date is required");
        }
        if (pkg.getPackageDate() != null && pkg.getPackageDate().isAfter(shipmentDate)) {
            throw new IllegalArgumentException(
                    "Shipment date cannot be before package date for package "
                            + pkg.getPackageNumber());
        }
    }

    private void validateCreationStatus(ShipmentStatus status) {
        if (status == ShipmentStatus.DELIVERED) {
            throw new IllegalArgumentException(
                    "Shipment cannot be created directly as DELIVERED. Use Mark as Delivered.");
        }
    }

    private void validateStatusTransition(
            ShipmentStatus oldStatus,
            ShipmentStatus newStatus) {
        if (oldStatus == null || newStatus == null || oldStatus == newStatus) {
            return;
        }
        if (oldStatus == ShipmentStatus.DELIVERED) {
            throw new IllegalStateException("Delivered shipment cannot change status.");
        }
        if (oldStatus == ShipmentStatus.CREATED
                && (newStatus == ShipmentStatus.IN_TRANSIT
                || newStatus == ShipmentStatus.CANCELLED)) {
            return;
        }
        if (oldStatus == ShipmentStatus.IN_TRANSIT
                && (newStatus == ShipmentStatus.DELIVERED
                || newStatus == ShipmentStatus.CANCELLED)) {
            return;
        }
        throw new IllegalStateException(
                "Invalid shipment status transition: " + oldStatus + " -> " + newStatus);
    }

    private void validateShipmentAgainstSalesOrderDate(Shipment shipment) {
        if (shipment.getPackages() == null || shipment.getPackages().isEmpty()) {
            return;
        }

        LocalDate shipmentDate = shipment.getShipmentDate();
        if (shipmentDate == null) {
            return;
        }

        for (Package pkg : shipment.getPackages()) {
            if (pkg == null || pkg.getSalesOrder() == null
                    || pkg.getSalesOrder().getOrderDate() == null) {
                continue;
            }

            LocalDate salesOrderDate =
                    LocalDateTime.of(pkg.getSalesOrder().getOrderDate().toLocalDate(),
                            java.time.LocalTime.MIDNIGHT).toLocalDate();

            if (shipmentDate.isBefore(salesOrderDate)) {
                throw new IllegalArgumentException(
                        "Shipment date cannot be before Sales Order date. Sales Order Date: "
                                + salesOrderDate + ", Shipment Date: " + shipmentDate);
            }
        }
    }
}
