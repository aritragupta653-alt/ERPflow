package com.erpflow.service;

import com.erpflow.dao.CarrierDAO;
import com.erpflow.dao.CarrierServiceDAO;
import com.erpflow.dao.PackageDAO;
import com.erpflow.dao.PackageItemDAO;
import com.erpflow.dao.SalesOrderDAO;
import com.erpflow.dao.SalesOrderItemDAO;
import com.erpflow.dao.ShipmentDAO;

import com.erpflow.model.Carrier;
import com.erpflow.model.CarrierService;
import com.erpflow.model.Item;
import com.erpflow.model.Package;
import com.erpflow.model.PackageItem;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;
import com.erpflow.model.Shipment;

import com.erpflow.model.enums.PackageStatus;
import com.erpflow.model.enums.ShipmentStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AutoPackShipService {

    // =====================================================
    // DEFAULT PACKAGE VALUES
    // =====================================================

    private static final double DEFAULT_LENGTH = 30.0;
    private static final double DEFAULT_WIDTH = 20.0;
    private static final double DEFAULT_HEIGHT = 15.0;

    private static final double DEFAULT_WEIGHT_PER_UNIT = 1.0;

    private static final String DEFAULT_DISPATCH_ADDRESS =
            "ERPFlow Warehouse, Chennai, Tamil Nadu, India";


    // =====================================================
    // DAOS / SERVICES
    // =====================================================

    private final SalesOrderDAO salesOrderDAO =
            new SalesOrderDAO();

    private final SalesOrderItemDAO salesOrderItemDAO =
            new SalesOrderItemDAO();

    private final PackageDAO packageDAO =
            new PackageDAO();

    private final PackageItemDAO packageItemDAO =
            new PackageItemDAO();

    private final ShipmentDAO shipmentDAO =
            new ShipmentDAO();

    private final PackageService packageService =
            new PackageService();

    private final ShippingService shippingService =
            new ShippingService();

    private final CarrierDAO carrierDAO =
            new CarrierDAO();

    private final CarrierServiceDAO carrierServiceDAO =
            new CarrierServiceDAO();


    // =====================================================
    // AUTO PACK & SHIP
    // =====================================================

    public AutoPackShipResult packAndShip(
            int salesOrderId,
            LocalDate shipmentDate,
            String deliveryStatus) {

        // =================================================
        // 1. BASIC VALIDATION
        // =================================================

        if (salesOrderId <= 0) {

            throw new RuntimeException(
                    "A valid Sales Order ID is required"
            );
        }

        if (shipmentDate == null) {

            throw new RuntimeException(
                    "Shipment date is required"
            );
        }

        if (deliveryStatus == null ||
                deliveryStatus.trim().isEmpty()) {

            throw new RuntimeException(
                    "Delivery status is required"
            );
        }


        String normalizedStatus =
                deliveryStatus
                        .trim()
                        .toUpperCase();


        ShipmentStatus shipmentStatus;

        try {

            shipmentStatus =
                    ShipmentStatus.valueOf(
                            normalizedStatus
                    );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Invalid shipment status: "
                            + deliveryStatus
            );
        }


        // =================================================
        // AUTO PACK & SHIP STATUS
        // =================================================

        /*
         * Auto Pack & Ship can create a shipment as:
         *
         * CREATED
         * IN_TRANSIT
         *
         * DELIVERED is NOT allowed here.
         *
         * Delivery must happen through the existing
         * Mark As Delivered flow.
         */

        if (shipmentStatus != ShipmentStatus.CREATED &&
                shipmentStatus != ShipmentStatus.IN_TRANSIT) {

            throw new RuntimeException(
                    "Auto Pack & Ship status must be CREATED or IN_TRANSIT"
            );
        }


        // =================================================
        // 2. FIND SALES ORDER
        // =================================================

        SalesOrder salesOrder =
                salesOrderDAO.findById(
                        salesOrderId
                );


        if (salesOrder == null) {

            throw new RuntimeException(
                    "Sales Order not found"
            );
        }


        // =================================================
        // 3. VALIDATE SALES ORDER STATUS
        // =================================================

        String orderStatus =
                salesOrder.getStatus() != null
                        ? salesOrder
                            .getStatus()
                            .toString()
                            .toUpperCase()
                        : "";


        if ("CANCELLED".equals(orderStatus) ||
                "SHIPPED".equals(orderStatus) ||
                "COMPLETED".equals(orderStatus) ||
                "DELIVERED".equals(orderStatus)) {

            throw new RuntimeException(
                    "Cannot Auto Pack & Ship a Sales Order with status "
                            + orderStatus
            );
        }


        // =================================================
        // 4. LOAD SALES ORDER ITEMS
        // =================================================

        List<SalesOrderItem> orderLines =
                salesOrderItemDAO.findBySalesOrder(
                        salesOrder
                );


        if (orderLines == null ||
                orderLines.isEmpty()) {

            throw new RuntimeException(
                    "Sales Order contains no items"
            );
        }


        // =================================================
        // 5. CALCULATE ALREADY PACKAGED QUANTITY
        // =================================================

        Map<Integer, Integer> alreadyPacked =
                getAlreadyPackagedQuantities(
                        salesOrderId
                );


        // =================================================
        // 6. BUILD REMAINING PACKAGE ITEMS
        // =================================================

        List<PackageItem> remainingPackageItems =
                new ArrayList<>();


        int totalRemainingUnits = 0;


        for (SalesOrderItem orderLine :
                orderLines) {

            if (orderLine == null ||
                    orderLine.getItem() == null) {

                throw new RuntimeException(
                        "Sales Order contains an invalid item line"
                );
            }


            Item item =
                    orderLine.getItem();


            // -------------------------------------------------
            // SERVICE / NON-INVENTORY ITEMS
            // -------------------------------------------------

            if (!"GOODS".equalsIgnoreCase(
                    item.getItemType()
            ) || !item.isTrackInventory()) {

                continue;
            }


            int orderedQuantity =
                    orderLine.getQuantity();


            if (orderedQuantity <= 0) {

                throw new RuntimeException(
                        "Invalid quantity for item: "
                                + item.getName()
                );
            }


            int packedQuantity =
                    alreadyPacked.getOrDefault(
                            orderLine.getId(),
                            0
                    );


            if (packedQuantity < 0) {

                throw new RuntimeException(
                        "Invalid packaged quantity for item: "
                                + item.getName()
                );
            }


            // -------------------------------------------------
            // REMAINING QUANTITY
            // -------------------------------------------------

            int remainingQuantity =
                    orderedQuantity -
                    packedQuantity;


            if (remainingQuantity < 0) {

                throw new RuntimeException(
                        "Packed quantity exceeds Sales Order quantity for item: "
                                + item.getName()
                );
            }


            if (remainingQuantity == 0) {
                continue;
            }


            // -------------------------------------------------
            // CREATE PACKAGE ITEM
            // -------------------------------------------------

            PackageItem packageItem =
                    new PackageItem();


            packageItem.setItem(item);

            packageItem.setQuantity(
                    remainingQuantity
            );

            packageItem.setSalesOrderItemId(
                    orderLine.getId()
            );


            remainingPackageItems.add(
                    packageItem
            );


            totalRemainingUnits +=
                    remainingQuantity;
        }


        // =================================================
        // 7. FIND CARRIER SERVICE
        // =================================================

        CarrierService selectedService =
                findDefaultCarrierService();


        if (selectedService == null) {

            throw new RuntimeException(
                    "No carrier service is configured. "
                            + "Configure a carrier and carrier service first."
            );
        }


        // =================================================
        // 8. CREATE PACKAGE IF ITEMS REMAIN
        // =================================================

        Package createdPackage = null;


        if (!remainingPackageItems.isEmpty()) {

            double estimatedWeight =
                    totalRemainingUnits *
                    DEFAULT_WEIGHT_PER_UNIT;


            /*
             * IMPORTANT:
             *
             * Your current PackageService requires
             * packageDate.
             *
             * Therefore Auto Pack & Ship must pass
             * the SAME shipment date as the package date.
             */

            createdPackage =
                    packageService.createPackage(
                            salesOrder,
                            remainingPackageItems,
                            estimatedWeight,
                            DEFAULT_LENGTH,
                            DEFAULT_WIDTH,
                            DEFAULT_HEIGHT,
                            shipmentDate
                    );
        }


        // =================================================
        // 9. FIND ALL PACKED BUT UNSHIPPED PACKAGES
        // =================================================

        List<Package> existingPackages =
                packageDAO.findBySalesOrder(
                        salesOrderId
                );


        List<Integer> packageIdsToShip =
                new ArrayList<>();


        if (existingPackages != null) {

            for (Package pkg :
                    existingPackages) {

                if (pkg == null) {
                    continue;
                }


                // -----------------------------------------
                // Only PACKED packages can be shipped
                // -----------------------------------------

                if (pkg.getStatus() != PackageStatus.PACKED) {
                    continue;
                }


                // -----------------------------------------
                // Don't ship an already shipped package
                // -----------------------------------------

                if (shipmentDAO.isPackageAlreadyShipped(
                        pkg.getId()
                )) {

                    continue;
                }


                // -----------------------------------------
                // Package date validation
                // -----------------------------------------

                if (pkg.getPackageDate() != null &&
                        pkg.getPackageDate().isAfter(
                                shipmentDate
                        )) {

                    throw new RuntimeException(
                            "Shipment date cannot be before package date for "
                                    + pkg.getPackageNumber()
                    );
                }


                packageIdsToShip.add(
                        pkg.getId()
                );
            }
        }


        // =================================================
        // 10. NOTHING TO SHIP
        // =================================================

        if (packageIdsToShip.isEmpty()) {

            throw new RuntimeException(
                    "There are no unshipped packages remaining for this Sales Order"
            );
        }


        // =================================================
        // 11. PREPARE SHIPMENT
        // =================================================

        Shipment shipment =
                new Shipment();


        shipment.setShipmentDate(
                shipmentDate
        );


        shipment.setStatus(
                shipmentStatus
        );


        shipment.setShippingMethod(
                selectedService.getName()
        );


        shipment.setCarrier(
                selectedService.getCarrier()
        );


        shipment.setCarrierService(
                selectedService
        );


        shipment.setDispatchAddress(
                DEFAULT_DISPATCH_ADDRESS
        );


        // =================================================
        // 12. CUSTOMER ADDRESS
        // =================================================

        if (salesOrder.getCustomer() == null ||
                salesOrder.getCustomer().getAddress() == null ||
                salesOrder.getCustomer()
                        .getAddress()
                        .trim()
                        .isEmpty()) {

            throw new RuntimeException(
                    "The customer has no shipping address. "
                            + "Update the customer details before shipping."
            );
        }


        shipment.setDestinationAddress(
                salesOrder
                        .getCustomer()
                        .getAddress()
        );


        shipment.setNotes(
                "Created automatically using Pack & Ship"
        );


        // =================================================
        // 13. ESTIMATED DELIVERY DATE
        // =================================================

        int estimatedDays =
                selectedService.getEstimatedDays();


        if (estimatedDays > 0) {

            shipment.setEstimatedDeliveryDate(
                    shipmentDate.plusDays(
                            estimatedDays
                    )
            );
        }


        // =================================================
        // 14. ACTUAL DELIVERY DATE
        // =================================================

        /*
         * NEVER set this during Auto Pack & Ship.
         *
         * It is set only by Mark As Delivered.
         */

        shipment.setActualDeliveryDate(
                null
        );


        // =================================================
        // 15. SHIP ALL RELEVANT PACKAGES
        // =================================================

        Shipment createdShipment =
                shippingService.shipPackages(
                        salesOrderId,
                        packageIdsToShip,
                        selectedService.getId(),
                        shipment
                );


        // =================================================
        // 16. RETURN RESULT
        // =================================================

        /*
         * If a new package was created, return it.
         *
         * If everything was already packaged and Auto
         * Pack & Ship only had to ship the existing
         * packages, return the first package that was
         * shipped.
         */

        Package resultPackage =
                createdPackage;


        if (resultPackage == null) {

            for (Package pkg :
                    existingPackages) {

                if (pkg != null &&
                        packageIdsToShip.contains(
                                pkg.getId()
                        )) {

                    resultPackage = pkg;
                    break;
                }
            }
        }


        return new AutoPackShipResult(
                resultPackage,
                createdShipment
        );
    }


    // =====================================================
    // CALCULATE ALREADY PACKAGED QUANTITIES
    // =====================================================

    private Map<Integer, Integer>
    getAlreadyPackagedQuantities(
            int salesOrderId) {

        Map<Integer, Integer> packedQuantities =
                new HashMap<>();


        List<Package> packages =
                packageDAO.findBySalesOrder(
                        salesOrderId
                );


        if (packages == null ||
                packages.isEmpty()) {

            return packedQuantities;
        }


        for (Package pkg :
                packages) {

            if (pkg == null) {
                continue;
            }


            /*
             * Every existing package consumes quantity.
             *
             * This is intentional:
             *
             * 10 ordered
             * 4 already packaged
             *
             * Auto Pack & Ship creates only 6.
             */

            List<PackageItem> packageItems =
                    packageItemDAO.findByPackage(
                            pkg
                    );


            if (packageItems == null) {
                continue;
            }


            for (PackageItem packageItem :
                    packageItems) {

                if (packageItem == null) {
                    continue;
                }


                int salesOrderItemId =
                        packageItem.getSalesOrderItemId();


                if (salesOrderItemId <= 0) {

                    throw new RuntimeException(
                            "Package "
                                    + pkg.getPackageNumber()
                                    + " contains an item without a Sales Order line reference"
                    );
                }


                int quantity =
                        packageItem.getQuantity();


                if (quantity <= 0) {

                    throw new RuntimeException(
                            "Invalid quantity in package "
                                    + pkg.getPackageNumber()
                    );
                }


                packedQuantities.merge(
                        salesOrderItemId,
                        quantity,
                        Integer::sum
                );
            }
        }


        return packedQuantities;
    }


    // =====================================================
    // FIND DEFAULT CARRIER SERVICE
    // =====================================================

    private CarrierService
    findDefaultCarrierService() {

        List<Carrier> carriers =
                carrierDAO.findAll();


        if (carriers == null ||
                carriers.isEmpty()) {

            return null;
        }


        for (Carrier carrier :
                carriers) {

            if (carrier == null) {
                continue;
            }


            if (carrier.getStatus() != null &&
                    "INACTIVE".equalsIgnoreCase(
                            carrier.getStatus().name()
                    )) {

                continue;
            }


            List<CarrierService> services =
                    carrierServiceDAO.findByCarrier(
                            carrier.getId()
                    );


            if (services == null ||
                    services.isEmpty()) {

                continue;
            }


            for (CarrierService service :
                    services) {

                if (service != null) {

                    return service;
                }
            }
        }


        return null;
    }


    // =====================================================
    // RESULT
    // =====================================================

    public static class AutoPackShipResult {

        private final Package packageEntity;

        private final Shipment shipment;


        public AutoPackShipResult(
                Package packageEntity,
                Shipment shipment) {

            this.packageEntity =
                    packageEntity;

            this.shipment =
                    shipment;
        }


        public Package getPackageEntity() {

            return packageEntity;
        }


        public Shipment getShipment() {

            return shipment;
        }
    }
}