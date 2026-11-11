package ua.edu.ldubgd.shelters.exception;

/** Запитаний об’єкт не існує. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
