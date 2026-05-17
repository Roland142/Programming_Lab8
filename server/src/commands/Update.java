package commands;

import elements.HumanBeing;
import exceptions.EmptyCollectionException;
import exceptions.InvalidDataException;
import managers.CollectionManager;
import managers.DatabaseManager;
import network.Request;
import network.Response;

import java.sql.SQLException;

/** Команда update {id} — обновляет элемент с заданным id. */
public class Update extends Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager db;

    public Update(CollectionManager collectionManager, DatabaseManager db) {
        super("update", "update {id}", "help.command.update",
                "обновить элемент коллекции с заданным id");
        this.collectionManager = collectionManager;
        this.db = db;
    }

    @Override
    public Response execute(Request request) {
        String[] args = request.getArgs();
        HumanBeing obj = request.getHumanBeing();
        String login   = request.getLogin();

        if (args == null || args.length == 0)
            return new Response("Ошибка: укажите id. Использование: update {id}", false);
        if (obj == null)
            return new Response("Ошибка: объект HumanBeing не передан", false);

        try {
            long id = Long.parseLong(args[0]);
            boolean updated = db.updateHumanBeing(id, obj, login);
            if (!updated)
                return new Response("Элемент не найден или не принадлежит вам", false);
            collectionManager.update(id, obj);
            return new Response("Элемент с id=" + id + " обновлён", true);
        } catch (NumberFormatException e) {
            return new Response("Ошибка: id должен быть числом типа long", false);
        } catch (SQLException e) {
            return new Response("Ошибка БД: " + e.getMessage(), false);
        } catch (EmptyCollectionException | InvalidDataException e) {
            return new Response("Ошибка: " + e.getMessage(), false);
        }
    }
}
