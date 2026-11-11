package ua.edu.ldubgd.shelters.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.edu.ldubgd.shelters.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);
}
