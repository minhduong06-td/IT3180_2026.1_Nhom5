package com.bluemoon.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private final String url;
    private final String username;
    private final String password;

    public Database() {
        this(setting("BLUEMOON_DB_URL", "jdbc:mysql://localhost:3307/bluemoon?connectionTimeZone=Asia/Ho_Chi_Minh"),
                setting("BLUEMOON_DB_USER", "bluemoon_app"), setting("BLUEMOON_DB_PASSWORD", "bluemoon_dev"));
    }

    public Database(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Connection openConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private static String setting(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
