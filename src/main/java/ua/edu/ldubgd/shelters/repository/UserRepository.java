package ua.edu.ldubgd.shelters.repository;

import ua.edu.ldubgd.shelters.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsername(String username);
}
