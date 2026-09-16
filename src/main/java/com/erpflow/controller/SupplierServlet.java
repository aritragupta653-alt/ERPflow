package com.erpflow.controller;

import com.erpflow.model.Supplier;
import com.erpflow.service.SupplierService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;


@WebServlet("/suppliers")
public class SupplierServlet extends HttpServlet {

    private final SupplierService supplierService =
            new SupplierService();


    // DISPLAY SUPPLIERS

    @Override
    
protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
) throws ServletException, IOException {

    String action =
            request.getParameter("action");


    // EDIT SUPPLIER
    if ("edit".equals(action)) {

        int id = Integer.parseInt(
                request.getParameter("id")
        );

        Supplier supplier =
                supplierService.getSupplierById(id);

        request.setAttribute(
                "supplier",
                supplier
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/editSupplier.jsp"
        ).forward(request, response);

        return;
    }


    // DISPLAY ALL SUPPLIERS

    List<Supplier> suppliers =
            supplierService.getAllSuppliers();

    request.setAttribute(
            "suppliers",
            suppliers
    );

    request.getRequestDispatcher(
            "/WEB-INF/views/suppliers.jsp"
    ).forward(request, response);
}


    // ADD SUPPLIER

    @Override
protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response
) throws ServletException, IOException {

    String action =
            request.getParameter("action");


    // DELETE SUPPLIER
    if ("delete".equals(action)) {

        int id = Integer.parseInt(
                request.getParameter("id")
        );

        supplierService.deleteSupplier(id);

        response.sendRedirect(
                request.getContextPath()
                        + "/suppliers"
        );

        return;
    }


    // UPDATE SUPPLIER
    if ("update".equals(action)) {

        int id = Integer.parseInt(
                request.getParameter("id")
        );

        Supplier supplier = new Supplier();

        supplier.setId(id);

        supplier.setName(
                request.getParameter("name")
        );

        supplier.setContactPerson(
                request.getParameter("contactPerson")
        );

        supplier.setPhone(
                request.getParameter("phone")
        );

        supplier.setEmail(
                request.getParameter("email")
        );

        supplier.setAddress(
                request.getParameter("address")
        );


        supplierService.updateSupplier(
                supplier
        );

        response.sendRedirect(
                request.getContextPath()
                        + "/suppliers"
        );

        return;
    }


    // ADD SUPPLIER

    Supplier supplier = new Supplier();

    supplier.setName(
            request.getParameter("name")
    );

    supplier.setContactPerson(
            request.getParameter("contactPerson")
    );

    supplier.setPhone(
            request.getParameter("phone")
    );

    supplier.setEmail(
            request.getParameter("email")
    );

    supplier.setAddress(
            request.getParameter("address")
    );


    supplierService.addSupplier(supplier);


    response.sendRedirect(
            request.getContextPath()
                    + "/suppliers"
    );
}
}