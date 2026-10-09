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

import com.erpflow.model.enums.CartonSize;
import com.erpflow.model.enums.PackageStatus;
import com.erpflow.model.enums.SalesOrderStatus;
import com.erpflow.model.enums.ShipmentStatus;

import com.erpflow.util.TransactionManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class AutoPackShipService {

    // =========================================================
    // DEFAULT VALUES
    // =========================================================

    private static final double DEFAULT_WEIGHT_PER_UNIT = 1.0;

    private static final String DEFAULT_DISPATCH_ADDRESS =
            "ERPFlow Warehouse, Chennai, Tamil Nadu, India";


   

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

    private final CarrierDAO carrierDAO =
            new CarrierDAO();

    private final CarrierServiceDAO carrierServiceDAO =
            new CarrierServiceDAO();

    private final ShippingRateService shippingRateService =
            new ShippingRateService();

    private final InventoryService inventoryService =
            new InventoryService();


    private final CartonSelectionService cartonSelectionService =
            new CartonSelectionService();


    // =========================================================
    // PACK AND SHIP
    // =========================================================

    public AutoPackShipResult packAndShip(
            int salesOrderId,
            LocalDate shipmentDate,
            String deliveryStatus,
            LocalDate deliveryDate) {

        // -----------------------------------------------------
        // 1. Validate request
        // -----------------------------------------------------

        validateRequest(
                salesOrderId,
                shipmentDate,
                deliveryStatus,
                deliveryDate
        );


        // -----------------------------------------------------
        // 2. Convert deliveryStatus to ShipmentStatus
        // -----------------------------------------------------

        ShipmentStatus shipmentStatus;

        try {

            shipmentStatus =
                    ShipmentStatus.valueOf(
                            deliveryStatus
                                    .trim()
                                    .toUpperCase()
                    );

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Invalid delivery status: "
                            + deliveryStatus
            );
        }



        if (shipmentStatus != ShipmentStatus.CREATED
                && shipmentStatus != ShipmentStatus.IN_TRANSIT
                && shipmentStatus != ShipmentStatus.DELIVERED) {

            throw new IllegalArgumentException(
                    "Auto Pack & Ship delivery status must be "
                            + "CREATED, IN_TRANSIT or DELIVERED"
            );
        }


        // -----------------------------------------------------
        // 3. One complete transaction
        // -----------------------------------------------------

        return TransactionManager.execute(() -> {

            // =================================================
            // LOAD SALES ORDER
            // =================================================

            SalesOrder salesOrder =
                    salesOrderDAO.findById(
                            salesOrderId
                    );

            if (salesOrder == null) {

                throw new IllegalArgumentException(
                        "Sales Order not found"
                );
            }


            // =================================================
            // VALIDATE SALES ORDER STATUS
            // =================================================

            if (salesOrder.getStatus() ==
                    SalesOrderStatus.CANCELLED
                    || salesOrder.getStatus() ==
                    SalesOrderStatus.SHIPPED
                    || salesOrder.getStatus() ==
                    SalesOrderStatus.COMPLETED) {

                throw new IllegalStateException(
                        "Cannot Auto Pack & Ship a Sales Order "
                                + "with status "
                                + salesOrder.getStatus()
                );
            }


            

            List<SalesOrderItem> orderLines =
                    salesOrderItemDAO
                            .findForUpdateBySalesOrder(
                                    salesOrderId
                            );

            if (orderLines == null ||
                    orderLines.isEmpty()) {

                throw new IllegalStateException(
                        "Sales Order contains no items"
                );
            }


            // =================================================
            // BUILD LINE LOOKUP
            // =================================================

            Map<Integer, SalesOrderItem> lineMap =
                    new HashMap<>();

            for (SalesOrderItem line :
                    orderLines) {

                if (line == null ||
                        line.getItem() == null) {

                    throw new IllegalStateException(
                            "Sales Order contains an invalid item line"
                    );
                }

                lineMap.put(
                        line.getId(),
                        line
                );
            }


            // =================================================
            // NEW PACKAGE DATA
            // =================================================

            List<PackageItem> newPackageItems =
                    new ArrayList<>();

            /*
             * packedDeltas stores:
             *
             * salesOrderItemId -> quantity to add to packed_quantity
             */
            Map<Integer, Integer> packedDeltas =
                    new HashMap<>();


            double calculatedWeight = 0.0;

            /*
             * Total physical volume required by the remaining
             * items.
             */
            double requiredVolume = 0.0;


            // =================================================
            // CALCULATE PACKAGE CONTENTS
            // =================================================

            for (SalesOrderItem line :
                    orderLines) {

                Item item =
                        line.getItem();


                // -------------------------------------------------
                // Validate ordered quantity
                // -------------------------------------------------

                if (line.getQuantity() <= 0) {

                    throw new IllegalStateException(
                            "Invalid quantity for item: "
                                    + item.getName()
                    );
                }


                // -------------------------------------------------
                // Ignore service / non-inventory items
                // -------------------------------------------------

                if (!"GOODS".equalsIgnoreCase(
                        item.getItemType())
                        || !item.isTrackInventory()) {

                    continue;
                }


                // -------------------------------------------------
                // Validate packed quantity
                // -------------------------------------------------

                if (line.getPackedQuantity() < 0
                        || line.getPackedQuantity()
                        > line.getQuantity()) {

                    throw new IllegalStateException(
                            "Invalid packed quantity for item: "
                                    + item.getName()
                    );
                }


                // -------------------------------------------------
                // Remaining quantity
                // -------------------------------------------------

                int remaining =
                        line.getQuantity()
                                - line.getPackedQuantity();


                /*
                 * Nothing new needs to be packed for this line.
                 */
                if (remaining <= 0) {
                    continue;
                }


                // =================================================
                // CREATE PACKAGE ITEM
                // =================================================

                PackageItem packageItem =
                        new PackageItem();

                packageItem.setItem(
                        item
                );

                packageItem.setSalesOrderItemId(
                        line.getId()
                );

                packageItem.setQuantity(
                        remaining
                );

                newPackageItems.add(
                        packageItem
                );


                // =================================================
                // PACKED DELTA
                // =================================================

                packedDeltas.put(
                        line.getId(),
                        remaining
                );


                // =================================================
                // WEIGHT
                // =================================================

                double unitWeight =
                        item.getWeight() > 0
                                ? item.getWeight()
                                : DEFAULT_WEIGHT_PER_UNIT;

                calculatedWeight +=
                        unitWeight * remaining;


                // =================================================
                // ITEM DIMENSIONS
                // =================================================

                if (item.getLength() <= 0
                        || item.getWidth() <= 0
                        || item.getHeight() <= 0) {

                    throw new IllegalStateException(
                            "Invalid dimensions for item: "
                                    + item.getName()
                                    + ". Length, width and height "
                                    + "must be greater than zero."
                    );
                }


                // =================================================
                // ITEM VOLUME
                // =================================================

                double itemVolume =
                        item.getLength()
                                * item.getWidth()
                                * item.getHeight();


                /*
                 * Required volume is based on the actual
                 * remaining quantity being packed.
                 */
                requiredVolume +=
                        itemVolume * remaining;
            }


            // =================================================
            // SELECT PREDEFINED CARTON
            // =================================================

            CartonSize selectedCarton =
                    null;

            if (!newPackageItems.isEmpty()) {

                selectedCarton =
                        cartonSelectionService
                                .selectCarton(
                                        requiredVolume
                                );


                if (selectedCarton == null) {

                    throw new IllegalStateException(
                            "No suitable carton is available "
                                    + "for the required package volume: "
                                    + requiredVolume
                    );
                }
            }


            // =================================================
            // CREATE NEW PACKAGE
            // =================================================

            Package createdPackage =
                    null;


            if (!newPackageItems.isEmpty()) {

                // -------------------------------------------------
                // Shipment date cannot be before Sales Order date
                // -------------------------------------------------

                if (salesOrder.getOrderDate() != null
                        && shipmentDate.isBefore(
                        salesOrder
                                .getOrderDate()
                                .toLocalDate())) {

                    throw new IllegalArgumentException(
                            "Shipment date cannot be before "
                                    + "Sales Order date"
                    );
                }


                LocalDate packageDate =
                        shipmentDate;


                createdPackage =
                        new Package();


                createdPackage.setSalesOrder(
                        salesOrder
                );

                createdPackage.setPackageDate(
                        packageDate
                );

                createdPackage.setStatus(
                        PackageStatus.PACKING
                );


                // -------------------------------------------------
                // Package weight
                // -------------------------------------------------

                createdPackage.setWeight(
                        Math.max(
                                0.01,
                                calculatedWeight
                        )
                );


                // -------------------------------------------------
                // IMPORTANT:
                //
                // Dimensions come from the selected predefined
                // carton, not arbitrary item max dimensions.
                // -------------------------------------------------

                createdPackage.setLength(
                        selectedCarton.getLength()
                );

                createdPackage.setWidth(
                        selectedCarton.getWidth()
                );

                createdPackage.setHeight(
                        selectedCarton.getHeight()
                );


                // -------------------------------------------------
                // Save package
                // -------------------------------------------------

                packageDAO.save(
                        createdPackage
                );


                // -------------------------------------------------
                // Generate package number
                // -------------------------------------------------

                createdPackage.setPackageNumber(
                        String.format(
                                "PKG-%06d",
                                createdPackage.getId()
                        )
                );


                packageDAO.update(
                        createdPackage
                );


                // -------------------------------------------------
                // Attach package to package items
                // -------------------------------------------------

                for (PackageItem packageItem :
                        newPackageItems) {

                    packageItem.setPackageEntity(
                            createdPackage
                    );
                }


                // -------------------------------------------------
                // Save package items in batch
                // -------------------------------------------------

                packageItemDAO.saveBatch(
                        newPackageItems
                );


                // -------------------------------------------------
                // Update packed quantities in one batch
                // -------------------------------------------------

                salesOrderItemDAO.adjustPackedQuantities(
                        packedDeltas
                );


                // -------------------------------------------------
                // Package is now packed
                // -------------------------------------------------

                createdPackage.setStatus(
                        PackageStatus.PACKED
                );

                packageDAO.update(
                        createdPackage
                );
            }


            // =================================================
            // LOAD PACKAGES
            // =================================================
            //
            // Re-read package state once because a newly-created
            // package now has its generated ID and PACKED status.
            //
            // =================================================

            List<Package> allPackages =
                    packageDAO.findBySalesOrder(
                            salesOrderId
                    );


            if (allPackages == null) {
                allPackages =
                        new ArrayList<>();
            }


            // =================================================
            // FIND PACKED PACKAGES
            // =================================================

            List<Integer> packageIdsToShip =
                    new ArrayList<>();


            Map<Integer, Package> packageMap =
                    new HashMap<>();


            for (Package pkg :
                    allPackages) {

                if (pkg == null) {
                    continue;
                }


                packageMap.put(
                        pkg.getId(),
                        pkg
                );


                /*
                 * Only PACKED packages are eligible for shipping.
                 */
                if (pkg.getStatus() ==
                        PackageStatus.PACKED) {

                    packageIdsToShip.add(
                            pkg.getId()
                    );
                }
            }


            if (packageIdsToShip.isEmpty()) {

                throw new IllegalStateException(
                        "There are no PACKED packages available "
                                + "for this Sales Order"
                );
            }


            // =================================================
            // VALIDATE PACKAGE DATES
            // =================================================

            for (Integer packageId :
                    packageIdsToShip) {

                Package pkg =
                        packageMap.get(
                                packageId
                        );

                if (pkg == null) {

                    throw new IllegalStateException(
                            "Package not found: "
                                    + packageId
                    );
                }


                if (pkg.getPackageDate() != null
                        && pkg.getPackageDate()
                        .isAfter(shipmentDate)) {

                    throw new IllegalArgumentException(
                            "Shipment date cannot be before "
                                    + "package date for "
                                    + pkg.getPackageNumber()
                    );
                }
            }


            // =================================================
            // FIND CARRIER SERVICE
            // =================================================

            CarrierService selectedService =
                    findDefaultCarrierService();


            if (selectedService == null) {

                throw new IllegalStateException(
                        "No carrier service is configured. "
                                + "Configure a carrier and carrier "
                                + "service before using Auto Pack & Ship."
                );
            }


            // =================================================
            // BUILD PACKAGES TO SHIP
            // =================================================

            List<Package> packagesToShip =
                    new ArrayList<>();


            for (Integer packageId :
                    packageIdsToShip) {

                Package pkg =
                        packageMap.get(
                                packageId
                        );


                if (pkg == null) {

                    throw new IllegalStateException(
                            "Package not found: "
                                    + packageId
                    );
                }


                packagesToShip.add(
                        pkg
                );
            }


            // =================================================
            // LOAD PACKAGE ITEMS
            // =================================================

            List<PackageItem> packageItems =
                    packageItemDAO.findByPackageIds(
                            packageIdsToShip
                    );


            if (packageItems == null) {
                packageItems =
                        new ArrayList<>();
            }


            // =================================================
            // BUILD SALES ORDER LINE LOOKUP
            // =================================================

            Map<Integer, SalesOrderItem> lineLookup =
                    new HashMap<>();


            for (SalesOrderItem line :
                    orderLines) {

                lineLookup.put(
                        line.getId(),
                        line
                );
            }


            // =================================================
            // SHIPPED DELTAS
            // =================================================
            //
            // package item quantities are aggregated by SO line.
            //
            // salesOrderItemId -> quantity to increment
            //
            // =================================================

            Map<Integer, Integer> shippedDeltas =
                    new HashMap<>();


            for (PackageItem packageItem :
                    packageItems) {

                Integer lineId =
                        packageItem.getSalesOrderItemId();


                if (lineId == null
                        || !lineLookup.containsKey(
                        lineId)) {

                    throw new IllegalStateException(
                            "Package item does not reference "
                                    + "a valid Sales Order line"
                    );
                }


                shippedDeltas.merge(
                        lineId,
                        packageItem.getQuantity(),
                        Math::addExact
                );
            }


            // =================================================
            // VALIDATE SHIPPED QUANTITY
            // =================================================

            for (Map.Entry<Integer, Integer> entry :
                    shippedDeltas.entrySet()) {

                SalesOrderItem line =
                        lineLookup.get(
                                entry.getKey()
                        );


                int currentShipped =
                        line.getShippedQuantity();


                int currentPacked =
                        line.getPackedQuantity();


                int packedAfterThisOperation =
                        currentPacked
                                + packedDeltas.getOrDefault(
                                line.getId(),
                                0
                        );


                int shippedAfterThisOperation =
                        currentShipped
                                + entry.getValue();


                if (shippedAfterThisOperation
                        > packedAfterThisOperation) {

                    throw new IllegalStateException(
                            "Auto Pack & Ship would ship "
                                    + "more than packed quantity "
                                    + "for line "
                                    + line.getId()
                    );
                }
            }


            // =================================================
            // CALCULATE SHIPPING RATE
            // =================================================

            ShippingRateService.ShippingRate rate =
                    shippingRateService
                            .calculateRateForPackages(
                                    packagesToShip,
                                    selectedService
                            );


            // =================================================
            // BUILD SHIPMENT
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


            shipment.setShippingCharge(
                    rate.getTotalCharge()
                            .doubleValue()
            );


            shipment.setDispatchAddress(
                    DEFAULT_DISPATCH_ADDRESS
            );


            // =================================================
            // VALIDATE CUSTOMER ADDRESS
            // =================================================

            if (salesOrder.getCustomer() == null
                    || salesOrder.getCustomer()
                    .getAddress() == null
                    || salesOrder.getCustomer()
                    .getAddress()
                    .isBlank()) {

                throw new IllegalArgumentException(
                        "The customer has no shipping address. "
                                + "Update the customer details "
                                + "before shipping."
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
            // ESTIMATED DELIVERY DATE
            // =================================================

            if (selectedService.getEstimatedDays() > 0) {

                shipment.setEstimatedDeliveryDate(
                        shipmentDate.plusDays(
                                selectedService
                                        .getEstimatedDays()
                        )
                );
            }


            // =================================================
            // ACTUAL DELIVERY DATE
            // =================================================
            //
            // Only populated when deliveryStatus is DELIVERED.
            //
            // CREATED / IN_TRANSIT -> NULL
            // DELIVERED             -> supplied deliveryDate
            //
            // =================================================

            shipment.setActualDeliveryDate(
                    shipmentStatus ==
                            ShipmentStatus.DELIVERED
                            ? deliveryDate
                            : null
            );


            // =================================================
            // SAVE SHIPMENT
            // =================================================

            shipmentDAO.save(
                    shipment
            );


            // =================================================
            // GENERATE SHIPMENT NUMBER
            // =================================================

            shipment.setShipmentNumber(
                    String.format(
                            "SH-%06d",
                            shipment.getId()
                    )
            );


            shipmentDAO.update(
                    shipment
            );


            // =================================================
            // LINK PACKAGES TO SHIPMENT
            // =================================================

            shipmentDAO.addPackages(
                    shipment.getId(),
                    packageIdsToShip
            );


            // =================================================
            // UPDATE SHIPPED QUANTITIES
            // =================================================

            if (!shippedDeltas.isEmpty()) {

                salesOrderItemDAO
                        .incrementShippedQuantities(
                                shippedDeltas
                        );

                // Shipping consumes physical stock and releases the reservation.
                // Keep this inside the Auto Pack & Ship transaction so a failure
                // rolls back shipment, package, fulfillment counters, and stock.
                for (Map.Entry<Integer, Integer> entry : shippedDeltas.entrySet()) {
                    SalesOrderItem line = lineLookup.get(entry.getKey());
                    if (line == null || line.getItem() == null) {
                        throw new IllegalStateException(
                                "Could not resolve the item for Sales Order line "
                                        + entry.getKey());
                    }

                    if ("GOODS".equalsIgnoreCase(line.getItem().getItemType())
                            && line.getItem().isTrackInventory()) {
                        inventoryService.shipReservedStock(
                                line.getItem(),
                                entry.getValue());
                    }
                }
            }


            // =================================================
            // MARK PACKAGES SHIPPED
            // =================================================

            packageDAO.updateStatusBatch(
                    packageIdsToShip,
                    PackageStatus.SHIPPED
            );


            // =================================================
            // CALCULATE FINAL SALES ORDER STATUS
            // =================================================

            boolean allShipped =
                    true;

            boolean anyShipped =
                    false;


            for (SalesOrderItem line :
                    orderLines) {

                int effectivePacked =
                        line.getPackedQuantity()
                                + packedDeltas.getOrDefault(
                                line.getId(),
                                0
                        );


                int effectiveShipped =
                        line.getShippedQuantity()
                                + shippedDeltas.getOrDefault(
                                line.getId(),
                                0
                        );


                // ---------------------------------------------
                // Only physical goods participate in shipment
                // completion.
                // ---------------------------------------------

                if (line.getItem() != null
                        && "GOODS".equalsIgnoreCase(
                        line.getItem()
                                .getItemType())
                        && line.getItem()
                        .isTrackInventory()) {


                    if (effectiveShipped > 0) {

                        anyShipped = true;
                    }


                    if (effectiveShipped <
                            line.getQuantity()) {

                        allShipped = false;
                    }
                }


                /*
                 * Keep in-memory line state consistent with the
                 * values that were written to the database.
                 */
                line.setPackedQuantity(
                        effectivePacked
                );


                line.setShippedQuantity(
                        effectiveShipped
                );
            }


            // =================================================
            // UPDATE SALES ORDER STATUS
            // =================================================

            if (allShipped) {

                salesOrder.setStatus(
                        SalesOrderStatus.SHIPPED
                );


                salesOrderDAO.update(
                        salesOrder
                );

            } else if (anyShipped) {

                salesOrder.setStatus(
                        SalesOrderStatus.PARTIALLY_SHIPPED
                );


                salesOrderDAO.update(
                        salesOrder
                );
            }


            // =================================================
            // RESULT PACKAGE
            // =================================================

            Package resultPackage =
                    createdPackage;


            if (resultPackage != null) {

                resultPackage.setStatus(
                        PackageStatus.SHIPPED
                );
            }


            if (resultPackage == null) {

                for (Package pkg :
                        allPackages) {

                    if (packageIdsToShip.contains(
                            pkg.getId())) {

                        pkg.setStatus(
                                PackageStatus.SHIPPED
                        );


                        resultPackage =
                                pkg;


                        break;
                    }
                }
            }


            // =================================================
            // RETURN
            // =================================================

            return new AutoPackShipResult(
                    resultPackage,
                    shipment
            );
        });
    }


    // =========================================================
    // REQUEST VALIDATION
    // =========================================================

    private void validateRequest(
            int salesOrderId,
            LocalDate shipmentDate,
            String deliveryStatus,
            LocalDate deliveryDate) {

        if (salesOrderId <= 0) {

            throw new IllegalArgumentException(
                    "A valid Sales Order ID is required"
            );
        }


        if (shipmentDate == null) {

            throw new IllegalArgumentException(
                    "Shipment date is required"
            );
        }


        if (deliveryStatus == null
                || deliveryStatus.isBlank()) {

            throw new IllegalArgumentException(
                    "Delivery status is required"
            );
        }


        String normalizedStatus =
                deliveryStatus
                        .trim()
                        .toUpperCase();


        /*
         * Delivery date is mandatory only for DELIVERED.
         */
        if ("DELIVERED".equals(
                normalizedStatus)) {

            if (deliveryDate == null) {

                throw new IllegalArgumentException(
                        "Delivery date is required when "
                                + "delivery status is DELIVERED"
                );
            }


            if (deliveryDate.isBefore(
                    shipmentDate
            )) {

                throw new IllegalArgumentException(
                        "Delivery date cannot be before "
                                + "shipment date"
                );
            }


        } else if (deliveryDate != null) {

            throw new IllegalArgumentException(
                    "Delivery date should only be provided "
                            + "when delivery status is DELIVERED"
            );
        }
    }


    // =========================================================
    // FIND DEFAULT CARRIER SERVICE
    // =========================================================

    private CarrierService findDefaultCarrierService() {

        List<Carrier> carriers =
                carrierDAO.findAll();


        if (carriers == null
                || carriers.isEmpty()) {

            return null;
        }


        for (Carrier carrier :
                carriers) {

            if (carrier == null) {
                continue;
            }


            if (carrier.getStatus() != null
                    && "INACTIVE".equalsIgnoreCase(
                    carrier.getStatus().name())) {

                continue;
            }


            List<CarrierService> services =
                    carrierServiceDAO
                            .findByCarrier(
                                    carrier.getId()
                            );


            if (services != null
                    && !services.isEmpty()) {

                return services.get(0);
            }
        }


        return null;
    }


    // =========================================================
    // RESULT
    // =========================================================

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

