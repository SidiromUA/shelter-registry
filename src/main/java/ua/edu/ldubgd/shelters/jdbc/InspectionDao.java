package ua.edu.ldubgd.shelters.jdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

/** Робота з перевірками укриттів. Демонструє транзакцію JDBC. */
public class InspectionDao {

    /**
     * Реєструє перевірку і в тій самій транзакції оновлює стан укриття.
     * Якщо будь-яка з операцій завершиться помилкою, зміни буде відкочено (rollback).
     */
    public void registerInspection(int shelterId, int inspectorId, LocalDate date,
                                   String result, String notes) throws SQLException {
        String insertSql = """
                INSERT INTO inspections (shelter_id, inspector_id, inspection_date, result, notes)
                VALUES (?, ?, ?, ?, ?)
                """;
        String updateSql = """
                UPDATE shelters SET status = ?, last_inspection_date = ? WHERE id = ?
                """;

        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);                 // початок транзакції
            try (PreparedStatement insert = con.prepareStatement(insertSql);
                 PreparedStatement update = con.prepareStatement(updateSql)) {

                insert.setInt(1, shelterId);
                insert.setInt(2, inspectorId);
                insert.setDate(3, Date.valueOf(date));
                insert.setString(4, result);
                insert.setString(5, notes);
                insert.executeUpdate();

                update.setString(1, statusFor(result));
                update.setDate(2, Date.valueOf(date));
                update.setInt(3, shelterId);
                if (update.executeUpdate() != 1) {
                    throw new SQLException("Укриття з id=" + shelterId + " не знайдено");
                }

                con.commit();                         // фіксація обох змін
            } catch (SQLException e) {
                con.rollback();                       // відкат обох змін
                throw e;
            }
        }
    }

    private String statusFor(String result) {
        return switch (result) {
            case "PASSED" -> "READY";
            case "FAILED" -> "NOT_READY";
            default -> "LIMITED";
        };
    }
}
