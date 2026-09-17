package com.erpflow.service;

import com.erpflow.dao.CustomerDAO;
import com.erpflow.model.Customer;

import java.util.List;

public class CustomerService {

    private final CustomerDAO customerDAO =
            new CustomerDAO();


    public void createCustomer(
            Customer customer) {

        if (customer == null) {

            throw new RuntimeException(
                    "Customer cannot be null"
            );
        }

        if (customer.getName() == null ||
                customer.getName().isBlank()) {

            throw new RuntimeException(
                    "Customer name is required"
            );
        }

        if (customer.getEmail() == null ||
                customer.getEmail().isBlank()) {

            throw new RuntimeException(
                    "Customer email is required"
            );
        }

        if (customer.getPhone() == null ||
                customer.getPhone().isBlank()) {

            throw new RuntimeException(
                    "Customer phone is required"
            );
        }

        if (customer.getAddress() == null ||
                customer.getAddress().isBlank()) {

            throw new RuntimeException(
                    "Customer address is required"
            );
        }

        if (customer.getStatus() == null) {

            customer.setStatus("ACTIVE");
        }

        customerDAO.save(customer);
    }


    public List<Customer> getAllCustomers() {

        return customerDAO.findAll();
    }


    public Customer getCustomerById(int id) {

        return customerDAO.findById(id);
    }


    public void updateCustomer(
            Customer customer) {

        if (customer == null) {

            throw new RuntimeException(
                    "Customer cannot be null"
            );
        }

        Customer existing =
                customerDAO.findById(
                        customer.getId()
                );

        if (existing == null) {

            throw new RuntimeException(
                    "Customer not found"
            );
        }

        customerDAO.update(customer);
    }


    public void deleteCustomer(int id) {

        Customer existing =
                customerDAO.findById(id);

        if (existing == null) {

            throw new RuntimeException(
                    "Customer not found"
            );
        }

        customerDAO.delete(id);
    }
}