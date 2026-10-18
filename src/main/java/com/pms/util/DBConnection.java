package com.pms.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Creates database connections.
 * The URL, user and password are read from db.properties (on the classpath),
 * so nothing is hard-coded in the Java code.
 */
public class DBConnection {

    private static Properties props;

    private DBConnection() {
    }

    /** Loads db.properties only once (the first time a connection is needed). */
    private static synchronized Properties loadProperties() throws SQLException {
        if (props == null) {
            Properties p = new Properties();
            try (InputStream in = DBConnection.class.getClassLoader()
                    .getResourceAsStream("db.properties")) {
                if (in == null) {
                    throw new SQLException("db.properties file not found on the classpath.");
                }
                p.load(in);
            } catch (IOException e) {
                throw new SQLException("Could not read db.properties.", e);
            }
            props = p;
        }
        return props;
    }

    /** Returns a NEW connection. The caller must close it (use try-with-resources). */
    public static Connection getConnection() throws SQLException {
        Properties p = loadProperties();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found.", e);
        }
        return DriverManager.getConnection(
                p.getProperty("db.url"),
                p.getProperty("db.user"),
                p.getProperty("db.password"));
    }
}
