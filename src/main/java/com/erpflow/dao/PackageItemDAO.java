package com.erpflow.dao;

import com.erpflow.model.Package;
import com.erpflow.model.PackageItem;
import com.erpflow.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class PackageItemDAO {

    public void save(PackageItem packageItem) {

        Transaction transaction = null;

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            transaction = session.beginTransaction();

            session.persist(packageItem);

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw e;
        }
    }


    public List<PackageItem> findByPackage(Package packageEntity) {

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            return session.createQuery(
                    "FROM PackageItem WHERE packageEntity = :packageEntity",
                    PackageItem.class
            )
            .setParameter("packageEntity", packageEntity)
            .getResultList();
        }
    }
}