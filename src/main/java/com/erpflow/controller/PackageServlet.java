package com.erpflow.controller;

import com.erpflow.model.Package;
import com.erpflow.model.PackageItem;
import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;
import com.erpflow.service.PackageService;
import com.erpflow.service.SalesOrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/packages")
public class PackageServlet extends HttpServlet {

    private final PackageService packageService =
            new PackageService();

    private final SalesOrderService salesOrderService =
            new SalesOrderService();


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String action =
                request.getParameter("action");


        // VIEW PACKAGE

        if ("view".equals(action)) {

            int packageId =
                    Integer.parseInt(
                            request.getParameter("id")
                    );


            Package packageEntity =
                    packageService.getPackageById(
                            packageId
                    );


            if (packageEntity == null) {

                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Package not found"
                );

                return;
            }


            List<PackageItem> packageItems =
                    packageService.getPackageItems(
                            packageEntity
                    );


            request.setAttribute(
                    "packageEntity",
                    packageEntity
            );

            request.setAttribute(
                    "packageItems",
                    packageItems
            );


            request.getRequestDispatcher(
                    "/WEB-INF/views/packageDetails.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }


        // CREATE PACKAGE PAGE

        if ("create".equals(action)) {

            int salesOrderId =
                    Integer.parseInt(
                            request.getParameter(
                                    "salesOrderId"
                            )
                    );


            SalesOrder salesOrder =
                    salesOrderService.getSalesOrderById(
                            salesOrderId
                    );


            if (salesOrder == null) {

                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Sales Order not found"
                );

                return;
            }


            List<SalesOrderItem> salesOrderItems =
                    salesOrderService.getSalesOrderItems(
                            salesOrder
                    );


            request.setAttribute(
                    "salesOrder",
                    salesOrder
            );

            request.setAttribute(
                    "salesOrderItems",
                    salesOrderItems
            );


            request.getRequestDispatcher(
                    "/WEB-INF/views/createPackage.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }


        // LIST PACKAGES

        List<Package> packages =
                packageService.getAllPackages();


        request.setAttribute(
                "packages",
                packages
        );


        request.getRequestDispatcher(
                "/WEB-INF/views/packages.jsp"
        ).forward(
                request,
                response
        );
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String action =
                request.getParameter("action");


        // CREATE PACKAGE

        if ("create".equals(action)) {

            int salesOrderId =
                    Integer.parseInt(
                            request.getParameter(
                                    "salesOrderId"
                            )
                    );


            SalesOrder salesOrder =
                    salesOrderService.getSalesOrderById(
                            salesOrderId
                    );


            if (salesOrder == null) {

                throw new RuntimeException(
                        "Sales Order not found"
                );
            }


            // Get Sales Order Items

            List<SalesOrderItem> salesOrderItems =
                    salesOrderService.getSalesOrderItems(
                            salesOrder
                    );


            String[] itemIds =
                    request.getParameterValues(
                            "itemId"
                    );

            String[] quantities =
                    request.getParameterValues(
                            "quantity"
                    );


            if (itemIds == null ||
                    quantities == null) {

                throw new RuntimeException(
                        "No package items selected"
                );
            }


            List<PackageItem> packageItems =
                    new ArrayList<>();


            // Build Package Items

            for (int i = 0;
                 i < itemIds.length;
                 i++) {

                int quantity =
                        Integer.parseInt(
                                quantities[i]
                        );


                // Ignore zero quantities

                if (quantity <= 0) {
                    continue;
                }


                int itemId =
                        Integer.parseInt(
                                itemIds[i]
                        );


                SalesOrderItem matchingOrderItem =
                        null;


                // Find matching Sales Order Item

                for (SalesOrderItem orderItem :
                        salesOrderItems) {

                    if (orderItem.getItem().getId()
                            == itemId) {

                        matchingOrderItem =
                                orderItem;

                        break;
                    }
                }


                if (matchingOrderItem == null) {

                    throw new RuntimeException(
                            "Invalid item in package"
                    );
                }


                // Create Package Item

                PackageItem packageItem =
                        new PackageItem();


                packageItem.setItem(
                        matchingOrderItem.getItem()
                );


                packageItem.setQuantity(
                        quantity
                );


                packageItems.add(
                        packageItem
                );
            }


            if (packageItems.isEmpty()) {

                throw new RuntimeException(
                        "Package must contain at least one item"
                );
            }


            // Weight

            double weight =
                    Double.parseDouble(
                            request.getParameter(
                                    "weight"
                            )
                    );


            // Create Package

            packageService.createPackage(
                    salesOrder,
                    packageItems,
                    weight
            );


            // Redirect to Packages

            response.sendRedirect(
                    request.getContextPath()
                            + "/packages"
            );

            return;
        }
    }
}