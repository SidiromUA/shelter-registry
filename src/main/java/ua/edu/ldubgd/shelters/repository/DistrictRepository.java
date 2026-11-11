package ua.edu.ldubgd.shelters.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.edu.ldubgd.shelters.model.District;

import java.util.List;

public interface DistrictRepository extends JpaRepository<District, Integer> {

    List<District> findAllByOrderByName();
}
