package ua.edu.ldubgd.shelters.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/shelters";
    }

    /** Сторінка входу. Обробку POST /login виконує Spring Security. */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
