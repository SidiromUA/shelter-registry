package ua.edu.ldubgd.shelters.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ua.edu.ldubgd.shelters.config.RegistryProperties;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.ldubgd.shelters.dto.ShelterForm;
import ua.edu.ldubgd.shelters.dto.ShelterView;
import ua.edu.ldubgd.shelters.exception.NotFoundException;
import ua.edu.ldubgd.shelters.exception.ValidationException;
import ua.edu.ldubgd.shelters.model.District;
import ua.edu.ldubgd.shelters.model.Inspection;
import ua.edu.ldubgd.shelters.model.InspectionResult;
import ua.edu.ldubgd.shelters.model.Shelter;
import ua.edu.ldubgd.shelters.repository.DistrictRepository;
import ua.edu.ldubgd.shelters.repository.InspectionRepository;
import ua.edu.ldubgd.shelters.repository.ShelterRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Бізнес-логіка. Порівняйте з ЛР3: код майже той самий, але об’єкти тепер створює Spring —
 * @Service робить клас біном, а залежності надходять у конструктор автоматично.
 */
@Service
public class ShelterService {

    private static final Logger log = LoggerFactory.getLogger(ShelterService.class);

    private final ShelterRepository shelters;
    private final DistrictRepository districts;
    private final InspectionRepository inspections;
    private final Clock clock;
    private final RegistryProperties properties;

    public ShelterService(ShelterRepository shelters, DistrictRepository districts,
                          InspectionRepository inspections, Clock clock, RegistryProperties properties) {
        this.shelters = shelters;
        this.districts = districts;
        this.inspections = inspections;
        this.clock = clock;
        this.properties = properties;
    }

    /**
     * Укриття, які давно не перевірялися (інтервал задано в application.yml → registry.inspection-interval-days).
     */
    public List<ShelterView> findOverdueInspection() {
        LocalDate border = LocalDate.now(clock).minusDays(properties.inspectionIntervalDays());
        log.debug("Пошук укриттів без перевірки після {}", border);
        return shelters.findAll().stream()
                .filter(s -> s.getLastInspectionDate() == null || s.getLastInspectionDate().isBefore(border))
                .map(ShelterView::from)
                .toList();
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

    /** Дані укриття для форми редагування. */
    public ShelterForm getForm(int id) {
        return ShelterForm.from(getShelter(id));
    }

    /** Формат полів уже перевірено (@Valid), тут — лише бізнес-правила. */
    public ShelterView create(ShelterForm form) {
        District district = checkBusinessRules(form, null);
        Shelter saved = shelters.save(new Shelter(form.getName().trim(), form.getAddress().trim(), district,
                form.getType(), form.getCapacity(), form.isAccessible()));
        log.info("Створено укриття id={} «{}»", saved.getId(), saved.getName());
        return ShelterView.from(saved);
    }

    public ShelterView update(int id, ShelterForm form) {
        Shelter shelter = getShelter(id);
        District district = checkBusinessRules(form, id);
        shelter.setName(form.getName().trim());
        shelter.setAddress(form.getAddress().trim());
        shelter.setDistrict(district);
        shelter.setType(form.getType());
        shelter.setCapacity(form.getCapacity());
        shelter.setAccessible(form.isAccessible());
        shelters.save(shelter);
        log.info("Оновлено укриття id={}", id);
        return ShelterView.from(shelter);
    }

    private District checkBusinessRules(ShelterForm form, Integer currentId) {
        Map<String, String> errors = new LinkedHashMap<>();
        String address = form.getAddress().trim();
        if (shelters.existsByAddress(address, currentId)) {
            errors.put("address", "Укриття за адресою «" + address + "» вже зареєстровано");
        }
        District district = districts.findById(form.getDistrictId()).orElse(null);
        if (district == null) {
            errors.put("districtId", "Такого району немає");
        }
        if (!errors.isEmpty()) {
            log.warn("Порушено бізнес-правила: {}", errors);
            throw new ValidationException(errors);
        }
        return district;
    }

    public void delete(int id) {
        if (!shelters.deleteById(id)) {
            throw new NotFoundException("Укриття з id=" + id + " не знайдено");
        }
        log.info("Видалено укриття id={}", id);
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
}
