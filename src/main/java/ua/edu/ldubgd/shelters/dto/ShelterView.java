package ua.edu.ldubgd.shelters.dto;

import ua.edu.ldubgd.shelters.model.Shelter;

/**
 * DTO для відображення: лише ті поля, які потрібні сторінці, вже у «людському» вигляді.
 * Представлення (view) не працює з сутністю напряму.
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
        String statusCode) {

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
                s.getStatus().name());
    }
}
