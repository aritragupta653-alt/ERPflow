package com.erpflow.dao;

import com.erpflow.model.SalesOrder;
import com.erpflow.model.SalesOrderItem;
import com.erpflow.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class SalesOrderItemDAO {

    public void save(SalesOrderItem salesOrderItem) {

        Transaction transaction = null;

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            transaction = session.beginTransaction();

            session.persist(salesOrderItem);

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw e;
        }
    }

    public List<SalesOrderItem> findBySalesOrder(SalesOrder salesOrder) {

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            return session.createQuery(
                    "FROM SalesOrderItem WHERE salesOrder = :salesOrder",
                    SalesOrderItem.class
            )
            .setParameter("salesOrder", salesOrder)
            .getResultList();
        }
    }
}