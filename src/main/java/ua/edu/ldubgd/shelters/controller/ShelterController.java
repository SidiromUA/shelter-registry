package ua.edu.ldubgd.shelters.controller;

import ua.edu.ldubgd.shelters.dto.CreateShelterRequest;
import ua.edu.ldubgd.shelters.exception.NotFoundException;
import ua.edu.ldubgd.shelters.exception.ValidationException;
import ua.edu.ldubgd.shelters.service.ShelterService;
import ua.edu.ldubgd.shelters.view.ShelterListPage;

import java.util.List;

/**
 * Контролер (C у MVC): приймає запит, викликає сервіс (модель) і обирає представлення (V).
 * У ЛР4 його роль перейде до сервлета, у ЛР5 — до класу з анотацією @Controller.
 */
public class ShelterController {

    private final ShelterService service;
    private final ShelterListPage listPage;

    public ShelterController(ShelterService service, ShelterListPage listPage) {
        this.service = service;
        this.listPage = listPage;
    }

    /** «GET /shelters» — сторінка зі списком укриттів. */
    public String list() {
        return listPage.render(service.findAll(), List.of(), null);
    }

    /** «POST /shelters» — додавання; у разі помилок повертає ту саму сторінку з повідомленнями. */
    public String create(CreateShelterRequest request) {
        try {
            var created = service.create(request);
            return listPage.render(service.findAll(), List.of(),
                    "Укриття «" + created.name() + "» додано");
        } catch (ValidationException e) {
            return listPage.render(service.findAll(), e.getErrors(), null);
        }
    }

    /** «POST /shelters/{id}/delete». */
    public String delete(int id) {
        try {
            service.delete(id);
            return listPage.render(service.findAll(), List.of(), "Укриття видалено");
        } catch (NotFoundException e) {
            return listPage.render(service.findAll(), List.of(e.getMessage()), null);
        }
    }
}
