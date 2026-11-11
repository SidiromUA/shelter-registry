package ua.edu.ldubgd.shelters.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.edu.ldubgd.shelters.dto.ShelterForm;
import ua.edu.ldubgd.shelters.dto.ShelterView;
import ua.edu.ldubgd.shelters.exception.ValidationException;
import ua.edu.ldubgd.shelters.model.District;
import ua.edu.ldubgd.shelters.model.InspectionResult;
import ua.edu.ldubgd.shelters.model.ShelterType;
import ua.edu.ldubgd.shelters.service.ShelterService;

import java.security.Principal;
import java.util.List;

/**
 * Вебсторінки (Thymeleaf). Метод повертає ім’я шаблону з templates/, а дані передає через Model.
 * Порівняйте з ShelterServlet і JSP з ЛР4.
 */
@Controller
@RequestMapping("/shelters")
public class ShelterWebController {

    private static final String DISTRICT_COOKIE = "district";

    private final ShelterService service;

    public ShelterWebController(ShelterService service) {
        this.service = service;
    }

    /** Атрибути, потрібні всім сторінкам цього контролера. */
    @ModelAttribute("districts")
    public List<District> districts() {
        return service.findAllDistricts();
    }

    @ModelAttribute("types")
    public ShelterType[] types() {
        return ShelterType.values();
    }

    /**
     * GET /shelters?districtId=5. Обраний район запам’ятовується в cookie,
     * тож під час наступного відкриття сторінки фільтр відновиться.
     */
    @GetMapping
    public String list(@RequestParam(required = false) Integer districtId,
                       @RequestParam(defaultValue = "0") int page,
                       @CookieValue(name = DISTRICT_COOKIE, required = false) Integer savedDistrict,
                       HttpServletResponse response, Model model) {
        Integer filter = districtId;
        if (filter == null && savedDistrict != null && savedDistrict > 0) {
            filter = savedDistrict;
        }
        if (districtId != null) {
            Cookie cookie = new Cookie(DISTRICT_COOKIE, String.valueOf(districtId));
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(districtId == 0 ? 0 : 30 * 24 * 3600);  // 0 = «усі райони», cookie видаляємо
            response.addCookie(cookie);
            if (districtId == 0) {
                filter = null;
            }
        }
        if (filter == null) {
            // Без фільтра — посторінково, по 10 записів, за назвою (ЛР8)
            Page<ShelterView> result = service.findPage(PageRequest.of(Math.max(page, 0), 10, Sort.by("name")));
            model.addAttribute("page", result);
            model.addAttribute("shelters", result.getContent());
            model.addAttribute("found", result.getTotalElements());
        } else {
            List<ShelterView> shelters = service.findByDistrict(filter);
            model.addAttribute("shelters", shelters);
            model.addAttribute("found", shelters.size());
        }
        model.addAttribute("selectedDistrict", filter);
        return "shelters/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable int id, Model model) {
        model.addAttribute("shelter", service.findById(id));
        model.addAttribute("inspections", service.findInspections(id));
        model.addAttribute("results", InspectionResult.values());
        return "shelters/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new ShelterForm());
        return "shelters/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") ShelterForm form, BindingResult binding,
                         RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            return "shelters/form";                       // показати форму з помилками біля полів
        }
        try {
            ShelterView created = service.create(form);
            redirect.addFlashAttribute("message", "Укриття «" + created.name() + "» додано");
            return "redirect:/shelters/" + created.id();  // Post/Redirect/Get
        } catch (ValidationException e) {
            e.getErrors().forEach((field, msg) -> binding.rejectValue(field, "business", msg));
            return "shelters/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        model.addAttribute("form", service.getForm(id));
        model.addAttribute("shelterId", id);
        return "shelters/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable int id, @Valid @ModelAttribute("form") ShelterForm form,
                         BindingResult binding, Model model, RedirectAttributes redirect) {
        model.addAttribute("shelterId", id);
        if (binding.hasErrors()) {
            return "shelters/form";
        }
        try {
            service.update(id, form);
            redirect.addFlashAttribute("message", "Зміни збережено");
            return "redirect:/shelters/" + id;
        } catch (ValidationException e) {
            e.getErrors().forEach((field, msg) -> binding.rejectValue(field, "business", msg));
            return "shelters/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes redirect) {
        service.delete(id);
        redirect.addFlashAttribute("message", "Укриття видалено");
        return "redirect:/shelters";
    }

    @PostMapping("/{id}/inspections")
    public String inspect(@PathVariable int id, @RequestParam InspectionResult result,
                          @RequestParam(required = false) String notes, Principal principal,
                          RedirectAttributes redirect) {
        // Principal — користувач, що увійшов (Spring Security); він і є інспектором
        service.registerInspection(id, principal.getName(), result, notes);
        redirect.addFlashAttribute("message", "Перевірку зареєстровано");
        return "redirect:/shelters/" + id;
    }
}
