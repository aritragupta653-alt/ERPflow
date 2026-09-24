package com.erpflow.service;

import com.erpflow.dao.CarrierDAO;
import com.erpflow.dao.CarrierServiceDAO;
import com.erpflow.model.Carrier;
import com.erpflow.model.CarrierService;
import com.erpflow.model.enums.CarrierStatus;

import java.util.List;

public class CarrierServiceService {

    private final CarrierDAO carrierDAO = new CarrierDAO();
    private final CarrierServiceDAO carrierServiceDAO =
            new CarrierServiceDAO();

    public List<Carrier> getAllCarriers() {
        return carrierDAO.findAll();
    }

    public Carrier getCarrierById(int id) {
        return carrierDAO.findById(id);
    }

    public List<CarrierService> getServicesByCarrier(int carrierId) {

        Carrier carrier = carrierDAO.findById(carrierId);

        if (carrier == null) {
            throw new RuntimeException("Carrier not found");
        }

        return carrierServiceDAO.findByCarrier(carrierId);
    }

    public CarrierService getServiceById(int id) {
        return carrierServiceDAO.findById(id);
    }

    public void createCarrier(Carrier carrier) {

        if (carrier == null) {
            throw new RuntimeException("Carrier is required");
        }

        if (carrier.getName() == null ||
                carrier.getName().isBlank()) {

            throw new RuntimeException("Carrier name is required");
        }

        if (carrier.getCode() == null ||
                carrier.getCode().isBlank()) {

            throw new RuntimeException("Carrier code is required");
        }

        if (carrier.getStatus() == null) {
            carrier.setStatus(CarrierStatus.ACTIVE);
        }

        carrierDAO.save(carrier);
    }

    public void createCarrierService(CarrierService service) {

        if (service == null) {
            throw new RuntimeException("Carrier service is required");
        }

        if (service.getCarrier() == null ||
                service.getCarrier().getId() <= 0) {

            throw new RuntimeException("Carrier is required");
        }

        if (service.getName() == null ||
                service.getName().isBlank()) {

            throw new RuntimeException("Service name is required");
        }

        if (service.getEstimatedDays() <= 0) {
            throw new RuntimeException(
                    "Estimated days must be greater than 0");
        }

        if (service.getBaseCharge() == null ||
                service.getBaseCharge().signum() < 0) {

            throw new RuntimeException(
                    "Base charge cannot be negative");
        }

        if (service.getChargePerKg() == null ||
                service.getChargePerKg().signum() < 0) {

            throw new RuntimeException(
                    "Charge per kg cannot be negative");
        }

        Carrier carrier =
                carrierDAO.findById(service.getCarrier().getId());

        if (carrier == null) {
            throw new RuntimeException("Carrier not found");
        }

        service.setCarrier(carrier);

        carrierServiceDAO.save(service);
    }
}