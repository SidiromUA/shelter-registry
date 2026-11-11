package ua.edu.ldubgd.shelters.repository.jdbc;

import ua.edu.ldubgd.shelters.exception.DataAccessException;
import ua.edu.ldubgd.shelters.jdbc.Database;
import ua.edu.ldubgd.shelters.model.Inspection;
import ua.edu.ldubgd.shelters.model.InspectionResult;
import ua.edu.ldubgd.shelters.model.Shelter;
import ua.edu.ldubgd.shelters.repository.InspectionRepository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcInspectionRepository implements InspectionRepository {

    @Override
    public List<Inspection> findByShelterId(int shelterId) {
        String sql = """
                SELECT id, shelter_id, inspector_id, inspection_date, result, notes
                FROM inspections WHERE shelter_id = ? ORDER BY inspection_date DESC
                """;
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, shelterId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Inspection> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(new Inspection(
                            rs.getInt("id"),
                            rs.getInt("shelter_id"),
                            rs.getInt("inspector_id"),
                            rs.getDate("inspection_date").toLocalDate(),
                            InspectionResult.valueOf(rs.getString("result")),
                            rs.getString("notes")));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Помилка читання перевірок", e);
        }
    }

    @Override
    public Inspection saveWithShelterStatus(Inspection inspection, Shelter shelter) {
        String sql = """
                INSERT INTO inspections (shelter_id, inspector_id, inspection_date, result, notes)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, inspection.shelterId());
                ps.setInt(2, inspection.inspectorId());
                ps.setDate(3, Date.valueOf(inspection.date()));
                ps.setString(4, inspection.result().name());
                ps.setString(5, inspection.notes());
                ps.executeUpdate();
                int id;
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    id = keys.getInt(1);
                }
                JdbcShelterRepository.updateShelter(con, shelter);
                con.commit();
                return new Inspection(id, inspection.shelterId(), inspection.inspectorId(),
                        inspection.date(), inspection.result(), inspection.notes());
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Помилка збереження перевірки", e);
        }
    }
}
