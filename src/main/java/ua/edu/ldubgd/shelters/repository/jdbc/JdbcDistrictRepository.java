package ua.edu.ldubgd.shelters.repository.jdbc;

import ua.edu.ldubgd.shelters.exception.DataAccessException;
import ua.edu.ldubgd.shelters.jdbc.Database;
import ua.edu.ldubgd.shelters.model.District;
import ua.edu.ldubgd.shelters.repository.DistrictRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcDistrictRepository implements DistrictRepository {

    @Override
    public List<District> findAll() {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id, name FROM districts ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            List<District> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new District(rs.getInt("id"), rs.getString("name")));
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Помилка читання районів", e);
        }
    }

    @Override
    public Optional<District> findById(int id) {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id, name FROM districts WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(new District(rs.getInt("id"), rs.getString("name")))
                        : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Помилка читання району", e);
        }
    }
}
