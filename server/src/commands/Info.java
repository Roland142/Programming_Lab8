package commands;

import managers.CollectionManager;
import network.Request;
import network.Response;

/** Команда info — возвращает информацию о коллекции (тип, дата, размер). */
public class Info extends Command {
    private final CollectionManager collectionManager;

    public Info(CollectionManager collectionManager) {
        super("info");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(Request request) {
        String info = "Тип: " + collectionManager.getCollection().getClass().getName() + "\n" +
                      "Дата инициализации: " + collectionManager.getCreationDate() + "\n" +
                      "Количество элементов: " + collectionManager.getCollection().size();
        return new Response(info, true);
    }
}
