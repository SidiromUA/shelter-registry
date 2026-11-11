package ua.edu.ldubgd.shelters.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.edu.ldubgd.shelters.model.Shelter;
import ua.edu.ldubgd.shelters.model.ShelterStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA: реалізацію цього інтерфейсу Spring створює сам.
 * Порівняйте з JdbcShelterRepository (ЛР5–7) — SQL більше писати не потрібно.
 */
public interface ShelterRepository extends JpaRepository<Shelter, Integer> {

    // ----- Похідні запити: SQL будується з назви методу -----

    /** WHERE district_id = ? ORDER BY name */
    @EntityGraph(attributePaths = "district")
    List<Shelter> findByDistrictIdOrderByName(Integer districtId);

    /** WHERE status = ? … LIMIT/OFFSET — посторінково */
    Page<Shelter> findByStatus(ShelterStatus status, Pageable pageable);

    boolean existsByAddress(String address);

    boolean existsByAddressAndIdNot(String address, Integer id);

    long countByAccessibleTrue();

    // ----- Запити JPQL: оперують сутностями та їхніми полями, а не таблицями -----

    @Query("""
            select s from Shelter s join fetch s.district
            where s.lastInspectionDate is null or s.lastInspectionDate < :border
            order by s.name
            """)
    List<Shelter> findInspectionOverdue(@Param("border") LocalDate border);

    @Query("select coalesce(sum(s.capacity), 0) from Shelter s where s.district.id = :districtId")
    long totalCapacity(@Param("districtId") Integer districtId);

    // ----- Боротьба з проблемою N+1: район завантажується тим самим запитом (JOIN FETCH) -----

    @Override
    @EntityGraph(attributePaths = "district")
    List<Shelter> findAll();

    @Override
    @EntityGraph(attributePaths = "district")
    Page<Shelter> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "district")
    Optional<Shelter> findById(Integer id);
}
