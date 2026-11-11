package ua.edu.ldubgd.shelters.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Параметри підключення до БД.
 * Значення беруться зі змінних оточення DB_URL, DB_USER, DB_PASSWORD,
 * а якщо їх немає — використовуються значення за замовчуванням для локальної розробки.
 */
public final class Database {

    private static final String URL =
            env("DB_URL", "jdbc:postgresql://localhost:5432/shelter_registry");
    private static final String USER = env("DB_USER", "shelter");
    private static final String PASSWORD = env("DB_PASSWORD", "shelter");

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
