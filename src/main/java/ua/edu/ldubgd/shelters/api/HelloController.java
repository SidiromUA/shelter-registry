package ua.edu.ldubgd.shelters.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Найпростіший контролер для перевірки, що застосунок запущено: GET /hello?name=Олена */
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello(@RequestParam(defaultValue = "світ") String name) {
        return "Привіт, " + name + "! Реєстр укриттів працює.";
    }
}
