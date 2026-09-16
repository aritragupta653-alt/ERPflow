package com.erpflow.dao;

import com.erpflow.model.PurchaseOrder;
import com.erpflow.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class PurchaseOrderDAO {


    // SAVE PURCHASE ORDER

    public void save(
            PurchaseOrder purchaseOrder
    ) {

        Transaction transaction = null;

        try (
                Session session =
                        HibernateUtil
                                .getSessionFactory()
                                .openSession()
        ) {

            transaction =
                    session.beginTransaction();

            session.persist(
                    purchaseOrder
            );

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {

                transaction.rollback();
            }

            throw e;
        }
    }


    // GET ALL PURCHASE ORDERS

    public List<PurchaseOrder> findAll() {

        try (
                Session session =
                        HibernateUtil
                                .getSessionFactory()
                                .openSession()
        ) {

            return session
                    .createQuery(
                            "FROM PurchaseOrder " +
                            "ORDER BY orderDate DESC",
                            PurchaseOrder.class
                    )
                    .list();
        }
    }


    // GET PURCHASE ORDER BY ID

    public PurchaseOrder findById(
            int id
    ) {

        try (
                Session session =
                        HibernateUtil
                                .getSessionFactory()
                                .openSession()
        ) {

            return session.get(
                    PurchaseOrder.class,
                    id
            );
        }
    }


    // UPDATE PURCHASE ORDER

    public void update(
            PurchaseOrder purchaseOrder
    ) {

        Transaction transaction = null;

        try (
                Session session =
                        HibernateUtil
                                .getSessionFactory()
                                .openSession()
        ) {

            transaction =
                    session.beginTransaction();

            session.merge(
                    purchaseOrder
            );

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {

                transaction.rollback();
            }

            throw e;
        }
    }
}