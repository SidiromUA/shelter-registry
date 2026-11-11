package ua.edu.ldubgd.shelters.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ua.edu.ldubgd.shelters.model.Shelter;
import ua.edu.ldubgd.shelters.model.ShelterStatus;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тести шару доступу до даних: піднімається лише JPA (без вебчастини),
 * БД — H2 з профілю dev, схема і дані — з міграцій Flyway. Кожен тест відкочується.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ShelterRepositoryTest {

    @Autowired
    ShelterRepository repository;

    @Test
    void derivedQueryByDistrict() {
        List<Shelter> result = repository.findByDistrictIdOrderByName(6);   // Шевченківський
        assertThat(result).extracting(Shelter::getName).containsExactly("Укриття № 1");
    }

    @Test
    void pagination() {
        Page<Shelter> page = repository.findAll(PageRequest.of(0, 4, Sort.by("capacity").descending()));

        assertThat(page.getTotalElements()).isEqualTo(6);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(4);
        assertThat(page.getContent().getFirst().getCapacity()).isEqualTo(450);
    }

    @Test
    void jpqlOverdueQuery() {
        List<Shelter> overdue = repository.findInspectionOverdue(LocalDate.of(2027, 1, 10));
        // перевірок до 10.01.2027 або без перевірки: Укриття № 6 (null), Укриття № 4, Ліцей № 3
        assertThat(overdue).extracting(Shelter::getName)
                .containsExactly("Ліцей № 3", "Укриття № 4", "Укриття № 6");   // за назвою
    }

    @Test
    void existsByAddressIgnoresCurrentShelter() {
        assertThat(repository.existsByAddress("вул. Наукова, 7")).isTrue();
        assertThat(repository.existsByAddressAndIdNot("вул. Наукова, 7", 3)).isFalse();
    }

    @Test
    void pagedFilterByStatus() {
        Page<Shelter> ready = repository.findByStatus(ShelterStatus.READY, PageRequest.of(0, 10));
        assertThat(ready.getTotalElements()).isEqualTo(3);
        assertThat(repository.countByAccessibleTrue()).isEqualTo(3);
        assertThat(repository.totalCapacity(2)).isEqualTo(450);
    }
}
