package ua.edu.ldubgd.shelters.model;

public enum ShelterStatus {
    READY("Готове"),
    LIMITED("Обмежено готове"),
    NOT_READY("Не готове");

    private final String label;

    ShelterStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Стан укриття за результатом перевірки. */
    public static ShelterStatus from(InspectionResult result) {
        return switch (result) {
            case PASSED -> READY;
            case FAILED -> NOT_READY;
            case PENDING -> LIMITED;
        };
    }
}
