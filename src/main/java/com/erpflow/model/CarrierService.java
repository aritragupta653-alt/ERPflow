package com.erpflow.model;

import java.math.BigDecimal;

public class CarrierService {

    private int id;

    private Carrier carrier;

    private String name;

    private int estimatedDays;

    private BigDecimal baseCharge;

    private BigDecimal chargePerKg;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Carrier getCarrier() {
        return carrier;
    }

    public void setCarrier(Carrier carrier) {
        this.carrier = carrier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getEstimatedDays() {
        return estimatedDays;
    }

    public void setEstimatedDays(int estimatedDays) {
        this.estimatedDays = estimatedDays;
    }

    public BigDecimal getBaseCharge() {
        return baseCharge;
    }

    public void setBaseCharge(BigDecimal baseCharge) {
        this.baseCharge = baseCharge;
    }

    public BigDecimal getChargePerKg() {
        return chargePerKg;
    }

    public void setChargePerKg(BigDecimal chargePerKg) {
        this.chargePerKg = chargePerKg;
    }
}