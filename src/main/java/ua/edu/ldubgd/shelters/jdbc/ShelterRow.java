package ua.edu.ldubgd.shelters.jdbc;

/** Рядок результату запиту «укриття + назва району». */
public record ShelterRow(
        int id,
        String name,
        String address,
        String district,
        String type,
        int capacity,
        boolean accessible,
        String status) {

    @Override
    public String toString() {
        return String.format("%-3d %-22s %-28s %-15s %-14s %5d %-4s %s",
                id, name, address, district, type, capacity, accessible ? "так" : "ні", status);
    }
}
