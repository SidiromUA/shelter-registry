package ua.edu.ldubgd.shelters.repository;

import ua.edu.ldubgd.shelters.model.Inspection;
import ua.edu.ldubgd.shelters.model.Shelter;

import java.util.List;

public interface InspectionRepository {

    List<Inspection> findByShelterId(int shelterId);

    /** Зберігає перевірку й оновлений стан укриття в одній транзакції. */
    Inspection saveWithShelterStatus(Inspection inspection, Shelter shelter);
}
