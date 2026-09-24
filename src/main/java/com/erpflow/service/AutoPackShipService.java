
package com.erpflow.service;

import com.erpflow.dao.CarrierDAO;
import com.erpflow.dao.CarrierServiceDAO;
import com.erpflow.dao.PackageDAO;
import com.erpflow.dao.SalesOrderDAO;
import com.erpflow.dao.SalesOrderItemDAO;

import com.erpflow.model.Carrier;
import com.erpflow.model.CarrierService;
import com.erpflow.model.Item;
import com.erpflow.model.Package;
import com.erpflow.model.PackageItem;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;
import com.erpflow.model.Shipment;
import com.erpflow.model.enums.ShipmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.erpflow.model.enums.CartonSize;


public class AutoPackShipService {

    // =====================================================
    // DEFAULT PACKING AND DISPATCH VALUES
    // =====================================================

    private static final double DEFAULT_LENGTH = 0;
    private static final double DEFAULT_WIDTH = 0;
    private static final double DEFAULT_HEIGHT = 0;

    // Assumed weight when item-level shipping weight is unavailable.
    private static final double DEFAULT_WEIGHT_PER_UNIT = 1.0;

    // Replace this with your actual warehouse/base address.
    private static final String DEFAULT_DISPATCH_ADDRESS =
            "ERPFlow Warehouse, Chennai, Tamil Nadu, India";


    // =====================================================
    // DAOS AND SERVICES
    // =====================================================

    private final SalesOrderDAO salesOrderDAO =
            new SalesOrderDAO();

    private final SalesOrderItemDAO salesOrderItemDAO =
            new SalesOrderItemDAO();

    private final PackageDAO packageDAO =
            new PackageDAO();

    private final PackageService packageService =
            new PackageService();

    private final ShippingService shippingService =
            new ShippingService();

    private final CarrierDAO carrierDAO =
            new CarrierDAO();

    private final CarrierServiceDAO carrierServiceDAO =
            new CarrierServiceDAO();
    private final CartonSelectionService cartonSelectionService =
        new CartonSelectionService();


    // =====================================================
    // PACK AND SHIP
    // =====================================================

    public AutoPackShipResult packAndShip(
            int salesOrderId,
            LocalDate shipmentDate,
            String deliveryStatus) {

        // -------------------------------------------------
        // 1. Validate request
        // -------------------------------------------------

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

        String normalizedStatus = deliveryStatus.trim().toUpperCase();
        ShipmentStatus status = ShipmentStatus.valueOf(normalizedStatus);

        /*
         * The shipment creation process ships inventory
         * immediately. Therefore, allow only statuses that
         * represent a shipment that has actually been created
         * or dispatched.
         *
         * DELIVERED is not accepted here because delivery
         * confirmation should be a separate operation.
         */
       /*if ( !normalizedStatus.equals("IN_TRANSIT") || !normalizedStatus.equals("CREATED") || !normalizedStatus.equals("DELIVERED")) {

            throw new RuntimeException(
                    "Delivery status must be CREATED or IN_TRANSIT OR DELIVERED"
            );
        }*/


        // -------------------------------------------------
        // 2. Load Sales Order
        // -------------------------------------------------

        SalesOrder salesOrder =
                salesOrderDAO.findById(salesOrderId);
        

        if (salesOrder == null) {
            throw new RuntimeException(
                    "Sales Order not found"
            );
        }

        if (salesOrder.getStatus().name() == null ||
                !"CREATED".equalsIgnoreCase(
                        salesOrder.getStatus().name())) {

            throw new RuntimeException(
                    "Only CREATED Sales Orders can use Pack & Ship"
            );
        }


        // -------------------------------------------------
        // 3. Prevent duplicate package creation
        // -------------------------------------------------

        List<Package> existingPackages =
                packageDAO.findBySalesOrder(salesOrderId);

        if (existingPackages != null &&
                !existingPackages.isEmpty()) {

            throw new RuntimeException(
                    "This Sales Order already has packages. "
                    + "Use the existing package and shipment workflow."
            );
        }


        // -------------------------------------------------
        // 4. Load Sales Order items
        // -------------------------------------------------

        List<SalesOrderItem> orderLines =
                salesOrderItemDAO.findBySalesOrder(salesOrder);
        

        if (orderLines == null || orderLines.isEmpty()) {
            throw new RuntimeException(
                    "Sales Order contains no items"
            );
        }
        //calculate volume for package dimensions
        double requiredVolume = 0;

for (SalesOrderItem orderLine : orderLines) {

    Item item = orderLine.getItem();

    if (!"GOODS".equalsIgnoreCase(item.getItemType())
            || !item.isTrackInventory()) {
        continue;
    }
    
    double itemVolume =
            item.getLength()
                    * item.getWidth()
                    * item.getHeight();

    requiredVolume += itemVolume * orderLine.getQuantity();
}
CartonSize selectedCarton =
        cartonSelectionService.selectCarton(requiredVolume);



        // -------------------------------------------------
        // 5. Build package items automatically
        // -------------------------------------------------

        List<PackageItem> packageItems =
                new ArrayList<>();

        int totalUnits = 0;

        for (SalesOrderItem orderLine : orderLines) {

            if (orderLine == null ||
                    orderLine.getItem() == null) {

                throw new RuntimeException(
                        "A Sales Order line has no associated item"
                );
            }

            Item item = orderLine.getItem();

            /*
             * Only inventory-tracked goods are physically
             * packed. Service and non-tracked items are skipped.
             */
            if (!"GOODS".equalsIgnoreCase(item.getItemType())) {

                continue;
            }

            int quantity = orderLine.getQuantity();

            if (quantity <= 0) {
                throw new RuntimeException(
                        "Invalid quantity for item: " + item.getName()
                );
            }

            PackageItem packageItem = new PackageItem();

            packageItem.setItem(item);
            packageItem.setQuantity(quantity);
            packageItem.setSalesOrderItemId(orderLine.getId());

            packageItems.add(packageItem);

            totalUnits += quantity;
        }

        if (packageItems.isEmpty()) {
            throw new RuntimeException(
                    "This Sales Order has no inventory-tracked goods to ship"
            );
        }


        // -------------------------------------------------
        // 6. Select a configured carrier service
        // -------------------------------------------------

        CarrierService selectedService =
                findDefaultCarrierService();

        if (selectedService == null) {
            throw new RuntimeException(
                    "No carrier service is configured. "
                    + "Configure a carrier and carrier service first."
            );
        }


        // -------------------------------------------------
        // 7. Create one package
        // -------------------------------------------------

        double estimatedWeight =
                totalUnits * DEFAULT_WEIGHT_PER_UNIT;

        Package createdPackage =
                packageService.createPackage(
                        salesOrder,
                        packageItems,
                        estimatedWeight,
                        selectedCarton.getLength(),
                        selectedCarton.getWidth(),
                        selectedCarton.getHeight()
                );


        // -------------------------------------------------
        // 8. Prepare shipment details
        // -------------------------------------------------

        Shipment shipment = new Shipment();

        // Store the selected shipment date at midnight.
        shipment.setShipmentDate(
                shipmentDate.atStartOfDay()
        );

        shipment.setStatus(status);

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

        /*
         * Customer.address is the address stored on the
         * Sales Order's associated customer.
         */
        if (salesOrder.getCustomer() == null ||
                salesOrder.getCustomer().getAddress() == null ||
                salesOrder.getCustomer().getAddress().trim().isEmpty()) {

            throw new RuntimeException(
                    "The customer has no shipping address. "
                    + "Update the customer details before shipping."
            );
        }

        shipment.setDestinationAddress(
                salesOrder.getCustomer().getAddress()
        );

        shipment.setNotes(
                "Created automatically using Pack & Ship"
        );


        // -------------------------------------------------
        // 9. Create shipment for the package
        // -------------------------------------------------

        Shipment createdShipment =
                shippingService.shipPackages(
                        salesOrderId,
                        List.of(createdPackage.getId()),
                        selectedService.getId(),
                        shipment
                );


        // -------------------------------------------------
        // 10. Return result
        // -------------------------------------------------

        return new AutoPackShipResult(
                createdPackage,
                createdShipment
        );
    }


    // =====================================================
    // FIND DEFAULT CARRIER SERVICE
    // =====================================================

    private CarrierService findDefaultCarrierService() {

        List<Carrier> carriers =
                carrierDAO.findAll();

        if (carriers == null || carriers.isEmpty()) {
            return null;
        }

        for (Carrier carrier : carriers) {

            if (carrier == null) {
                continue;
            }

            String carrierStatus = carrier.getStatus().name();

            if (carrierStatus != null &&
                    "INACTIVE".equalsIgnoreCase(carrierStatus)) {

                continue;
            }

            List<CarrierService> services =
                    carrierServiceDAO.findByCarrier(
                            carrier.getId()
                    );

            if (services == null || services.isEmpty()) {
                continue;
            }

            for (CarrierService service : services) {

                if (service != null) {
                    return service;
                }
            }
        }

        return null;
    }


    // =====================================================
    // RESULT CLASS
    // =====================================================

    public static class AutoPackShipResult {

        private final Package packageEntity;
        private final Shipment shipment;

        public AutoPackShipResult(
                Package packageEntity,
                Shipment shipment) {

            this.packageEntity = packageEntity;
            this.shipment = shipment;
        }

        public Package getPackageEntity() {
            return packageEntity;
        }

        public Shipment getShipment() {
            return shipment;
        }
    }
}