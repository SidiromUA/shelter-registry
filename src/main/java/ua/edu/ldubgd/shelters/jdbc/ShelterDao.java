package ua.edu.ldubgd.shelters.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Доступ до таблиці shelters засобами JDBC. */
public class ShelterDao {

    private static final String SELECT_WITH_DISTRICT = """
            SELECT s.id, s.name, s.address, d.name AS district,
                   s.type, s.capacity, s.accessible, s.status
            FROM shelters s
            JOIN districts d ON d.id = s.district_id
            """;

    public List<ShelterRow> findAll() throws SQLException {
        String sql = SELECT_WITH_DISTRICT + " ORDER BY s.id";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<ShelterRow> result = new ArrayList<>();
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        }
    }

    public Optional<ShelterRow> findById(int id) throws SQLException {
        String sql = SELECT_WITH_DISTRICT + " WHERE s.id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    /** Пошук за назвою району. Параметр передається через «?», тому SQL-ін’єкція неможлива. */
    public List<ShelterRow> findByDistrict(String districtName) throws SQLException {
        String sql = SELECT_WITH_DISTRICT + " WHERE d.name = ? ORDER BY s.name";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, districtName);
            try (ResultSet rs = ps.executeQuery()) {
                List<ShelterRow> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        }
    }

    /** Додає укриття та повертає згенерований ідентифікатор. */
    public int insert(String name, String address, int districtId, String type,
                      int capacity, boolean accessible) throws SQLException {
        String sql = """
                INSERT INTO shelters (name, address, district_id, type, capacity, accessible)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, address);
            ps.setInt(3, districtId);
            ps.setString(4, type);
            ps.setInt(5, capacity);
            ps.setBoolean(6, accessible);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public boolean updateStatus(int id, String status) throws SQLException {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE shelters SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM shelters WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    /** Загальна місткість укриттів за районами (GROUP BY). */
    public Map<String, Integer> capacityByDistrict() throws SQLException {
        String sql = """
                SELECT d.name, COALESCE(SUM(s.capacity), 0) AS total
                FROM districts d
                LEFT JOIN shelters s ON s.district_id = d.id
                GROUP BY d.name
                ORDER BY total DESC
                """;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            Map<String, Integer> result = new LinkedHashMap<>();
            while (rs.next()) {
                result.put(rs.getString("name"), rs.getInt("total"));
            }
            return result;
        }
    }

    private ShelterRow map(ResultSet rs) throws SQLException {
        return new ShelterRow(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("address"),
                rs.getString("district"),
                rs.getString("type"),
                rs.getInt("capacity"),
                rs.getBoolean("accessible"),
                rs.getString("status"));
    }
}
