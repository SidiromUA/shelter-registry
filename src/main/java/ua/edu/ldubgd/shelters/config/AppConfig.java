package ua.edu.ldubgd.shelters.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Явне оголошення бінів методами з анотацією @Bean.
 * Так створюють біни з класів, які ми не можемо позначити @Component (наприклад, класи JDK).
 */
@Configuration
public class AppConfig {

    /** Годинник для бізнес-логіки: у тестах його можна підмінити на фіксований час. */
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Europe/Kyiv"));
    }
}
