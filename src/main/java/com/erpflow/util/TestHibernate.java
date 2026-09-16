package com.erpflow.util;

import org.hibernate.SessionFactory;

public class TestHibernate {

    public static void main(String[] args) {

        try {

            SessionFactory sessionFactory =
                    HibernateUtil.getSessionFactory();

            System.out.println(
                    "Hibernate connected successfully!"
            );

            sessionFactory.close();

        } catch (Exception e) {

            e.printStackTrace();

        }
    }
}