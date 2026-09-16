package com.erpflow.dao;

import com.erpflow.model.Customer;
import com.erpflow.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class CustomerDAO {

    public void save(Customer customer) {
        Transaction transaction = null;

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            transaction = session.beginTransaction();

            session.persist(customer);

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw e;
        }
    }

    public List<Customer> findAll() {
        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            return session.createQuery(
                    "FROM Customer c WHERE c.status = 'ACTIVE'",
                    Customer.class
            ).getResultList();
        }
    }

    public Customer findById(int id) {
        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            return session.get(Customer.class, id);
        }
    }

    public void update(Customer customer) {
        Transaction transaction = null;

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            transaction = session.beginTransaction();

            session.merge(customer);

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw e;
        }
    }

    public void delete(int id) {
        Transaction transaction = null;

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            transaction = session.beginTransaction();

            Customer customer = session.get(Customer.class, id);

            if (customer != null) {
                customer.setStatus("INACTIVE");
                session.merge(customer);
            }

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw e;
        }
    }
}