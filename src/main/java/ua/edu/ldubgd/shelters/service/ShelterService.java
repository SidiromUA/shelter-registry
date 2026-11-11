package ua.edu.ldubgd.shelters.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Бізнес-логіка. Порівняйте з ЛР3: код майже той самий, але об’єкти тепер створює Spring —
 * @Service робить клас біном, а залежності надходять у конструктор автоматично.
 */
@Service
public class ShelterService {

    private final ShelterRepository shelters;
    private final DistrictRepository districts;
    private final InspectionRepository inspections;
    private final Clock clock;

    public ShelterService(ShelterRepository shelters, DistrictRepository districts,
                          InspectionRepository inspections, Clock clock) {
        this.shelters = shelters;
        this.districts = districts;
        this.inspections = inspections;
        this.clock = clock;
    }

    public List<ShelterView> findAll() {
        return shelters.findAll().stream().map(ShelterView::from).toList();
    }

    public ShelterView findById(int id) {
        return ShelterView.from(getShelter(id));
    }

    public List<ShelterView> findByDistrict(int districtId) {
        return shelters.findByDistrictId(districtId).stream().map(ShelterView::from).toList();
    }

    public List<District> findAllDistricts() {
        return districts.findAll();
    }

    public List<Inspection> findInspections(int shelterId) {
        getShelter(shelterId);
        return inspections.findByShelterId(shelterId);
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

    /**
     * Обидві зміни (нова перевірка + новий стан укриття) виконуються в одній транзакції.
     * Замість ручних setAutoCommit/commit/rollback (ЛР2) — одна анотація @Transactional.
     */
    @Transactional
    public ShelterView registerInspection(int shelterId, int inspectorId, InspectionResult result, String notes) {
        Shelter shelter = getShelter(shelterId);
        LocalDate today = LocalDate.now(clock);
        inspections.save(new Inspection(null, shelterId, inspectorId, today, result, notes));
        shelter.applyInspection(result, today);
        shelters.save(shelter);
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
