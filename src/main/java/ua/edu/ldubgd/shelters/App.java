package ua.edu.ldubgd.shelters;

import ua.edu.ldubgd.shelters.jdbc.InspectionDao;
import ua.edu.ldubgd.shelters.jdbc.ShelterDao;
import ua.edu.ldubgd.shelters.jdbc.SqlInjectionDemo;

import java.sql.SQLException;
import java.time.LocalDate;

/** ЛР2. Консольна демонстрація роботи з БД через JDBC. */
public class App {

    public static void main(String[] args) throws SQLException {
        ShelterDao shelters = new ShelterDao();
        InspectionDao inspections = new InspectionDao();

        System.out.println("=== Усі укриття ===");
        shelters.findAll().forEach(System.out::println);

        System.out.println("\n=== Укриття Франківського району ===");
        shelters.findByDistrict("Франківський").forEach(System.out::println);

        System.out.println("\n=== Місткість за районами ===");
        shelters.capacityByDistrict().forEach((d, c) -> System.out.printf("%-16s %5d%n", d, c));

        System.out.println("\n=== Додавання та видалення ===");
        int id = shelters.insert("Тимчасове укриття", "вул. Тестова, 1", 1, "BASEMENT", 40, false);
        System.out.println("Додано: " + shelters.findById(id).orElseThrow());
        System.out.println("Видалено: " + shelters.delete(id));

        System.out.println("\n=== Транзакція: реєстрація перевірки ===");
        inspections.registerInspection(4, 2, LocalDate.now(), "PASSED", "Недоліки усунено");
        System.out.println("Після перевірки: " + shelters.findById(4).orElseThrow());

        try {
            inspections.registerInspection(999, 2, LocalDate.now(), "PASSED", null);
        } catch (SQLException e) {
            System.out.println("Помилка, транзакцію відкочено: " + e.getMessage());
        }

        System.out.println("\n=== SQL-ін’єкція ===");
        SqlInjectionDemo demo = new SqlInjectionDemo();
        String attack = "' OR '1'='1";
        System.out.println("Небезпечний запит з «" + attack + "»: " + demo.unsafeCount(attack) + " записів");
        System.out.println("PreparedStatement з тим самим рядком: " + demo.safeCount(attack) + " записів");
    }
}
