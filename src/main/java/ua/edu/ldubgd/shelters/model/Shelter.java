package ua.edu.ldubgd.shelters.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Укриття — сутність JPA. Hibernate сам формує SQL для читання й запису,
 * а зв’язки між таблицями описуються анотаціями.
 */
@Entity
@Table(name = "shelters")
public class Shelter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true)
    private String address;

    /** Багато укриттів → один район. LAZY: район читається з БД лише тоді, коли до нього звертаються. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "district_id")
    private District district;

    @Enumerated(EnumType.STRING)                          // зберігати назву константи, а не її номер
    @Column(nullable = false, length = 30)
    private ShelterType type;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private boolean accessible;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShelterStatus status = ShelterStatus.NOT_READY;

    @Column(name = "last_inspection_date")
    private LocalDate lastInspectionDate;

    /** Одне укриття → багато перевірок. Видалення укриття видаляє і його перевірки. */
    @OneToMany(mappedBy = "shelter", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("date DESC")
    private List<Inspection> inspections = new ArrayList<>();

    protected Shelter() {
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

    /** Бізнес-правило: додати перевірку і змінити стан відповідно до її результату. */
    public Inspection addInspection(User inspector, LocalDate date, InspectionResult result, String notes) {
        Inspection inspection = new Inspection(this, inspector, date, result, notes);
        inspections.add(inspection);
        this.status = ShelterStatus.from(result);
        this.lastInspectionDate = date;
        return inspection;
    }

    public Integer getId() { return id; }
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
    public LocalDate getLastInspectionDate() { return lastInspectionDate; }
    public List<Inspection> getInspections() { return inspections; }
}
