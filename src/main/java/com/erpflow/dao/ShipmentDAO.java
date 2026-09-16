package com.erpflow.dao;

import com.erpflow.model.Shipment;
import com.erpflow.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class ShipmentDAO {

    public void save(Shipment shipment) {

        Transaction transaction = null;

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            transaction = session.beginTransaction();

            session.persist(shipment);

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null &&
                    transaction.isActive()) {

                transaction.rollback();
            }

            throw e;
        }
    }

    public List<Shipment> findAll() {

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            return session.createQuery(
                    "FROM Shipment ORDER BY shipmentDate DESC",
                    Shipment.class
            ).getResultList();
        }
    }

    public Shipment findById(int id) {

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            return session.get(Shipment.class, id);
        }
    }
}