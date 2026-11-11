package ua.edu.ldubgd.shelters.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.ldubgd.shelters.config.RegistryProperties;
import ua.edu.ldubgd.shelters.dto.InspectionView;
import ua.edu.ldubgd.shelters.dto.ShelterForm;
import ua.edu.ldubgd.shelters.dto.ShelterView;
import ua.edu.ldubgd.shelters.exception.NotFoundException;
import ua.edu.ldubgd.shelters.exception.ValidationException;
import ua.edu.ldubgd.shelters.model.District;
import ua.edu.ldubgd.shelters.model.InspectionResult;
import ua.edu.ldubgd.shelters.model.Shelter;
import ua.edu.ldubgd.shelters.model.User;
import ua.edu.ldubgd.shelters.repository.DistrictRepository;
import ua.edu.ldubgd.shelters.repository.InspectionRepository;
import ua.edu.ldubgd.shelters.repository.ShelterRepository;
import ua.edu.ldubgd.shelters.repository.UserRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Бізнес-логіка реєстру. Репозиторії тепер — Spring Data JPA.
 * readOnly = true для читання: Hibernate не відстежує зміни сутностей, це швидше.
 */
@Service
@Transactional(readOnly = true)
public class ShelterService {

    private static final Logger log = LoggerFactory.getLogger(ShelterService.class);

    private final ShelterRepository shelters;
    private final DistrictRepository districts;
    private final InspectionRepository inspections;
    private final UserRepository users;
    private final Clock clock;
    private final RegistryProperties properties;

    public ShelterService(ShelterRepository shelters, DistrictRepository districts,
                          InspectionRepository inspections, UserRepository users,
                          Clock clock, RegistryProperties properties) {
        this.shelters = shelters;
        this.districts = districts;
        this.inspections = inspections;
        this.users = users;
        this.clock = clock;
        this.properties = properties;
    }

    /** Посторінковий список: Pageable містить номер сторінки, розмір і сортування. */
    public Page<ShelterView> findPage(Pageable pageable) {
        return shelters.findAll(pageable).map(ShelterView::from);
    }

    public List<ShelterView> findAll() {
        return shelters.findAll().stream().map(ShelterView::from).toList();
    }

    public List<ShelterView> findByDistrict(int districtId) {
        return shelters.findByDistrictIdOrderByName(districtId).stream().map(ShelterView::from).toList();
    }

    public ShelterView findById(int id) {
        return ShelterView.from(getShelter(id));
    }

    public List<District> findAllDistricts() {
        return districts.findAllByOrderByName();
    }

    public List<InspectionView> findInspections(int shelterId) {
        getShelter(shelterId);
        return inspections.findByShelterIdOrderByDateDesc(shelterId).stream().map(InspectionView::from).toList();
    }

    public List<ShelterView> findOverdueInspection() {
        LocalDate border = LocalDate.now(clock).minusDays(properties.inspectionIntervalDays());
        return shelters.findInspectionOverdue(border).stream().map(ShelterView::from).toList();
    }

    public ShelterForm getForm(int id) {
        return ShelterForm.from(getShelter(id));
    }

    @Transactional
    public ShelterView create(ShelterForm form) {
        District district = checkBusinessRules(form, null);
        Shelter saved = shelters.save(new Shelter(form.getName().trim(), form.getAddress().trim(), district,
                form.getType(), form.getCapacity(), form.isAccessible()));
        log.info("Створено укриття id={} «{}»", saved.getId(), saved.getName());
        return ShelterView.from(saved);
    }

    /**
     * Оновлення без явного save(): сутність, прочитана в транзакції, «керована» —
     * Hibernate сам запише зміни в БД під час фіксації транзакції (dirty checking).
     */
    @Transactional
    public ShelterView update(int id, ShelterForm form) {
        Shelter shelter = getShelter(id);
        District district = checkBusinessRules(form, id);
        shelter.setName(form.getName().trim());
        shelter.setAddress(form.getAddress().trim());
        shelter.setDistrict(district);
        shelter.setType(form.getType());
        shelter.setCapacity(form.getCapacity());
        shelter.setAccessible(form.isAccessible());
        log.info("Оновлено укриття id={}", id);
        return ShelterView.from(shelter);
    }

    /** Захист на рівні методу: навіть якщо хтось обійде перевірку URL, видалити зможе лише ADMIN. */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(int id) {
        Shelter shelter = getShelter(id);
        shelters.delete(shelter);
        log.info("Видалено укриття id={}", id);
    }

    /** Перевірку реєструє користувач, що увійшов до системи. */
    @Transactional
    public ShelterView registerInspection(int shelterId, String inspectorUsername,
                                          InspectionResult result, String notes) {
        Shelter shelter = getShelter(shelterId);
        User inspector = users.findByUsername(inspectorUsername)
                .orElseThrow(() -> new NotFoundException("Користувача " + inspectorUsername + " не знайдено"));
        shelter.addInspection(inspector, LocalDate.now(clock), result, notes);   // cascade = ALL збереже перевірку
        log.info("Перевірка укриття id={} користувачем {}: {}", shelterId, inspectorUsername, result);
        return ShelterView.from(shelter);
    }

    private District checkBusinessRules(ShelterForm form, Integer currentId) {
        Map<String, String> errors = new LinkedHashMap<>();
        String address = form.getAddress().trim();
        boolean duplicate = currentId == null
                ? shelters.existsByAddress(address)
                : shelters.existsByAddressAndIdNot(address, currentId);
        if (duplicate) {
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

    private Shelter getShelter(int id) {
        return shelters.findById(id)
                .orElseThrow(() -> new NotFoundException("Укриття з id=" + id + " не знайдено"));
    }
}
