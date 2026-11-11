package ua.edu.ldubgd.shelters.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ua.edu.ldubgd.shelters.service.ShelterService;

/**
 * Демонстрація життєвого циклу біна:
 * конструктор (DI) → @PostConstruct → … робота … → @PreDestroy під час зупинки контексту.
 * CommandLineRunner виконується один раз, коли застосунок повністю запущено.
 */
@Component
public class StartupReport implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupReport.class);

    private final ShelterService service;

    public StartupReport(ShelterService service) {
        this.service = service;
        log.info("1. Конструктор: Spring передав залежність ShelterService");
    }

    @PostConstruct
    void init() {
        log.info("2. @PostConstruct: усі залежності впроваджено");
    }

    @Override
    public void run(String... args) {
        log.info("3. Застосунок запущено. Укриттів у реєстрі: {}", service.findAll().size());
    }

    @PreDestroy
    void shutdown() {
        log.info("4. @PreDestroy: контекст закривається");
    }
}
