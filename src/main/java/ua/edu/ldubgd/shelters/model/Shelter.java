package ua.edu.ldubgd.shelters.model;

import java.time.LocalDate;

/** Укриття — основна сутність предметної області. */
public class Shelter {

    private Integer id;
    private String name;
    private String address;
    private District district;
    private ShelterType type;
    private int capacity;
    private boolean accessible;
    private ShelterStatus status = ShelterStatus.NOT_READY;
    private LocalDate lastInspectionDate;

    public Shelter() {
    }

    public Shelter(String name, String address, District district, ShelterType type,
                   int capacity, boolean accessible) {
        this.name = name;
        this.address = address;
        this.district = district;
        this.type = type;
        this.capacity = capacity;
        this.accessible = accessible;
    }

    /** Бізнес-правило: стан змінюється лише за результатом перевірки. */
    public void applyInspection(InspectionResult result, LocalDate date) {
        this.status = ShelterStatus.from(result);
        this.lastInspectionDate = date;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public District getDistrict() { return district; }
    public void setDistrict(District district) { this.district = district; }
    public ShelterType getType() { return type; }
    public void setType(ShelterType type) { this.type = type; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public boolean isAccessible() { return accessible; }
    public void setAccessible(boolean accessible) { this.accessible = accessible; }
    public ShelterStatus getStatus() { return status; }
    public void setStatus(ShelterStatus status) { this.status = status; }
    public LocalDate getLastInspectionDate() { return lastInspectionDate; }
    public void setLastInspectionDate(LocalDate date) { this.lastInspectionDate = date; }
}
