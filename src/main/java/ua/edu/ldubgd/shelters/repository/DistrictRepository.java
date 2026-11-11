package ua.edu.ldubgd.shelters.repository;

import ua.edu.ldubgd.shelters.model.District;

import java.util.List;
import java.util.Optional;

public interface DistrictRepository {

    List<District> findAll();

    Optional<District> findById(int id);
}
