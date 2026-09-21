
package com.erpflow.service;

import com.erpflow.dao.ShipmentDAO;
import com.erpflow.model.Shipment;

import java.time.LocalDate;
import java.util.List;

public class ShipmentService {

    private final ShipmentDAO shipmentDAO = new ShipmentDAO();

    // =========================
    // CREATE
    // =========================

    public void createShipment(Shipment shipment) {

        if (shipment == null) {
            throw new IllegalArgumentException("Shipment cannot be null");
        }

        if (shipment.getShipmentDate() == null) {
            throw new IllegalArgumentException("Shipment date is required");
        }

        if (shipment.getStatus() == null || shipment.getStatus().isBlank()) {
            shipment.setStatus("CREATED");
        }

        if (shipment.getShippingMethod() == null ||
                shipment.getShippingMethod().isBlank()) {
            shipment.setShippingMethod("MANUAL");
        }

        shipmentDAO.save(shipment);

        if (shipment.getShipmentNumber() == null ||
                shipment.getShipmentNumber().isBlank()) {

            shipment.setShipmentNumber(
                    String.format("SH-%06d", shipment.getId())
            );

            shipmentDAO.update(shipment);
        }
    }

    // =========================
    // GET ALL
    // =========================

    public List<Shipment> getAllShipments() {
        return shipmentDAO.findAll();
    }

    // =========================
    // GET BY ID
    // =========================

    public Shipment getShipmentById(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException("Invalid shipment ID");
        }

        return shipmentDAO.findById(id);
    }

    // =========================
    // UPDATE SHIPMENT DETAILS
    // =========================

    public void updateShipment(Shipment updatedShipment) {

        if (updatedShipment == null || updatedShipment.getId() <= 0) {
            throw new IllegalArgumentException("Invalid shipment");
        }

        Shipment existing = shipmentDAO.findById(updatedShipment.getId());

        if (existing == null) {
            throw new IllegalArgumentException("Shipment not found");
        }

        if (updatedShipment.getShippingCharge() < 0) {
            throw new IllegalArgumentException(
                    "Shipping charge cannot be negative"
            );
        }

        // Keep the original shipment number.
        updatedShipment.setShipmentNumber(existing.getShipmentNumber());

        // Preserve existing values when fields are omitted.
        if (updatedShipment.getShipmentDate() == null) {
            updatedShipment.setShipmentDate(existing.getShipmentDate());
        }

        if (updatedShipment.getStatus() == null ||
                updatedShipment.getStatus().isBlank()) {
            updatedShipment.setStatus(existing.getStatus());
        }

        if (updatedShipment.getShippingMethod() == null ||
                updatedShipment.getShippingMethod().isBlank()) {
            updatedShipment.setShippingMethod(existing.getShippingMethod());
        }

        if (updatedShipment.getCarrier() == null) {
            updatedShipment.setCarrier(existing.getCarrier());
        }

        if (updatedShipment.getCarrierService() == null) {
            updatedShipment.setCarrierService(existing.getCarrierService());
        }

        if (updatedShipment.getTrackingNumber() == null) {
            updatedShipment.setTrackingNumber(existing.getTrackingNumber());
        }

        if (updatedShipment.getTrackingUrl() == null) {
            updatedShipment.setTrackingUrl(existing.getTrackingUrl());
        }

        if (updatedShipment.getDispatchAddress() == null) {
            updatedShipment.setDispatchAddress(existing.getDispatchAddress());
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
            updatedShipment.setNotes(existing.getNotes());
        }

        // Package assignments are managed through the package endpoint.
        updatedShipment.setPackages(existing.getPackages());

        shipmentDAO.update(updatedShipment);
    }

    // =========================
    // MARK AS DELIVERED
    // =========================

    public void markShipmentDelivered(int shipmentId) {

        if (shipmentId <= 0) {
            throw new IllegalArgumentException("Invalid shipment ID");
        }

        Shipment shipment = shipmentDAO.findById(shipmentId);

        if (shipment == null) {
            throw new IllegalArgumentException("Shipment not found");
        }

        if ("DELIVERED".equalsIgnoreCase(shipment.getStatus())) {
            throw new IllegalArgumentException(
                    "Shipment is already marked as delivered"
            );
        }

        shipment.setStatus("DELIVERED");
        shipment.setActualDeliveryDate(LocalDate.now());

        shipmentDAO.update(shipment);
    }

    // =========================
    // LINK PACKAGE
    // =========================

    public void addPackageToShipment(int shipmentId, int packageId) {

        if (shipmentId <= 0 || packageId <= 0) {
            throw new IllegalArgumentException(
                    "Shipment ID and package ID must be valid"
            );
        }

        Shipment shipment = shipmentDAO.findById(shipmentId);

        if (shipment == null) {
            throw new IllegalArgumentException("Shipment not found");
        }

        if (shipment.getPackages() != null &&
                shipment.getPackages().stream()
                        .anyMatch(pkg -> pkg.getId() == packageId)) {
            throw new IllegalArgumentException(
                    "Package is already assigned to this shipment"
            );
        }

        if (shipmentDAO.isPackageAlreadyShipped(packageId)) {
            throw new IllegalArgumentException(
                    "Package is already assigned to another shipment"
            );
        }

        shipmentDAO.addPackage(shipmentId, packageId);
    }

    // =========================
    // REPLACE PACKAGE ASSIGNMENTS
    // =========================

    public void updateShipmentPackages(
            int shipmentId,
            List<Integer> packageIds) {

        if (shipmentId <= 0) {
            throw new IllegalArgumentException("Invalid shipment ID");
        }

        if (packageIds == null) {
            throw new IllegalArgumentException("Package IDs are required");
        }

        Shipment shipment = shipmentDAO.findById(shipmentId);

        if (shipment == null) {
            throw new IllegalArgumentException("Shipment not found");
        }

        if ("DELIVERED".equalsIgnoreCase(shipment.getStatus())) {
    throw new IllegalStateException(
        "Packages cannot be changed after delivery."
    );
}

shipmentDAO.replacePackages(shipmentId, packageIds);
        

        // Package existence, duplicate IDs, and cross-shipment assignment
        // checks are performed by ShipmentDAO.replacePackages().
        shipmentDAO.replacePackages(shipmentId, packageIds);
    }
}