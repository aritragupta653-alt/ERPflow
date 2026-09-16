package com.erpflow.dao;

import com.erpflow.model.Inventory;
import com.erpflow.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class InventoryDAO {

    public void save(Inventory inventory) {

        Transaction transaction = null;

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            transaction =
                    session.beginTransaction();

            session.persist(inventory);

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            throw e;
        }
    }
    public Inventory findByItemId(int itemId) {

    try (Session session =
                 HibernateUtil
                         .getSessionFactory()
                         .openSession()) {

        return session.createQuery(
                        "FROM Inventory WHERE item.id = :itemId",
                        Inventory.class
                )
                .setParameter("itemId", itemId)
                .uniqueResult();
    }

}

public List<Inventory> findAll() {

    try (Session session =
                 HibernateUtil
                         .getSessionFactory()
                         .openSession()) {

        return session.createQuery(
                        "FROM Inventory",
                        Inventory.class
                )
                .getResultList();
    }
}
public void update(Inventory inventory) {

    Transaction transaction = null;

    try (Session session =
                 HibernateUtil
                         .getSessionFactory()
                         .openSession()) {

        transaction = session.beginTransaction();

        session.merge(inventory);

        transaction.commit();

    } catch (Exception e) {

        if (transaction != null
                && transaction.isActive()) {

            transaction.rollback();
        }

        throw e;
    }
}
public void deleteByItemId(int itemId) {

    Transaction transaction = null;

    try (
            Session session =
                    HibernateUtil
                            .getSessionFactory()
                            .openSession()
    ) {

        transaction =
                session.beginTransaction();

        session.createMutationQuery(
                "DELETE FROM Inventory " +
                "WHERE item.id = :itemId"
        )
        .setParameter(
                "itemId",
                itemId
        )
        .executeUpdate();

        transaction.commit();

    } catch (Exception e) {

        if (transaction != null) {

            transaction.rollback();
        }

        throw e;
    }
}
}