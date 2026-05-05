package commands;

import exceptions.EmptyCollectionException;
import managers.CollectionManager;
import managers.DatabaseManager;
import network.Request;
import network.Response;

import java.sql.SQLException;

/** Команда remove_key {key} — удаляет элемент по ключу. */
public class RemoveKey extends Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager db;

    public RemoveKey(CollectionManager collectionManager, DatabaseManager db) {
        super("remove_key");
        this.collectionManager = collectionManager;
        this.db = db;
    }

    @Override
    public Response execute(Request request) {
        String[] args = request.getArgs();
        String login  = request.getLogin();

        if (args == null || args.length == 0)
            return new Response("Ошибка: укажите ключ. Использование: remove_key {key}", false);

        try {
            long key = Long.parseLong(args[0]);
            boolean removed = db.removeByKey(key, login);
            if (!removed)
                return new Response("Элемент не найден или не принадлежит вам", false);
            collectionManager.removeKey(key);
            return new Response("Элемент с ключом " + key + " удалён", true);
        } catch (NumberFormatException e) {
            return new Response("Ошибка: ключ должен быть числом типа long", false);
        } catch (SQLException e) {
            return new Response("Ошибка БД: " + e.getMessage(), false);
        } catch (EmptyCollectionException e) {
            return new Response("Ошибка: " + e.getMessage(), false);
        }
    }
}
