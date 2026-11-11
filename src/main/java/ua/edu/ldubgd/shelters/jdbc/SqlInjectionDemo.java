package ua.edu.ldubgd.shelters.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * НАВЧАЛЬНИЙ ПРИКЛАД: чому не можна складати SQL конкатенацією рядків.
 * Не використовуйте метод unsafeCount у реальних застосунках.
 */
public class SqlInjectionDemo {

    /** Небезпечно: введення користувача потрапляє безпосередньо в текст SQL. */
    public int unsafeCount(String district) throws SQLException {
        String sql = "SELECT COUNT(*) FROM shelters s JOIN districts d ON d.id = s.district_id "
                + "WHERE d.name = '" + district + "'";
        try (Connection con = Database.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Безпечно: значення передається як параметр і не змінює структуру запиту. */
    public int safeCount(String district) throws SQLException {
        String sql = "SELECT COUNT(*) FROM shelters s JOIN districts d ON d.id = s.district_id "
                + "WHERE d.name = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, district);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
