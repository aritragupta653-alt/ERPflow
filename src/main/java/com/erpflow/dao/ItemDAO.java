package com.erpflow.dao;
import java.util.List;
import com.erpflow.model.Item;
import com.erpflow.util.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class ItemDAO {

    public void save(Item item) {

        Transaction transaction = null;

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            transaction =
                    session.beginTransaction();

            session.persist(item);

            transaction.commit();

        } catch (Exception e) {

            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }

            throw e;
        }
    }

    public List<Item> findAll() {

    try (Session session =
                 HibernateUtil
                         .getSessionFactory()
                         .openSession()) {
        

        return session
                .createQuery(
                        "FROM Item i "+"WHERE i.status ='ACTIVE'",
                        Item.class
                )
                .getResultList();
    }
}
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

        Item item =
                session.get(
                        Item.class,
                        id
                );

        if (item != null) {

            item.setStatus(
                    "INACTIVE"
            );

            session.merge(item);
        }

        transaction.commit();

    } catch (Exception e) {

        if (transaction != null) {

            transaction.rollback();
        }

        throw e;
    }
}
public Item findById(int id) {

    try (Session session =
                 HibernateUtil
                         .getSessionFactory()
                         .openSession()) {

        return session.get(
                Item.class,
                id
        );
    }
}
public void update(Item item) {

    Transaction transaction = null;

    try (Session session =
                 HibernateUtil
                         .getSessionFactory()
                         .openSession()) {

        transaction = session.beginTransaction();

        session.merge(item);

        transaction.commit();

    } catch (Exception e) {

        if (transaction != null
                && transaction.isActive()) {

            transaction.rollback();
        }

        throw e;
    }
}
}