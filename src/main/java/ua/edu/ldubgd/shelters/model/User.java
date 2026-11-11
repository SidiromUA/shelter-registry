package ua.edu.ldubgd.shelters.model;

/** Користувач системи. Пароль зберігається у вигляді хешу (поки що — з префіксом {noop}). */
public record User(Integer id, String username, String passwordHash, String fullName, Role role) {

    public enum Role { ADMIN, INSPECTOR }
}
