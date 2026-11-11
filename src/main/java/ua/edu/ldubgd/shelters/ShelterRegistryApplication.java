package ua.edu.ldubgd.shelters;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Точка входу застосунку Spring Boot.
 * {@code @SpringBootApplication} = {@code @Configuration} + {@code @EnableAutoConfiguration} + {@code @ComponentScan}:
 * Spring знаходить класи з анотаціями @Component/@Service/@Repository/@Controller у цьому пакеті та підпакетах,
 * створює з них біни і сам передає їм залежності (впровадження залежностей, DI).
 */
@SpringBootApplication
@ConfigurationPropertiesScan          // знайти класи з @ConfigurationProperties (ЛР6)
public class ShelterRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShelterRegistryApplication.class, args);
    }
}
