package ua.edu.ldubgd.shelters.web;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ua.edu.ldubgd.shelters.exception.NotFoundException;

/** Помилки на вебсторінках: замість «білої сторінки» — шаблон error/404.html. */
@ControllerAdvice(assignableTypes = ShelterWebController.class)
public class WebExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(NotFoundException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/404";
    }
}
