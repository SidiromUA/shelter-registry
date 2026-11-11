package ua.edu.ldubgd.shelters.exception;

/** Обгортка над SQLException: верхні шари не залежать від JDBC. */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
