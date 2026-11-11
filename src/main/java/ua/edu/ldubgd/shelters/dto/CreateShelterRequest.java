package ua.edu.ldubgd.shelters.dto;

/** Дані, які надходять від користувача (форма, консоль, згодом — HTTP-запит). */
public record CreateShelterRequest(
        String name,
        String address,
        Integer districtId,
        String type,
        Integer capacity,
        boolean accessible) {
}
