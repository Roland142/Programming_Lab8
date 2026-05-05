package commands;

import managers.CollectionManager;
import managers.DatabaseManager;
import network.Request;
import network.Response;

import java.sql.SQLException;
import java.util.List;

/** Команда remove_greater_key {key} — удаляет элементы пользователя с ключом больше заданного. */
public class RemoveGreaterKey extends Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager db;

    public RemoveGreaterKey(CollectionManager collectionManager, DatabaseManager db) {
        super("remove_greater_key");
        this.collectionManager = collectionManager;
        this.db = db;
    }

    @Override
    public Response execute(Request request) {
        String[] args = request.getArgs();
        String login  = request.getLogin();

        if (args == null || args.length == 0)
            return new Response("Ошибка: укажите ключ. Использование: remove_greater_key {key}", false);

        try {
            long key = Long.parseLong(args[0]);
            List<Long> removed = db.removeGreaterKey(key, login);
            collectionManager.removeByKeys(removed);
            return new Response("Удалено элементов: " + removed.size(), true);
        } catch (NumberFormatException e) {
            return new Response("Ошибка: ключ должен быть числом типа long", false);
        } catch (SQLException e) {
            return new Response("Ошибка БД: " + e.getMessage(), false);
        }
    }
}
