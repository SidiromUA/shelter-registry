package ua.edu.ldubgd.shelters.model;

import java.time.LocalDate;

/** Перевірка укриття інспектором. */
public record Inspection(
        Integer id,
        int shelterId,
        int inspectorId,
        LocalDate date,
        InspectionResult result,
        String notes) {
}
