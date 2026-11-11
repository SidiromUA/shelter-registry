package ua.edu.ldubgd.shelters.model;

public enum InspectionResult {
    PASSED("Пройдено"),
    FAILED("Не пройдено"),
    PENDING("Очікує повторної перевірки");

    private final String label;

    InspectionResult(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
