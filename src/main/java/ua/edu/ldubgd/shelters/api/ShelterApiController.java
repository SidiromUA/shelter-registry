package ua.edu.ldubgd.shelters.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ua.edu.ldubgd.shelters.dto.InspectionRequest;
import ua.edu.ldubgd.shelters.dto.InspectionView;
import ua.edu.ldubgd.shelters.dto.PageResponse;
import ua.edu.ldubgd.shelters.dto.ShelterForm;
import ua.edu.ldubgd.shelters.dto.ShelterView;
import ua.edu.ldubgd.shelters.model.District;
import ua.edu.ldubgd.shelters.service.ShelterService;

import java.net.URI;
import java.security.Principal;
import java.util.List;

/**
 * RESTful API укриттів. Ресурси — іменники у множині, дії — HTTP-методи:
 * GET (читання), POST (створення), PUT (заміна), DELETE (видалення).
 * Помилки обробляє ApiExceptionHandler і повертає їх у форматі Problem Details (RFC 9457).
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Укриття", description = "Реєстр укриттів і перевірок. Зміни — з HTTP Basic (admin/admin, inspector/inspector)")
public class ShelterApiController {

    private final ShelterService service;

    public ShelterApiController(ShelterService service) {
        this.service = service;
    }

    @Operation(summary = "Список укриттів посторінково",
            description = "Параметри: ?page=0&size=5&sort=capacity,desc")
    @GetMapping("/shelters")
    public PageResponse<ShelterView> list(@ParameterObject @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return PageResponse.of(service.findPage(pageable));
    }

    @Operation(summary = "Укриття району")
    @GetMapping("/districts/{districtId}/shelters")
    public List<ShelterView> byDistrict(@PathVariable int districtId) {
        return service.findByDistrict(districtId);
    }

    @Operation(summary = "Укриття з простроченою перевіркою")
    @GetMapping("/shelters/overdue")
    public List<ShelterView> overdue() {
        return service.findOverdueInspection();
    }

    @Operation(summary = "Укриття за ідентифікатором")
    @ApiResponse(responseCode = "404", description = "Укриття не знайдено")
    @GetMapping("/shelters/{id}")
    public ShelterView get(@PathVariable int id) {
        return service.findById(id);
    }

    @Operation(summary = "Створити укриття")
    @ApiResponse(responseCode = "201", description = "Створено; адреса нового ресурсу — у заголовку Location")
    @ApiResponse(responseCode = "400", description = "Некоректні дані")
    @PostMapping("/shelters")
    public ResponseEntity<ShelterView> create(@Valid @RequestBody ShelterForm form) {
        ShelterView created = service.create(form);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Оновити укриття")
    @PutMapping("/shelters/{id}")
    public ShelterView update(@PathVariable int id, @Valid @RequestBody ShelterForm form) {
        return service.update(id, form);
    }

    @Operation(summary = "Видалити укриття")
    @ApiResponse(responseCode = "204", description = "Видалено")
    @DeleteMapping("/shelters/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Перевірки укриття")
    @GetMapping("/shelters/{id}/inspections")
    public List<InspectionView> inspections(@PathVariable int id) {
        return service.findInspections(id);
    }

    @Operation(summary = "Зареєструвати перевірку", description = "Стан укриття змінюється за результатом перевірки")
    @PostMapping("/shelters/{id}/inspections")
    public ShelterView inspect(@PathVariable int id, @Valid @RequestBody InspectionRequest request,
                               Principal principal) {
        return service.registerInspection(id, principal.getName(), request.result(), request.notes());
    }

    @Operation(summary = "Райони міста")
    @GetMapping("/districts")
    public List<District> districts() {
        return service.findAllDistricts();
    }
}
