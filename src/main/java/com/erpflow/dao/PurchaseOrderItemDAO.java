package com.erpflow.dao;

import com.erpflow.model.PurchaseOrder;
import com.erpflow.model.PurchaseOrderItem;
import com.erpflow.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class PurchaseOrderItemDAO {


    // SAVE PURCHASE ORDER ITEM

    public void save(
            PurchaseOrderItem purchaseOrderItem
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
                    purchaseOrderItem
            );

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {

                transaction.rollback();
            }

            throw e;
        }
    }


    // GET ITEMS OF A PURCHASE ORDER

    public List<PurchaseOrderItem> findByPurchaseOrder(
            PurchaseOrder purchaseOrder
    ) {

        try (
                Session session =
                        HibernateUtil
                                .getSessionFactory()
                                .openSession()
        ) {

            return session
                    .createQuery(
                            "FROM PurchaseOrderItem " +
                            "WHERE purchaseOrder = :purchaseOrder",
                            PurchaseOrderItem.class
                    )
                    .setParameter(
                            "purchaseOrder",
                            purchaseOrder
                    )
                    .list();
        }
    }
}