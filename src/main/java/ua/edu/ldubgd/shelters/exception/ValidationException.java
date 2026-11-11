package ua.edu.ldubgd.shelters.exception;

import java.util.List;

/** Некоректні вхідні дані. Містить перелік усіх виявлених помилок. */
public class ValidationException extends RuntimeException {

    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
