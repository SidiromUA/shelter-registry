package ua.edu.ldubgd.shelters.repository.jdbc;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ua.edu.ldubgd.shelters.model.Inspection;
import ua.edu.ldubgd.shelters.model.InspectionResult;
import ua.edu.ldubgd.shelters.repository.InspectionRepository;

import java.util.List;

@Repository
public class JdbcInspectionRepository implements InspectionRepository {

    private final JdbcClient jdbc;

    public JdbcInspectionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Inspection> findByShelterId(int shelterId) {
        return jdbc.sql("""
                        SELECT id, shelter_id, inspector_id, inspection_date, result, notes
                        FROM inspections WHERE shelter_id = :shelterId ORDER BY inspection_date DESC
                        """)
                .param("shelterId", shelterId)
                .query((rs, n) -> new Inspection(
                        rs.getInt("id"),
                        rs.getInt("shelter_id"),
                        rs.getInt("inspector_id"),
                        rs.getDate("inspection_date").toLocalDate(),
                        InspectionResult.valueOf(rs.getString("result")),
                        rs.getString("notes")))
                .list();
    }

    @Override
    public Inspection save(Inspection i) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.sql("""
                        INSERT INTO inspections (shelter_id, inspector_id, inspection_date, result, notes)
                        VALUES (:shelterId, :inspectorId, :date, :result, :notes)
                        """)
                .param("shelterId", i.shelterId())
                .param("inspectorId", i.inspectorId())
                .param("date", i.date())
                .param("result", i.result().name())
                .param("notes", i.notes())
                .update(keys, "id");
        return new Inspection(keys.getKeyAs(Integer.class), i.shelterId(), i.inspectorId(),
                i.date(), i.result(), i.notes());
    }
}
