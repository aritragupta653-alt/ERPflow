package com.erpflow.controller;

import com.erpflow.service.ShippingService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/shipping")
public class ShippingServlet extends HttpServlet {

    private final ShippingService shippingService =
            new ShippingService();


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {


        String action =
                request.getParameter("action");


        if ("ship".equals(action)) {

            int packageId =
                    Integer.parseInt(
                            request.getParameter(
                                    "packageId"
                            )
                    );


            shippingService.shipPackage(
                    packageId
            );


            response.sendRedirect(
                    request.getContextPath()
                            + "/packages?action=view&id="
                            + packageId
            );

            return;
        }


        response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Invalid shipping action"
        );
    }
}