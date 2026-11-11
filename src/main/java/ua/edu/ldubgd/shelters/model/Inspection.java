package ua.edu.ldubgd.shelters.model;

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
import jakarta.persistence.Table;

import java.time.LocalDate;

/** Перевірка укриття. Зв’язки: багато перевірок → одне укриття, багато перевірок → один інспектор. */
@Entity
@Table(name = "inspections")
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shelter_id")
    private Shelter shelter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inspector_id")
    private User inspector;

    @Column(name = "inspection_date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InspectionResult result;

    private String notes;

    protected Inspection() {
    }

    Inspection(Shelter shelter, User inspector, LocalDate date, InspectionResult result, String notes) {
        this.shelter = shelter;
        this.inspector = inspector;
        this.date = date;
        this.result = result;
        this.notes = notes;
    }

    public Integer getId() { return id; }
    public Shelter getShelter() { return shelter; }
    public User getInspector() { return inspector; }
    public LocalDate getDate() { return date; }
    public InspectionResult getResult() { return result; }
    public String getNotes() { return notes; }
}
