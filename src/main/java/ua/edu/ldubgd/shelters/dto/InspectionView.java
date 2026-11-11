package ua.edu.ldubgd.shelters.dto;

import ua.edu.ldubgd.shelters.model.Inspection;

import java.time.LocalDate;

/** Перевірка для відображення: сутність JPA назовні не віддаємо (ліниві зв’язки, зайві поля). */
public record InspectionView(int id, LocalDate date, String result, String resultLabel,
                             String notes, String inspector) {

    public static InspectionView from(Inspection i) {
        return new InspectionView(i.getId(), i.getDate(), i.getResult().name(), i.getResult().getLabel(),
                i.getNotes(), i.getInspector().getFullName());
    }
}
