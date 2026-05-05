package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Работа с таблицей users: регистрация и аутентификация. */
public class UserRepository {

    private final PreparedStatement insertUser;
    private final PreparedStatement findUser;

    public UserRepository(Connection connection) throws SQLException {
        insertUser = connection.prepareStatement(
                "INSERT INTO users(login, password) VALUES(?, ?)");
        findUser = connection.prepareStatement(
                "SELECT password FROM users WHERE login = ?");
    }

    public synchronized boolean register(String login, String passwordHash) throws SQLException {
        findUser.setString(1, login);
        try (ResultSet rs = findUser.executeQuery()) {
            if (rs.next()) return false;
        }
        insertUser.setString(1, login);
        insertUser.setString(2, passwordHash);
        insertUser.executeUpdate();
        return true;
    }

    public synchronized boolean authenticate(String login, String passwordHash) throws SQLException {
        findUser.setString(1, login);
        try (ResultSet rs = findUser.executeQuery()) {
            if (rs.next()) {
                return passwordHash.equals(rs.getString("password"));
            }
        }
        return false;
    }
}
