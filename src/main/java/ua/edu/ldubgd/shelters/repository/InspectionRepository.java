package ua.edu.ldubgd.shelters.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.edu.ldubgd.shelters.model.Inspection;

import java.util.List;

public interface InspectionRepository extends JpaRepository<Inspection, Integer> {

    /** Перевірки укриття разом з інспектором (без додаткових запитів на кожен рядок). */
    @EntityGraph(attributePaths = "inspector")
    List<Inspection> findByShelterIdOrderByDateDesc(Integer shelterId);
}
