package ua.edu.ldubgd.shelters.service;

import ua.edu.ldubgd.shelters.model.User;
import ua.edu.ldubgd.shelters.repository.UserRepository;

import java.util.Optional;

/**
 * Автентифікація за логіном і паролем.
 * УВАГА: паролі поки що зберігаються як «{noop}пароль» — лише для навчання.
 * У ЛР8 їх замінить BCrypt через Spring Security.
 */
public class AuthService {

    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
    }

    public Optional<User> authenticate(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        return users.findByUsername(username.trim())
                .filter(u -> u.passwordHash().equals("{noop}" + password));
    }
}
