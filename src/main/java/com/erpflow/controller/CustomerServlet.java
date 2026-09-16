package com.erpflow.controller;

import com.erpflow.model.Customer;
import com.erpflow.service.CustomerService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/customers")
public class CustomerServlet extends HttpServlet {

    private final CustomerService customerService = new CustomerService();

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // Edit customer
        if ("edit".equals(action)) {

            int id = Integer.parseInt(request.getParameter("id"));

            Customer customer = customerService.getCustomerById(id);

            request.setAttribute("customer", customer);

            request.getRequestDispatcher(
                    "/WEB-INF/views/editCustomer.jsp"
            ).forward(request, response);

            return;
        }

        // Display all customers
        request.setAttribute(
                "customers",
                customerService.getAllCustomers()
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/customers.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // Delete customer
        if ("delete".equals(action)) {

            int id = Integer.parseInt(request.getParameter("id"));

            customerService.deleteCustomer(id);

            response.sendRedirect(
                    request.getContextPath() + "/customers"
            );

            return;
        }

        // Update customer
        if ("update".equals(action)) {

            int id = Integer.parseInt(request.getParameter("id"));

            Customer customer =
                    customerService.getCustomerById(id);

            customer.setName(request.getParameter("name"));
            customer.setEmail(request.getParameter("email"));
            customer.setPhone(request.getParameter("phone"));
            customer.setAddress(request.getParameter("address"));

            customerService.updateCustomer(customer);

            response.sendRedirect(
                    request.getContextPath() + "/customers"
            );

            return;
        }

        // Create customer
        Customer customer = new Customer();

        customer.setName(request.getParameter("name"));
        customer.setEmail(request.getParameter("email"));
        customer.setPhone(request.getParameter("phone"));
        customer.setAddress(request.getParameter("address"));
        customer.setStatus("ACTIVE");

        customerService.addCustomer(customer);

        response.sendRedirect(
                request.getContextPath() + "/customers"
        );
    }
}