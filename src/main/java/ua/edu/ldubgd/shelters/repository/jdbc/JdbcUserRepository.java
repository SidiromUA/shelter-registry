package ua.edu.ldubgd.shelters.repository.jdbc;

import ua.edu.ldubgd.shelters.exception.DataAccessException;
import ua.edu.ldubgd.shelters.jdbc.Database;
import ua.edu.ldubgd.shelters.model.User;
import ua.edu.ldubgd.shelters.repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, full_name, role FROM users WHERE username = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("full_name"),
                        User.Role.valueOf(rs.getString("role"))));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Помилка читання користувача", e);
        }
    }
}
