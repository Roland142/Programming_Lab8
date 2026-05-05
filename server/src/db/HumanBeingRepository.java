package db;

import elements.*;
import exceptions.InvalidDataException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/** CRUD-операции над таблицей human_beings. */
public class HumanBeingRepository {

    private static final Logger logger = Logger.getLogger("HumanBeingRepository");

    private final PreparedStatement loadAll;
    private final PreparedStatement insertHB;
    private final PreparedStatement updateHB;
    private final PreparedStatement psRemoveByKey;
    private final PreparedStatement psRemoveLower;
    private final PreparedStatement psRemoveGreaterKey;
    private final PreparedStatement psRemoveAllByMinutes;
    private final PreparedStatement psClearByUser;

    public HumanBeingRepository(Connection connection) throws SQLException {
        loadAll = connection.prepareStatement(
                "SELECT id, map_key, name, coord_x, coord_y, creation_date, real_hero, " +
                "has_toothpick, impact_speed, soundtrack_name, minutes_of_waiting, " +
                "mood, car_name, owner_login FROM human_beings");

        insertHB = connection.prepareStatement(
                "INSERT INTO human_beings(map_key, name, coord_x, coord_y, creation_date, " +
                "real_hero, has_toothpick, impact_speed, soundtrack_name, " +
                "minutes_of_waiting, mood, car_name, owner_login) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?) RETURNING id");

        updateHB = connection.prepareStatement(
                "UPDATE human_beings SET name=?, coord_x=?, coord_y=?, real_hero=?, " +
                "has_toothpick=?, impact_speed=?, soundtrack_name=?, " +
                "minutes_of_waiting=?, mood=?, car_name=? " +
                "WHERE id=? AND owner_login=?");

        psRemoveByKey = connection.prepareStatement(
                "DELETE FROM human_beings WHERE map_key=? AND owner_login=?");

        psRemoveLower = connection.prepareStatement(
                "DELETE FROM human_beings " +
                "WHERE owner_login=? AND (name < ? OR (name = ? AND id < ?)) " +
                "RETURNING map_key");

        psRemoveGreaterKey = connection.prepareStatement(
                "DELETE FROM human_beings WHERE map_key > ? AND owner_login=? RETURNING map_key");

        psRemoveAllByMinutes = connection.prepareStatement(
                "DELETE FROM human_beings WHERE minutes_of_waiting=? AND owner_login=? RETURNING map_key");

        psClearByUser = connection.prepareStatement(
                "DELETE FROM human_beings WHERE owner_login=? RETURNING map_key");
    }

    public synchronized TreeMap<Long, HumanBeing> loadCollection(Map<Long, String> ownerMap) throws SQLException {
        TreeMap<Long, HumanBeing> collection = new TreeMap<>();
        try (ResultSet rs = loadAll.executeQuery()) {
            while (rs.next()) {
                try {
                    long id               = rs.getLong("id");
                    long mapKey           = rs.getLong("map_key");
                    String name           = rs.getString("name");
                    double coordX         = rs.getDouble("coord_x");
                    int coordY            = rs.getInt("coord_y");
                    Date date             = rs.getDate("creation_date");
                    boolean realHero      = rs.getBoolean("real_hero");
                    Boolean hasToothpick  = rs.getObject("has_toothpick") != null
                                           ? rs.getBoolean("has_toothpick") : null;
                    double impactSpeed    = rs.getDouble("impact_speed");
                    String soundtrackName = rs.getString("soundtrack_name");
                    int minutesOfWaiting  = rs.getInt("minutes_of_waiting");
                    String moodStr        = rs.getString("mood");
                    String carName        = rs.getString("car_name");
                    String ownerLogin     = rs.getString("owner_login");

                    Mood mood = moodStr != null ? Mood.valueOf(moodStr) : null;
                    Car  car  = carName != null ? new Car(carName)      : null;

                    HumanBeing hb = new HumanBeing(name, new Coordinates(coordX, coordY),
                            realHero, hasToothpick, impactSpeed, soundtrackName,
                            minutesOfWaiting, mood, car);
                    hb.setId(id);
                    hb.setCreationDate(date);

                    collection.put(mapKey, hb);
                    ownerMap.put(mapKey, ownerLogin);
                } catch (InvalidDataException e) {
                    logger.warning("Пропущена некорректная запись из БД: " + e.getMessage());
                }
            }
        }
        return collection;
    }

    public synchronized long insert(long key, HumanBeing hb, String ownerLogin) throws SQLException {
        insertHB.setLong(1, key);
        insertHB.setString(2, hb.getName());
        insertHB.setDouble(3, hb.getCoordinates().getX());
        insertHB.setInt(4, hb.getCoordinates().getY());
        insertHB.setDate(5, new java.sql.Date(hb.getCreationDate().getTime()));
        insertHB.setBoolean(6, hb.getRealHero());
        if (hb.getHasToothpick() != null)
            insertHB.setBoolean(7, hb.getHasToothpick());
        else
            insertHB.setNull(7, Types.BOOLEAN);
        insertHB.setDouble(8, hb.getImpactSpeed());
        insertHB.setString(9, hb.getSoundtrackName());
        insertHB.setInt(10, hb.getMinutesOfWaiting());
        insertHB.setString(11, hb.getMood() != null ? hb.getMood().name() : null);
        insertHB.setString(12, hb.getCar() != null ? hb.getCar().getName() : null);
        insertHB.setString(13, ownerLogin);
        try (ResultSet rs = insertHB.executeQuery()) {
            rs.next();
            return rs.getLong("id");
        }
    }

    public synchronized boolean update(long id, HumanBeing hb, String ownerLogin) throws SQLException {
        updateHB.setString(1, hb.getName());
        updateHB.setDouble(2, hb.getCoordinates().getX());
        updateHB.setInt(3, hb.getCoordinates().getY());
        updateHB.setBoolean(4, hb.getRealHero());
        if (hb.getHasToothpick() != null)
            updateHB.setBoolean(5, hb.getHasToothpick());
        else
            updateHB.setNull(5, Types.BOOLEAN);
        updateHB.setDouble(6, hb.getImpactSpeed());
        updateHB.setString(7, hb.getSoundtrackName());
        updateHB.setInt(8, hb.getMinutesOfWaiting());
        updateHB.setString(9, hb.getMood() != null ? hb.getMood().name() : null);
        updateHB.setString(10, hb.getCar() != null ? hb.getCar().getName() : null);
        updateHB.setLong(11, id);
        updateHB.setString(12, ownerLogin);
        return updateHB.executeUpdate() > 0;
    }

    public synchronized boolean removeByKey(long key, String ownerLogin) throws SQLException {
        psRemoveByKey.setLong(1, key);
        psRemoveByKey.setString(2, ownerLogin);
        return psRemoveByKey.executeUpdate() > 0;
    }

    public synchronized List<Long> removeLower(HumanBeing hb, String ownerLogin) throws SQLException {
        psRemoveLower.setString(1, ownerLogin);
        psRemoveLower.setString(2, hb.getName());
        psRemoveLower.setString(3, hb.getName());
        psRemoveLower.setLong(4, hb.getId());
        return collectKeys(psRemoveLower.executeQuery());
    }

    public synchronized List<Long> removeGreaterKey(long key, String ownerLogin) throws SQLException {
        psRemoveGreaterKey.setLong(1, key);
        psRemoveGreaterKey.setString(2, ownerLogin);
        return collectKeys(psRemoveGreaterKey.executeQuery());
    }

    public synchronized List<Long> removeAllByMinutes(int minutes, String ownerLogin) throws SQLException {
        psRemoveAllByMinutes.setInt(1, minutes);
        psRemoveAllByMinutes.setString(2, ownerLogin);
        return collectKeys(psRemoveAllByMinutes.executeQuery());
    }

    public synchronized List<Long> clearByUser(String ownerLogin) throws SQLException {
        psClearByUser.setString(1, ownerLogin);
        return collectKeys(psClearByUser.executeQuery());
    }

    private List<Long> collectKeys(ResultSet rs) throws SQLException {
        List<Long> keys = new ArrayList<>();
        try (rs) {
            while (rs.next()) keys.add(rs.getLong("map_key"));
        }
        return keys;
    }
}
