package com.erpflow.service;

import com.erpflow.dao.CarrierServiceDAO;
import com.erpflow.dao.PackageDAO;
import com.erpflow.model.CarrierService;
import com.erpflow.model.Package;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class ShippingRateService {

    private static final BigDecimal DIMENSIONAL_DIVISOR =
            new BigDecimal("5000");

    private final PackageDAO packageDAO = new PackageDAO();
    private final CarrierServiceDAO carrierServiceDAO =
            new CarrierServiceDAO();

    public ShippingRate calculateRate(
            List<Integer> packageIds,
            int carrierServiceId) {

        if (packageIds == null || packageIds.isEmpty()) {
            throw new RuntimeException(
                    "Select at least one package");
        }

        CarrierService carrierService =
                carrierServiceDAO.findById(carrierServiceId);

        if (carrierService == null) {
            throw new RuntimeException(
                    "Carrier service not found");
        }

        BigDecimal totalActualWeight =
                BigDecimal.ZERO;

        BigDecimal totalDimensionalWeight =
                BigDecimal.ZERO;

        for (Integer packageId : packageIds) {

            Package pkg = packageDAO.findById(packageId);

            if (pkg == null) {
                throw new RuntimeException(
                        "Package not found: " + packageId);
            }

            if (pkg.getWeight() <= 0) {
                throw new RuntimeException(
                        "Package " + pkg.getPackageNumber()
                                + " has invalid weight");
            }

            if (pkg.getLength() <= 0 ||
                    pkg.getWidth() <= 0 ||
                    pkg.getHeight() <= 0) {

                throw new RuntimeException(
                        "Package " + pkg.getPackageNumber()
                                + " has invalid dimensions");
            }

            BigDecimal actualWeight =
                    BigDecimal.valueOf(pkg.getWeight());

            BigDecimal dimensionalWeight =
                    BigDecimal.valueOf(pkg.getLength())
                            .multiply(
                                    BigDecimal.valueOf(
                                            pkg.getWidth()))
                            .multiply(
                                    BigDecimal.valueOf(
                                            pkg.getHeight()))
                            .divide(
                                    DIMENSIONAL_DIVISOR,
                                    2,
                                    RoundingMode.HALF_UP
                            );

            totalActualWeight =
                    totalActualWeight.add(actualWeight);

            totalDimensionalWeight =
                    totalDimensionalWeight.add(
                            dimensionalWeight);
        }

        BigDecimal chargeableWeight =
                totalActualWeight.max(
                        totalDimensionalWeight);

        BigDecimal baseCharge =
                carrierService.getBaseCharge();

        BigDecimal chargePerKg =
                carrierService.getChargePerKg();

        BigDecimal weightCharge =
                chargeableWeight.multiply(
                        chargePerKg);

        BigDecimal totalCharge =
                baseCharge.add(weightCharge)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP);

        return new ShippingRate(
                totalActualWeight.setScale(
                        2,
                        RoundingMode.HALF_UP),
                totalDimensionalWeight.setScale(
                        2,
                        RoundingMode.HALF_UP),
                chargeableWeight.setScale(
                        2,
                        RoundingMode.HALF_UP),
                baseCharge.setScale(
                        2,
                        RoundingMode.HALF_UP),
                chargePerKg.setScale(
                        2,
                        RoundingMode.HALF_UP),
                totalCharge,
                carrierService
        );
    }

    public static class ShippingRate {

        private BigDecimal actualWeight;
        private BigDecimal dimensionalWeight;
        private BigDecimal chargeableWeight;
        private BigDecimal baseCharge;
        private BigDecimal chargePerKg;
        private BigDecimal totalCharge;
        private CarrierService carrierService;

        public ShippingRate(
                BigDecimal actualWeight,
                BigDecimal dimensionalWeight,
                BigDecimal chargeableWeight,
                BigDecimal baseCharge,
                BigDecimal chargePerKg,
                BigDecimal totalCharge,
                CarrierService carrierService) {

            this.actualWeight = actualWeight;
            this.dimensionalWeight = dimensionalWeight;
            this.chargeableWeight = chargeableWeight;
            this.baseCharge = baseCharge;
            this.chargePerKg = chargePerKg;
            this.totalCharge = totalCharge;
            this.carrierService = carrierService;
        }

        public BigDecimal getActualWeight() {
            return actualWeight;
        }

        public BigDecimal getDimensionalWeight() {
            return dimensionalWeight;
        }

        public BigDecimal getChargeableWeight() {
            return chargeableWeight;
        }

        public BigDecimal getBaseCharge() {
            return baseCharge;
        }

        public BigDecimal getChargePerKg() {
            return chargePerKg;
        }

        public BigDecimal getTotalCharge() {
            return totalCharge;
        }

        public CarrierService getCarrierService() {
            return carrierService;
        }
    }
}