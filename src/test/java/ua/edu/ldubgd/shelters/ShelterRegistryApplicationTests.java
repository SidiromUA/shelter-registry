package ua.edu.ldubgd.shelters;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ua.edu.ldubgd.shelters.service.ShelterService;

import static org.assertj.core.api.Assertions.assertThat;

/** Перевіряє, що контекст Spring піднімається і всі біни створено (потрібна запущена PostgreSQL). */
@SpringBootTest
class ShelterRegistryApplicationTests {

    @Autowired
    ShelterService service;

    @Test
    void contextLoads() {
        assertThat(service).isNotNull();
    }
}
