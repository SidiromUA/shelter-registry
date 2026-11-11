package ua.edu.ldubgd.shelters.model;

public enum ShelterType {
    BASEMENT("Підвал"),
    PARKING("Паркінг"),
    CIVIL_DEFENCE("Захисна споруда ЦЗ"),
    METRO("Метрополітен");

    private final String label;

    ShelterType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
