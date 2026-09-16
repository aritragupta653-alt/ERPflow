package com.erpflow.service;

import com.erpflow.dao.CustomerDAO;
import com.erpflow.model.Customer;

import java.util.List;

public class CustomerService {

    private final CustomerDAO customerDAO = new CustomerDAO();

    public void addCustomer(Customer customer) {
        customerDAO.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.findAll();
    }

    public Customer getCustomerById(int id) {
        return customerDAO.findById(id);
    }

    public void updateCustomer(Customer customer) {
        customerDAO.update(customer);
    }

    public void deleteCustomer(int id) {
        customerDAO.delete(id);
    }
}