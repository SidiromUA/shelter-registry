package ua.edu.ldubgd.shelters.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ua.edu.ldubgd.shelters.model.InspectionResult;

/** Тіло запиту POST /api/shelters/{id}/inspections. Інспектор — користувач, що виконав запит. */
public record InspectionRequest(
        @NotNull(message = "Вкажіть результат перевірки") InspectionResult result,
        @Size(max = 1000, message = "Примітки — не довше 1000 символів") String notes) {
}
