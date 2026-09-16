package com.erpflow.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static final SessionFactory sessionFactory;

    static {
        try {
            sessionFactory = new Configuration()
                    .configure()
                    .buildSessionFactory();

        } catch (Exception e) {

            System.err.println(
                    "SessionFactory creation failed."
            );

            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory() {

        return sessionFactory;
    }

    public static void shutdown() {

        sessionFactory.close();
    }
}