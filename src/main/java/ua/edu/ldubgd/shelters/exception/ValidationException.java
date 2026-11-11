package ua.edu.ldubgd.shelters.exception;

import java.util.Map;

/**
 * Порушення бізнес-правил. Містить помилки у вигляді «поле → повідомлення»,
 * щоб показати їх біля відповідного поля форми або у відповіді API.
 */
public class ValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super(String.join("; ", errors.values()));
        this.errors = Map.copyOf(errors);
    }

    public ValidationException(String field, String message) {
        this(Map.of(field, message));
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
