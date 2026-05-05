package commands;

import elements.HumanBeing;
import exceptions.InvalidDataException;
import managers.CollectionManager;
import managers.DatabaseManager;
import network.Request;
import network.Response;

import java.sql.SQLException;

/** Команда insert {key} — добавляет новый элемент с заданным ключом. */
public class Insert extends Command {
    private final CollectionManager collectionManager;
    private final DatabaseManager db;

    public Insert(CollectionManager collectionManager, DatabaseManager db) {
        super("insert");
        this.collectionManager = collectionManager;
        this.db = db;
    }

    @Override
    public Response execute(Request request) {
        String[] args = request.getArgs();
        HumanBeing obj = request.getHumanBeing();
        String login   = request.getLogin();

        if (args == null || args.length == 0)
            return new Response("Ошибка: укажите ключ. Использование: insert {key}", false);
        if (obj == null)
            return new Response("Ошибка: объект HumanBeing не передан", false);

        try {
            long key = Long.parseLong(args[0]);
            long id  = db.insertHumanBeing(key, obj, login);
            obj.setId(id);
            collectionManager.insert(key, obj, login);
            return new Response("Элемент добавлен: ключ=" + key + ", id=" + id, true);
        } catch (NumberFormatException e) {
            return new Response("Ошибка: ключ должен быть числом типа long", false);
        } catch (SQLException e) {
            return new Response("Ошибка БД: " + e.getMessage(), false);
        } catch (InvalidDataException e) {
            return new Response("Ошибка данных: " + e.getMessage(), false);
        }
    }
}
