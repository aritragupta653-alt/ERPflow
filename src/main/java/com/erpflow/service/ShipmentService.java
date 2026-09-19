package com.erpflow.service;

import com.erpflow.dao.ShipmentDAO;
import com.erpflow.model.Shipment;

import java.util.List;

public class ShipmentService {

    private final ShipmentDAO shipmentDAO =
            new ShipmentDAO();


    // =========================
    // CREATE
    // =========================

    public void createShipment(Shipment shipment) {

        if (shipment == null) {
            throw new RuntimeException(
                    "Shipment cannot be null"
            );
        }

        if (shipment.getShipmentDate() == null) {
            throw new RuntimeException(
                    "Shipment date is required"
            );
        }

        if (shipment.getStatus() == null ||
                shipment.getStatus().isBlank()) {

            shipment.setStatus("CREATED");
        }

        if (shipment.getShippingMethod() == null ||
                shipment.getShippingMethod().isBlank()) {

            shipment.setShippingMethod("MANUAL");
        }

        shipmentDAO.save(shipment);

        // Generate shipment number after ID exists
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

        return shipmentDAO.findById(id);
    }


    // =========================
    // LINK PACKAGE
    // =========================

    public void addPackageToShipment(
            int shipmentId,
            int packageId) {

        if (shipmentDAO.isPackageAlreadyShipped(packageId)) {

            throw new RuntimeException(
                    "Package is already assigned to a shipment"
            );
        }

        shipmentDAO.addPackage(
                shipmentId,
                packageId
        );
    }
}