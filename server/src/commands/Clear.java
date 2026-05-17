package commands;

import managers.CollectionManager;
import managers.DatabaseManager;
import network.Request;
import network.Response;

import java.sql.SQLException;
import java.util.List;

/** Команда clear — удаляет все элементы, принадлежащие текущему пользователю. */
public class Clear extends Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager db;

    public Clear(CollectionManager collectionManager, DatabaseManager db) {
        super("clear", "clear", "help.command.clear", "удалить все свои элементы");
        this.collectionManager = collectionManager;
        this.db = db;
    }

    @Override
    public Response execute(Request request) {
        String login = request.getLogin();
        try {
            List<Long> removed = db.clearByUser(login);
            collectionManager.removeByKeys(removed);
            return new Response("Удалено элементов: " + removed.size(), true);
        } catch (SQLException e) {
            return new Response("Ошибка БД: " + e.getMessage(), false);
        }
    }
}
