package commands;

import managers.CollectionManager;
import managers.DatabaseManager;
import network.Request;
import network.Response;

import java.sql.SQLException;
import java.util.List;

/** Команда remove_all_by_minutes_of_waiting {minutes} — удаляет элементы пользователя с заданным minutesOfWaiting. */
public class RemoveAllByMinutesOfWaiting extends Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager db;

    public RemoveAllByMinutesOfWaiting(CollectionManager collectionManager, DatabaseManager db) {
        super("remove_all_by_minutes_of_waiting");
        this.collectionManager = collectionManager;
        this.db = db;
    }

    @Override
    public Response execute(Request request) {
        String[] args = request.getArgs();
        String login  = request.getLogin();

        if (args == null || args.length == 0)
            return new Response("Ошибка: укажите значение. Использование: remove_all_by_minutes_of_waiting {minutes}", false);

        try {
            int minutes = Integer.parseInt(args[0]);
            List<Long> removed = db.removeAllByMinutesOfWaiting(minutes, login);
            collectionManager.removeByKeys(removed);
            return new Response("Удалено элементов: " + removed.size(), true);
        } catch (NumberFormatException e) {
            return new Response("Ошибка: значение должно быть числом типа int", false);
        } catch (SQLException e) {
            return new Response("Ошибка БД: " + e.getMessage(), false);
        }
    }
}
