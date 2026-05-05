package managers;

import java.sql.SQLException;

/**
 * Менеджер авторизации.
 * Принимает уже хешированные пароли (хеширование происходит на клиенте).
 */
public class AuthManager {

    private final DatabaseManager db;

    public AuthManager(DatabaseManager db) {
        this.db = db;
    }

    public boolean register(String login, String passwordHash) throws SQLException {
        return db.registerUser(login, passwordHash);
    }

    public boolean authenticate(String login, String passwordHash) throws SQLException {
        return db.authenticateUser(login, passwordHash);
    }
}
