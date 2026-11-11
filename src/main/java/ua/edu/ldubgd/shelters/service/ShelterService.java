package ua.edu.ldubgd.shelters.service;

import ua.edu.ldubgd.shelters.dto.CreateShelterRequest;
import ua.edu.ldubgd.shelters.dto.ShelterView;
import ua.edu.ldubgd.shelters.exception.NotFoundException;
import ua.edu.ldubgd.shelters.exception.ValidationException;
import ua.edu.ldubgd.shelters.model.District;
import ua.edu.ldubgd.shelters.model.Inspection;
import ua.edu.ldubgd.shelters.model.InspectionResult;
import ua.edu.ldubgd.shelters.model.Shelter;
import ua.edu.ldubgd.shelters.model.ShelterType;
import ua.edu.ldubgd.shelters.repository.DistrictRepository;
import ua.edu.ldubgd.shelters.repository.InspectionRepository;
import ua.edu.ldubgd.shelters.repository.ShelterRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Бізнес-логіка роботи з укриттями: валідація, правила предметної області.
 * Залежності передаються через конструктор — так само їх згодом передаватиме Spring (ЛР5).
 */
public class ShelterService {

    private final ShelterRepository shelters;
    private final DistrictRepository districts;
    private final InspectionRepository inspections;

    public ShelterService(ShelterRepository shelters, DistrictRepository districts,
                          InspectionRepository inspections) {
        this.shelters = shelters;
        this.districts = districts;
        this.inspections = inspections;
    }

    public List<ShelterView> findAll() {
        return shelters.findAll().stream().map(ShelterView::from).toList();
    }

    public ShelterView findById(int id) {
        return ShelterView.from(getShelter(id));
    }

    public List<District> findAllDistricts() {
        return districts.findAll();
    }

    public ShelterView create(CreateShelterRequest request) {
        List<String> errors = new ArrayList<>();
        if (request.name() == null || request.name().isBlank()) {
            errors.add("Вкажіть назву укриття");
        }
        if (request.address() == null || request.address().isBlank()) {
            errors.add("Вкажіть адресу");
        } else if (shelters.existsByAddress(request.address().trim())) {
            errors.add("Укриття за адресою «" + request.address().trim() + "» вже зареєстровано");
        }
        if (request.capacity() == null || request.capacity() <= 0) {
            errors.add("Місткість має бути додатним числом");
        }
        ShelterType type = parseType(request.type(), errors);
        District district = request.districtId() == null ? null
                : districts.findById(request.districtId()).orElse(null);
        if (district == null) {
            errors.add("Оберіть район зі списку");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        Shelter shelter = new Shelter(request.name().trim(), request.address().trim(), district,
                type, request.capacity(), request.accessible());
        return ShelterView.from(shelters.save(shelter));
    }

    public void delete(int id) {
        if (!shelters.deleteById(id)) {
            throw new NotFoundException("Укриття з id=" + id + " не знайдено");
        }
    }

    /** Реєстрація перевірки: змінює стан укриття за результатом. */
    public ShelterView registerInspection(int shelterId, int inspectorId, InspectionResult result, String notes) {
        Shelter shelter = getShelter(shelterId);
        LocalDate today = LocalDate.now();
        shelter.applyInspection(result, today);
        inspections.saveWithShelterStatus(
                new Inspection(null, shelterId, inspectorId, today, result, notes), shelter);
        return ShelterView.from(shelter);
    }

    private Shelter getShelter(int id) {
        return shelters.findById(id)
                .orElseThrow(() -> new NotFoundException("Укриття з id=" + id + " не знайдено"));
    }

    private ShelterType parseType(String value, List<String> errors) {
        try {
            return ShelterType.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            errors.add("Оберіть тип укриття");
            return null;
        }
    }
}
