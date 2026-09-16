package com.erpflow.service;

import com.erpflow.dao.PackageDAO;
import com.erpflow.dao.PackageItemDAO;
import com.erpflow.dao.SalesOrderDAO;
import com.erpflow.model.Package;
import com.erpflow.model.PackageItem;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.Shipment;
import java.time.LocalDateTime;

import java.util.List;

public class ShippingService {

    private final PackageDAO packageDAO =
            new PackageDAO();

    private final PackageItemDAO packageItemDAO =
            new PackageItemDAO();

    private final SalesOrderDAO salesOrderDAO =
            new SalesOrderDAO();

    private final InventoryService inventoryService =
            new InventoryService();
    private final ShipmentService shipmentService =
        new ShipmentService();


    // SHIP PACKAGE

    public void shipPackage(int packageId) {

        Package packageEntity =
                packageDAO.findById(
                        packageId
                );


        if (packageEntity == null) {

            throw new RuntimeException(
                    "Package not found"
            );
        }


        // Only PACKED packages can be shipped

        if (!"PACKED".equals(
                packageEntity.getStatus())) {

            throw new RuntimeException(
                    "Only PACKED packages can be shipped"
            );
        }


        SalesOrder salesOrder =
                packageEntity.getSalesOrder();


        if (salesOrder == null) {

            throw new RuntimeException(
                    "Sales Order not found"
            );
        }


        // Get package items

        List<PackageItem> packageItems =
                packageItemDAO.findByPackage(
                        packageEntity
                );


        if (packageItems == null ||
                packageItems.isEmpty()) {

            throw new RuntimeException(
                    "Package contains no items"
            );
        }


        // Ship every item

        for (PackageItem packageItem :
                packageItems) {

            inventoryService.shipReservedStock(
                    packageItem.getItem(),
                    packageItem.getQuantity()
            );
        }


        // Package is now shipped

        packageEntity.setStatus(
                "SHIPPED"
        );

        packageDAO.update(
                packageEntity
        );


        // Sales Order is now shipped

        salesOrder.setStatus(
                "SHIPPED"
        );

        salesOrderDAO.update(
                salesOrder
        );
        Shipment shipment = new Shipment();

shipment.setPackageEntity(packageEntity);
shipment.setShipmentDate(LocalDateTime.now());
shipment.setStatus("SHIPPED");

shipmentService.createShipment(shipment);
    }
}