package ua.edu.ldubgd.shelters;

import ua.edu.ldubgd.shelters.controller.ShelterController;
import ua.edu.ldubgd.shelters.dto.CreateShelterRequest;
import ua.edu.ldubgd.shelters.repository.jdbc.JdbcDistrictRepository;
import ua.edu.ldubgd.shelters.repository.jdbc.JdbcInspectionRepository;
import ua.edu.ldubgd.shelters.repository.jdbc.JdbcShelterRepository;
import ua.edu.ldubgd.shelters.service.ShelterService;
import ua.edu.ldubgd.shelters.view.ShelterListPage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ЛР3. Збирання застосунку «вручну»: створюємо об’єкти шарів і передаємо залежності через конструктори.
 * Результат роботи контролера — HTML-сторінка web/shelters.html.
 */
public class App {

    public static void main(String[] args) throws IOException {
        // Composition root: тут «з’єднуються» всі шари. У ЛР5 це зробить Spring.
        ShelterService service = new ShelterService(
                new JdbcShelterRepository(),
                new JdbcDistrictRepository(),
                new JdbcInspectionRepository());
        ShelterController controller = new ShelterController(service, new ShelterListPage());

        // Некоректний запит — сторінка з переліком помилок валідації
        String page = controller.create(new CreateShelterRequest("", "пл. Ринок, 10", 99, "CAVE", -5, false));
        Path errorsPage = Path.of("web", "shelters-errors.html");
        Files.writeString(errorsPage, page);
        System.out.println("Сторінку з помилками збережено: " + errorsPage.toAbsolutePath());

        // Звичайний перегляд списку
        Path listPage = Path.of("web", "shelters.html");
        Files.writeString(listPage, controller.list());
        System.out.println("Сторінку зі списком збережено: " + listPage.toAbsolutePath());
        System.out.println("Відкрийте її через jwebserver і перевірте доступність (Lighthouse / axe).");
    }
}
