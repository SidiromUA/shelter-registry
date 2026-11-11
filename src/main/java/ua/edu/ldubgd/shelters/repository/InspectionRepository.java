package ua.edu.ldubgd.shelters.repository;

import ua.edu.ldubgd.shelters.model.Inspection;

import java.util.List;

public interface InspectionRepository {

    List<Inspection> findByShelterId(int shelterId);

    Inspection save(Inspection inspection);
}
