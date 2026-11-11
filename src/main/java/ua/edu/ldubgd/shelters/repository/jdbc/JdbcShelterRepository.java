package ua.edu.ldubgd.shelters.repository.jdbc;

import ua.edu.ldubgd.shelters.exception.DataAccessException;
import ua.edu.ldubgd.shelters.jdbc.Database;
import ua.edu.ldubgd.shelters.model.District;
import ua.edu.ldubgd.shelters.model.Shelter;
import ua.edu.ldubgd.shelters.model.ShelterStatus;
import ua.edu.ldubgd.shelters.model.ShelterType;
import ua.edu.ldubgd.shelters.repository.ShelterRepository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcShelterRepository implements ShelterRepository {

    private static final String SELECT = """
            SELECT s.id, s.name, s.address, s.type, s.capacity, s.accessible,
                   s.status, s.last_inspection_date, d.id AS district_id, d.name AS district_name
            FROM shelters s
            JOIN districts d ON d.id = s.district_id
            """;

    @Override
    public List<Shelter> findAll() {
        return query(SELECT + " ORDER BY s.id");
    }

    @Override
    public Optional<Shelter> findById(int id) {
        return query(SELECT + " WHERE s.id = ?", id).stream().findFirst();
    }

    @Override
    public List<Shelter> findByDistrictId(int districtId) {
        return query(SELECT + " WHERE d.id = ? ORDER BY s.name", districtId);
    }

    @Override
    public boolean existsByAddress(String address) {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT 1 FROM shelters WHERE address = ?")) {
            ps.setString(1, address);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Помилка перевірки адреси", e);
        }
    }

    @Override
    public Shelter save(Shelter s) {
        return s.getId() == null ? insert(s) : update(s);
    }

    @Override
    public boolean deleteById(int id) {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM shelters WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DataAccessException("Помилка видалення укриття", e);
        }
    }

    private Shelter insert(Shelter s) {
        String sql = """
                INSERT INTO shelters (name, address, district_id, type, capacity, accessible, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getAddress());
            ps.setInt(3, s.getDistrict().id());
            ps.setString(4, s.getType().name());
            ps.setInt(5, s.getCapacity());
            ps.setBoolean(6, s.isAccessible());
            ps.setString(7, s.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                s.setId(keys.getInt(1));
            }
            return s;
        } catch (SQLException e) {
            throw new DataAccessException("Помилка додавання укриття", e);
        }
    }

    private Shelter update(Shelter s) {
        try (Connection con = Database.getConnection()) {
            updateShelter(con, s);
            return s;
        } catch (SQLException e) {
            throw new DataAccessException("Помилка оновлення укриття", e);
        }
    }

    /** Оновлення в межах переданого з’єднання — використовується і в транзакції перевірки. */
    static void updateShelter(Connection con, Shelter s) throws SQLException {
        String sql = """
                UPDATE shelters SET name = ?, address = ?, district_id = ?, type = ?, capacity = ?,
                       accessible = ?, status = ?, last_inspection_date = ?
                WHERE id = ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getAddress());
            ps.setInt(3, s.getDistrict().id());
            ps.setString(4, s.getType().name());
            ps.setInt(5, s.getCapacity());
            ps.setBoolean(6, s.isAccessible());
            ps.setString(7, s.getStatus().name());
            ps.setDate(8, s.getLastInspectionDate() == null ? null : Date.valueOf(s.getLastInspectionDate()));
            ps.setInt(9, s.getId());
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Укриття з id=" + s.getId() + " не знайдено");
            }
        }
    }

    private List<Shelter> query(String sql, Object... params) {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Shelter> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Помилка читання укриттів", e);
        }
    }

    private Shelter map(ResultSet rs) throws SQLException {
        Shelter s = new Shelter(
                rs.getString("name"),
                rs.getString("address"),
                new District(rs.getInt("district_id"), rs.getString("district_name")),
                ShelterType.valueOf(rs.getString("type")),
                rs.getInt("capacity"),
                rs.getBoolean("accessible"));
        s.setId(rs.getInt("id"));
        s.setStatus(ShelterStatus.valueOf(rs.getString("status")));
        Date last = rs.getDate("last_inspection_date");
        s.setLastInspectionDate(last == null ? null : last.toLocalDate());
        return s;
    }
}
