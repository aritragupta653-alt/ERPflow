package com.erpflow.dao;

import com.erpflow.model.Package;
import com.erpflow.model.SalesOrder;
import com.erpflow.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class PackageDAO {


    // SAVE PACKAGE

    public void save(Package packageEntity) {

        Transaction transaction = null;

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            transaction =
                    session.beginTransaction();

            session.persist(packageEntity);

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null &&
                    transaction.isActive()) {

                transaction.rollback();
            }

            throw e;
        }
    }


    // GET ALL PACKAGES

    public List<Package> findAll() {

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            return session.createQuery(
                    "FROM Package ORDER BY packageDate DESC",
                    Package.class
            ).getResultList();
        }
    }


    // GET PACKAGE BY ID

    public Package findById(int id) {

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            return session.get(
                    Package.class,
                    id
            );
        }
    }


    // GET PACKAGES FOR SALES ORDER

    public List<Package> findBySalesOrder(
            SalesOrder salesOrder) {

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            return session.createQuery(
                    "FROM Package " +
                    "WHERE salesOrder = :salesOrder " +
                    "ORDER BY packageDate DESC",
                    Package.class
            )
            .setParameter(
                    "salesOrder",
                    salesOrder
            )
            .getResultList();
        }
    }


    // UPDATE PACKAGE

    public void update(Package packageEntity) {

        Transaction transaction = null;

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            transaction =
                    session.beginTransaction();

            session.merge(packageEntity);

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null &&
                    transaction.isActive()) {

                transaction.rollback();
            }

            throw e;
        }
    }
}