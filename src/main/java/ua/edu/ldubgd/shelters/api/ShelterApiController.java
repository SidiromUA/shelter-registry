package ua.edu.ldubgd.shelters.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.ldubgd.shelters.dto.ShelterView;
import ua.edu.ldubgd.shelters.exception.NotFoundException;
import ua.edu.ldubgd.shelters.model.District;
import ua.edu.ldubgd.shelters.model.Inspection;
import ua.edu.ldubgd.shelters.service.ShelterService;

import java.util.List;
import java.util.Map;

/**
 * Перший REST-контролер. Порівняйте з ShelterServlet (ЛР4):
 * немає doGet/request/response, маршрут задає анотація, а об’єкти автоматично перетворюються на JSON.
 */
@RestController
@RequestMapping("/api")
public class ShelterApiController {

    private final ShelterService service;

    public ShelterApiController(ShelterService service) {
        this.service = service;
    }

    /** GET /api/shelters або /api/shelters?districtId=5 */
    @GetMapping("/shelters")
    public List<ShelterView> shelters(@RequestParam(required = false) Integer districtId) {
        return districtId == null ? service.findAll() : service.findByDistrict(districtId);
    }

    /** GET /api/shelters/overdue — укриття з простроченою перевіркою (ЛР6) */
    @GetMapping("/shelters/overdue")
    public List<ShelterView> overdue() {
        return service.findOverdueInspection();
    }

    /** GET /api/shelters/3 */
    @GetMapping("/shelters/{id}")
    public ShelterView shelter(@PathVariable int id) {
        return service.findById(id);
    }

    @GetMapping("/shelters/{id}/inspections")
    public List<Inspection> inspections(@PathVariable int id) {
        return service.findInspections(id);
    }

    @GetMapping("/districts")
    public List<District> districts() {
        return service.findAllDistricts();
    }

    /** Неіснуюче укриття → HTTP 404 (детальна обробка помилок — у ЛР7). */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(NotFoundException e) {
        return Map.of("error", e.getMessage());
    }
}
