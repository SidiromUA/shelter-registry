package ua.edu.ldubgd.shelters.repository.jdbc;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ua.edu.ldubgd.shelters.model.District;
import ua.edu.ldubgd.shelters.model.Shelter;
import ua.edu.ldubgd.shelters.model.ShelterStatus;
import ua.edu.ldubgd.shelters.model.ShelterType;
import ua.edu.ldubgd.shelters.repository.ShelterRepository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

/**
 * Та сама логіка, що й у ЛР3, але без ручного керування з’єднаннями:
 * JdbcClient (Spring) бере з’єднання з пулу, закриває ресурси і перетворює SQLException
 * на неперевірювані винятки DataAccessException.
 */
@Repository
public class JdbcShelterRepository implements ShelterRepository {

    private static final String SELECT = """
            SELECT s.id, s.name, s.address, s.type, s.capacity, s.accessible,
                   s.status, s.last_inspection_date, d.id AS district_id, d.name AS district_name
            FROM shelters s
            JOIN districts d ON d.id = s.district_id
            """;

    private static final RowMapper<Shelter> MAPPER = (rs, rowNum) -> {
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
    };

    private final JdbcClient jdbc;

    // Єдиний конструктор — Spring автоматично передасть бін JdbcClient (анотація @Autowired не потрібна)
    public JdbcShelterRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Shelter> findAll() {
        return jdbc.sql(SELECT + " ORDER BY s.id").query(MAPPER).list();
    }

    @Override
    public Optional<Shelter> findById(int id) {
        return jdbc.sql(SELECT + " WHERE s.id = :id").param("id", id).query(MAPPER).optional();
    }

    @Override
    public List<Shelter> findByDistrictId(int districtId) {
        return jdbc.sql(SELECT + " WHERE d.id = :districtId ORDER BY s.name")
                .param("districtId", districtId)
                .query(MAPPER).list();
    }

    @Override
    public boolean existsByAddress(String address) {
        return jdbc.sql("SELECT COUNT(*) FROM shelters WHERE address = :address")
                .param("address", address)
                .query(Integer.class).single() > 0;
    }

    @Override
    public Shelter save(Shelter s) {
        if (s.getId() == null) {
            KeyHolder keys = new GeneratedKeyHolder();
            jdbc.sql("""
                    INSERT INTO shelters (name, address, district_id, type, capacity, accessible, status)
                    VALUES (:name, :address, :districtId, :type, :capacity, :accessible, :status)
                    """)
                    .param("name", s.getName())
                    .param("address", s.getAddress())
                    .param("districtId", s.getDistrict().id())
                    .param("type", s.getType().name())
                    .param("capacity", s.getCapacity())
                    .param("accessible", s.isAccessible())
                    .param("status", s.getStatus().name())
                    .update(keys, "id");
            s.setId(keys.getKeyAs(Integer.class));
        } else {
            jdbc.sql("""
                    UPDATE shelters SET name = :name, address = :address, district_id = :districtId,
                           type = :type, capacity = :capacity, accessible = :accessible,
                           status = :status, last_inspection_date = :lastInspectionDate
                    WHERE id = :id
                    """)
                    .param("name", s.getName())
                    .param("address", s.getAddress())
                    .param("districtId", s.getDistrict().id())
                    .param("type", s.getType().name())
                    .param("capacity", s.getCapacity())
                    .param("accessible", s.isAccessible())
                    .param("status", s.getStatus().name())
                    .param("lastInspectionDate", s.getLastInspectionDate())
                    .param("id", s.getId())
                    .update();
        }
        return s;
    }

    @Override
    public boolean deleteById(int id) {
        return jdbc.sql("DELETE FROM shelters WHERE id = :id").param("id", id).update() == 1;
    }
}
