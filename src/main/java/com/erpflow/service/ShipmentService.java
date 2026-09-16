package com.erpflow.service;

import com.erpflow.dao.ShipmentDAO;
import com.erpflow.model.Shipment;

import java.util.List;

public class ShipmentService {

    private final ShipmentDAO shipmentDAO =
            new ShipmentDAO();

    public void createShipment(Shipment shipment) {

        if (shipment == null) {
            throw new RuntimeException(
                    "Shipment cannot be null"
            );
        }

        shipmentDAO.save(shipment);
    }

    public List<Shipment> getAllShipments() {

        return shipmentDAO.findAll();
    }

    public Shipment getShipmentById(int id) {

        return shipmentDAO.findById(id);
    }
}