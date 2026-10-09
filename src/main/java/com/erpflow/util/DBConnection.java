package com.erpflow.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

 
public final class DBConnection {

    private static final String URL =
            System.getProperty("erpflow.db.url",
                    "jdbc:mysql://localhost:3306/erpflow?useSSL=false&serverTimezone=Asia/Kolkata");

    private static final String USER =
            System.getProperty("erpflow.db.user", "root");

    private static final String PASSWORD =
            System.getProperty("erpflow.db.password", "");

    private static final ThreadLocal<Connection> CURRENT_TRANSACTION =
            new ThreadLocal<>();

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Connection transactionConnection = CURRENT_TRANSACTION.get();

        if (transactionConnection != null) {
            return nonClosingProxy(transactionConnection);
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    static Connection beginTransaction() throws SQLException {
        if (CURRENT_TRANSACTION.get() != null) {
            throw new IllegalStateException(
                    "A JDBC transaction is already active on this thread");
        }

        Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
        connection.setAutoCommit(false);
        CURRENT_TRANSACTION.set(connection);
        return connection;
    }

    static void endTransaction() {
        CURRENT_TRANSACTION.remove();
    }

    public static boolean isTransactionActive() {
        return CURRENT_TRANSACTION.get() != null;
    }

    private static Connection nonClosingProxy(Connection target) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {

                if ("close".equals(method.getName()) && method.getParameterCount() == 0) {
                    return null;
                }

                if ("isClosed".equals(method.getName()) && method.getParameterCount() == 0) {
                    return target.isClosed();
                }

                if ("unwrap".equals(method.getName()) && method.getParameterCount() == 1) {
                    return target.unwrap((Class<?>) args[0]);
                }

                if ("isWrapperFor".equals(method.getName()) && method.getParameterCount() == 1) {
                    return target.isWrapperFor((Class<?>) args[0]);
                }

                return method.invoke(target, args);
            }
        };

        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                handler);
    }

    public static String getUrl() {
        return URL;
    }

    public static String getUser() {
        return USER;
    }

    public static String getPassword() {
        return PASSWORD;
    }
}
