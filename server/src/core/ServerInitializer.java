package core;

import commands.RegisterCommands;
import elements.HumanBeing;
import managers.AuthManager;
import managers.CollectionManager;
import managers.CommandManager;
import managers.DatabaseManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ServerInitializer {

    public final DatabaseManager db;
    public final CollectionManager collectionManager;
    public final AuthManager authManager;
    public final CommandManager commandManager;

    public ServerInitializer() throws SQLException, IOException {
        this.db = new DatabaseManager();
        this.collectionManager = loadCollection(db);
        this.authManager = new AuthManager(db);
        this.commandManager = new CommandManager();
        RegisterCommands.register(commandManager, collectionManager, db, authManager);
    }

    private static CollectionManager loadCollection(DatabaseManager db) throws SQLException {
        CollectionManager cm = new CollectionManager();
        Map<Long, String> ownerMap = new HashMap<>();
        TreeMap<Long, HumanBeing> loaded = db.loadCollection(ownerMap);
        for (Map.Entry<Long, HumanBeing> entry : loaded.entrySet()) {
            cm.insert(entry.getKey(), entry.getValue(), ownerMap.get(entry.getKey()));
        }
        return cm;
    }
}
