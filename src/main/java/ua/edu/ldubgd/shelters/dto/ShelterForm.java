package ua.edu.ldubgd.shelters.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ua.edu.ldubgd.shelters.model.Shelter;
import ua.edu.ldubgd.shelters.model.ShelterType;

/**
 * Дані для створення/редагування укриття — і з HTML-форми (Thymeleaf), і з JSON (REST API).
 * Перевірку формату виконує Bean Validation за анотаціями (@Valid у контролері),
 * бізнес-правила (унікальна адреса, наявність району) — сервіс.
 */
public class ShelterForm {

    @NotBlank(message = "Вкажіть назву укриття")
    @Size(max = 150, message = "Назва — не довше 150 символів")
    private String name;

    @NotBlank(message = "Вкажіть адресу")
    @Size(max = 255, message = "Адреса — не довше 255 символів")
    private String address;

    @NotNull(message = "Оберіть район")
    private Integer districtId;

    @NotNull(message = "Оберіть тип укриття")
    private ShelterType type;

    @NotNull(message = "Вкажіть місткість")
    @Min(value = 1, message = "Місткість має бути не менше 1")
    @Max(value = 10_000, message = "Місткість — не більше 10 000 осіб")
    private Integer capacity;

    private boolean accessible;

    public static ShelterForm from(Shelter s) {
        ShelterForm f = new ShelterForm();
        f.setName(s.getName());
        f.setAddress(s.getAddress());
        f.setDistrictId(s.getDistrict().id());
        f.setType(s.getType());
        f.setCapacity(s.getCapacity());
        f.setAccessible(s.isAccessible());
        return f;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Integer getDistrictId() { return districtId; }
    public void setDistrictId(Integer districtId) { this.districtId = districtId; }
    public ShelterType getType() { return type; }
    public void setType(ShelterType type) { this.type = type; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public boolean isAccessible() { return accessible; }
    public void setAccessible(boolean accessible) { this.accessible = accessible; }
}
