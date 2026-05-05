package commands;

import elements.HumanBeing;
import managers.CollectionManager;
import managers.DatabaseManager;
import network.Request;
import network.Response;

import java.sql.SQLException;
import java.util.List;

/** Команда remove_lower — удаляет элементы пользователя, меньшие заданного. */
public class RemoveLower extends Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager db;

    public RemoveLower(CollectionManager collectionManager, DatabaseManager db) {
        super("remove_lower");
        this.collectionManager = collectionManager;
        this.db = db;
    }

    @Override
    public Response execute(Request request) {
        HumanBeing obj = request.getHumanBeing();
        String login   = request.getLogin();

        if (obj == null)
            return new Response("Ошибка: объект для сравнения не передан", false);

        try {
            List<Long> removed = db.removeLower(obj, login);
            collectionManager.removeByKeys(removed);
            return new Response("Удалено элементов: " + removed.size(), true);
        } catch (SQLException e) {
            return new Response("Ошибка БД: " + e.getMessage(), false);
        }
    }
}
