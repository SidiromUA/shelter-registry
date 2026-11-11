package ua.edu.ldubgd.shelters.dto;

import ua.edu.ldubgd.shelters.model.Shelter;

import java.time.LocalDate;

/**
 * DTO для відображення (сторінки, JSON): лише потрібні поля у «людському» вигляді.
 * Сутність назовні не віддаємо — так змінювати модель БД можна без змін у API.
 */
public record ShelterView(
        int id,
        String name,
        String address,
        String district,
        String type,
        int capacity,
        String accessibility,
        String status,
        String statusCode,
        LocalDate lastInspectionDate) {

    public static ShelterView from(Shelter s) {
        return new ShelterView(
                s.getId(),
                s.getName(),
                s.getAddress(),
                s.getDistrict().name(),
                s.getType().getLabel(),
                s.getCapacity(),
                s.isAccessible() ? "Так" : "Ні",
                s.getStatus().getLabel(),
                s.getStatus().name(),
                s.getLastInspectionDate());
    }
}
