package com.erpflow.dao;

import com.erpflow.model.InventoryTransaction;
import com.erpflow.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class InventoryTransactionDAO {


    // SAVE TRANSACTION

    public void save(
            InventoryTransaction inventoryTransaction
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
                    inventoryTransaction
            );

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {

                transaction.rollback();
            }

            throw e;
        }
    }


    // GET ALL TRANSACTIONS

    public List<InventoryTransaction> findAll() {

        try (
                Session session =
                        HibernateUtil
                                .getSessionFactory()
                                .openSession()
        ) {

            return session
                    .createQuery(
                            "FROM InventoryTransaction " +
                            "ORDER BY transactionDate DESC",
                            InventoryTransaction.class
                    )
                    .list();
        }
    }
}