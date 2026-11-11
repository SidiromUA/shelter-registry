package ua.edu.ldubgd.shelters.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Район міста — сутність JPA (таблиця districts). */
@Entity
@Table(name = "districts")
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // значення генерує БД (SERIAL)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    protected District() {                                 // конструктор без параметрів потрібен JPA
    }

    public District(String name) {
        this.name = name;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
