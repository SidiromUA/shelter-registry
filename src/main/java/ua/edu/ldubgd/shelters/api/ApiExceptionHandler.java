package ua.edu.ldubgd.shelters.api;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ua.edu.ldubgd.shelters.exception.NotFoundException;
import ua.edu.ldubgd.shelters.exception.ValidationException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Централізована обробка помилок REST API. Відповідь — у стандартному форматі Problem Details:
 * {"type": ..., "title": ..., "status": 400, "detail": ..., "errors": {"поле": "повідомлення"}}
 */
@RestControllerAdvice(assignableTypes = ShelterApiController.class)
public class ApiExceptionHandler {

    /** @Valid не пройдено — помилки формату полів. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalid(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        return problem(HttpStatus.BAD_REQUEST, "Некоректні дані", errors);
    }

    /** Порушено бізнес-правила (сервіс). */
    @ExceptionHandler(ValidationException.class)
    public ProblemDetail business(ValidationException e) {
        return problem(HttpStatus.BAD_REQUEST, "Порушено правила реєстру", e.getErrors());
    }

    /** Тіло запиту — не JSON або невідоме значення переліку (наприклад, type: "CAVE"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail unreadable(HttpMessageNotReadableException e) {
        return problem(HttpStatus.BAD_REQUEST, "Тіло запиту не вдалося прочитати", Map.of());
    }

    /** Сортування за неіснуючим полем: ?sort=abc */
    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail badSort(PropertyReferenceException e) {
        return problem(HttpStatus.BAD_REQUEST, "Некоректний параметр сортування",
                Map.of("sort", "Невідоме поле: " + e.getPropertyName()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail notFound(NotFoundException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        pd.setTitle("Не знайдено");
        return pd;
    }

    /** Наприклад, неіснуючий inspectorId — порушення зовнішнього ключа. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail conflict(DataIntegrityViolationException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Операція порушує цілісність даних");
        pd.setTitle("Конфлікт даних");
        return pd;
    }

    private static ProblemDetail problem(HttpStatus status, String title, Map<String, String> errors) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, "Перевірте поля запиту");
        pd.setTitle(title);
        pd.setProperty("errors", errors);
        return pd;
    }
}
