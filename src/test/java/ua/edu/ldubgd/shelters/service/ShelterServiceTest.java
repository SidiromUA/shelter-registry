package ua.edu.ldubgd.shelters.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ua.edu.ldubgd.shelters.config.RegistryProperties;
import ua.edu.ldubgd.shelters.dto.ShelterForm;
import ua.edu.ldubgd.shelters.dto.ShelterView;
import ua.edu.ldubgd.shelters.exception.NotFoundException;
import ua.edu.ldubgd.shelters.exception.ValidationException;
import ua.edu.ldubgd.shelters.model.District;
import ua.edu.ldubgd.shelters.model.InspectionResult;
import ua.edu.ldubgd.shelters.model.Shelter;
import ua.edu.ldubgd.shelters.model.ShelterStatus;
import ua.edu.ldubgd.shelters.model.ShelterType;
import ua.edu.ldubgd.shelters.model.User;
import ua.edu.ldubgd.shelters.repository.DistrictRepository;
import ua.edu.ldubgd.shelters.repository.InspectionRepository;
import ua.edu.ldubgd.shelters.repository.ShelterRepository;
import ua.edu.ldubgd.shelters.repository.UserRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Модульні тести сервісу: без Spring і без БД. Репозиторії замінено «заглушками» Mockito,
 * тому перевіряється лише бізнес-логіка, і тести виконуються за мілісекунди.
 */
@ExtendWith(MockitoExtension.class)
class ShelterServiceTest {

    @Mock ShelterRepository shelters;
    @Mock DistrictRepository districts;
    @Mock InspectionRepository inspections;
    @Mock UserRepository users;

    // Фіксований «сьогодні» — тест не залежить від дати запуску
    final Clock clock = Clock.fixed(Instant.parse("2027-03-01T10:00:00Z"), ZoneId.of("Europe/Kyiv"));
    final RegistryProperties properties = new RegistryProperties("Львів", 90, "cz@example.com");

    ShelterService service;
    District district;

    @BeforeEach
    void setUp() {
        service = new ShelterService(shelters, districts, inspections, users, clock, properties);
        district = new District("Франківський");
        ReflectionTestUtils.setField(district, "id", 5);
    }

    @Test
    @DisplayName("Не можна зареєструвати два укриття за однією адресою")
    void createRejectsDuplicateAddress() {
        when(shelters.existsByAddress("вул. Наукова, 7")).thenReturn(true);
        when(districts.findById(5)).thenReturn(Optional.of(district));

        assertThatThrownBy(() -> service.create(form("вул. Наукова, 7")))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors()).containsKey("address"));
        verify(shelters, never()).save(any());
    }

    @Test
    @DisplayName("Нове укриття отримує стан «Не готове» і зберігається")
    void createSavesShelter() {
        when(shelters.existsByAddress("вул. Нова, 1")).thenReturn(false);
        when(districts.findById(5)).thenReturn(Optional.of(district));
        when(shelters.save(any())).thenAnswer(inv -> {
            Shelter s = inv.getArgument(0);
            ReflectionTestUtils.setField(s, "id", 42);
            return s;
        });

        ShelterView created = service.create(form("  вул. Нова, 1  "));

        assertThat(created.id()).isEqualTo(42);
        assertThat(created.address()).isEqualTo("вул. Нова, 1");          // пробіли обрізано
        assertThat(created.statusCode()).isEqualTo(ShelterStatus.NOT_READY.name());
    }

    @Test
    @DisplayName("Успішна перевірка робить укриття готовим")
    void passedInspectionMakesShelterReady() {
        Shelter shelter = new Shelter("Укриття № 4", "просп. Червоної Калини, 60", district,
                ShelterType.BASEMENT, 80, false);
        ReflectionTestUtils.setField(shelter, "id", 4);
        when(shelters.findById(4)).thenReturn(Optional.of(shelter));
        when(users.findByUsername("inspector"))
                .thenReturn(Optional.of(new User("inspector", "x", "Петренко Олена", User.Role.INSPECTOR)));

        ShelterView result = service.registerInspection(4, "inspector", InspectionResult.PASSED, "ок");

        assertThat(result.statusCode()).isEqualTo("READY");
        assertThat(result.lastInspectionDate()).isEqualTo(LocalDate.of(2027, 3, 1));
        assertThat(shelter.getInspections()).hasSize(1);
    }

    @Test
    @DisplayName("Межа «прострочених» перевірок = сьогодні − інтервал з налаштувань")
    void overdueUsesConfiguredInterval() {
        when(shelters.findInspectionOverdue(LocalDate.of(2026, 12, 1))).thenReturn(List.of());

        assertThat(service.findOverdueInspection()).isEmpty();
        verify(shelters).findInspectionOverdue(LocalDate.of(2026, 12, 1));   // 01.03.2027 − 90 днів
    }

    @Test
    void unknownShelterThrowsNotFound() {
        when(shelters.findById(999)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(999)).isInstanceOf(NotFoundException.class);
    }

    private ShelterForm form(String address) {
        ShelterForm f = new ShelterForm();
        f.setName("Укриття");
        f.setAddress(address);
        f.setDistrictId(5);
        f.setType(ShelterType.BASEMENT);
        f.setCapacity(100);
        return f;
    }
}
