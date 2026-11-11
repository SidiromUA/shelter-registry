package ua.edu.ldubgd.shelters.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Типізовані налаштування з блоку {@code registry:} у application.yml.
 * Якщо значення некоректне (наприклад, interval = 0), застосунок не запуститься — помилку видно одразу.
 */
@Validated
@ConfigurationProperties(prefix = "registry")
public record RegistryProperties(
        @NotBlank String city,
        @Min(1) int inspectionIntervalDays,
        @Email String contactEmail) {
}
