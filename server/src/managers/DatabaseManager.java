package managers;

import db.DatabaseConnection;
import db.HumanBeingRepository;
import db.UserRepository;
import elements.HumanBeing;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Фасад над UserRepository и HumanBeingRepository.
 * Сохраняет публичный API, чтобы AuthManager и команды не менялись.
 */
public class DatabaseManager {

    private final UserRepository users;
    private final HumanBeingRepository humanBeings;

    public DatabaseManager() throws SQLException, IOException {
        Connection connection = DatabaseConnection.open();
        this.users       = new UserRepository(connection);
        this.humanBeings = new HumanBeingRepository(connection);
    }

    // ---- Auth ---------------------------------------------------------------

    public boolean registerUser(String login, String passwordHash) throws SQLException {
        return users.register(login, passwordHash);
    }

    public boolean authenticateUser(String login, String passwordHash) throws SQLException {
        return users.authenticate(login, passwordHash);
    }

    // ---- Коллекция ----------------------------------------------------------

    public TreeMap<Long, HumanBeing> loadCollection(Map<Long, String> ownerMap) throws SQLException {
        return humanBeings.loadCollection(ownerMap);
    }

    public long insertHumanBeing(long key, HumanBeing hb, String ownerLogin) throws SQLException {
        return humanBeings.insert(key, hb, ownerLogin);
    }

    public boolean updateHumanBeing(long id, HumanBeing hb, String ownerLogin) throws SQLException {
        return humanBeings.update(id, hb, ownerLogin);
    }

    public boolean removeByKey(long key, String ownerLogin) throws SQLException {
        return humanBeings.removeByKey(key, ownerLogin);
    }

    public List<Long> removeLower(HumanBeing hb, String ownerLogin) throws SQLException {
        return humanBeings.removeLower(hb, ownerLogin);
    }

    public List<Long> removeGreaterKey(long key, String ownerLogin) throws SQLException {
        return humanBeings.removeGreaterKey(key, ownerLogin);
    }

    public List<Long> removeAllByMinutesOfWaiting(int minutes, String ownerLogin) throws SQLException {
        return humanBeings.removeAllByMinutes(minutes, ownerLogin);
    }

    public List<Long> clearByUser(String ownerLogin) throws SQLException {
        return humanBeings.clearByUser(ownerLogin);
    }
}
