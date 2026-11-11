package ua.edu.ldubgd.shelters.repository;

import ua.edu.ldubgd.shelters.model.Shelter;

import java.util.List;
import java.util.Optional;

/**
 * Контракт доступу до укриттів. Сервіс залежить від інтерфейсу, а не від JDBC:
 * реалізацію можна замінити (JDBC → JPA у ЛР8) без змін у бізнес-логіці.
 */
public interface ShelterRepository {

    List<Shelter> findAll();

    Optional<Shelter> findById(int id);

    List<Shelter> findByDistrictId(int districtId);

    boolean existsByAddress(String address);

    Shelter save(Shelter shelter);

    boolean deleteById(int id);
}
