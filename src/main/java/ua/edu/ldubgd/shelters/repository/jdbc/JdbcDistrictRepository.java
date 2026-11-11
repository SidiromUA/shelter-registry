package ua.edu.ldubgd.shelters.repository.jdbc;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import ua.edu.ldubgd.shelters.model.District;
import ua.edu.ldubgd.shelters.repository.DistrictRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDistrictRepository implements DistrictRepository {

    private final JdbcClient jdbc;

    public JdbcDistrictRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<District> findAll() {
        // Record District відображається на рядок автоматично — за назвами стовпців
        return jdbc.sql("SELECT id, name FROM districts ORDER BY name").query(District.class).list();
    }

    @Override
    public Optional<District> findById(int id) {
        return jdbc.sql("SELECT id, name FROM districts WHERE id = :id")
                .param("id", id)
                .query(District.class).optional();
    }
}
