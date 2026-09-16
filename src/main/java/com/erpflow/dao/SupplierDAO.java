package com.erpflow.dao;

import com.erpflow.model.Supplier;
import com.erpflow.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class SupplierDAO {


    // SAVE SUPPLIER

    public void save(Supplier supplier) {

        Transaction transaction = null;

        try (
                Session session =
                        HibernateUtil
                                .getSessionFactory()
                                .openSession()
        ) {

            transaction =
                    session.beginTransaction();

            session.persist(supplier);

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {

                transaction.rollback();
            }

            throw e;
        }
    }


    // GET ALL SUPPLIERS

    public List<Supplier> findAll() {

        try (
                Session session =
                        HibernateUtil
                                .getSessionFactory()
                                .openSession()
        ) {

            return session
                    .createQuery(
                            "FROM Supplier",
                            Supplier.class
                    )
                    .list();
        }
    }


    // FIND SUPPLIER BY ID

    public Supplier findById(int id) {

        try (
                Session session =
                        HibernateUtil
                                .getSessionFactory()
                                .openSession()
        ) {

            return session.get(
                    Supplier.class,
                    id
            );
        }
    }


    // UPDATE SUPPLIER

    public void update(Supplier supplier) {

        Transaction transaction = null;

        try (
                Session session =
                        HibernateUtil
                                .getSessionFactory()
                                .openSession()
        ) {

            transaction =
                    session.beginTransaction();

            session.merge(supplier);

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {

                transaction.rollback();
            }

            throw e;
        }
    }


    // DELETE SUPPLIER

    public void delete(int id) {

        Transaction transaction = null;

        try (
                Session session =
                        HibernateUtil
                                .getSessionFactory()
                                .openSession()
        ) {

            transaction =
                    session.beginTransaction();

            Supplier supplier =
                    session.get(
                            Supplier.class,
                            id
                    );

            if (supplier != null) {

                session.remove(supplier);
            }

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {

                transaction.rollback();
            }

            throw e;
        }
    }
}