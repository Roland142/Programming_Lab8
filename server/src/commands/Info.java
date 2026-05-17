package commands;

import managers.CollectionManager;
import network.CollectionInfo;
import network.Request;
import network.Response;

/** Команда info — возвращает информацию о коллекции (тип, дата, размер). */
public class Info extends Command {
    private final CollectionManager collectionManager;

    public Info(CollectionManager collectionManager) {
        super("info", "info", "help.command.info", "вывести информацию о коллекции");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(Request request) {
        CollectionInfo payload = new CollectionInfo(
                collectionManager.getCollection().getClass().getName(),
                collectionManager.getCreationDate(),
                collectionManager.getCollection().size());
        String info = "Тип: " + payload.getType() + "\n" +
                      "Дата инициализации: " + payload.getCreationDate() + "\n" +
                      "Количество элементов: " + payload.getSize();
        return new Response(info, true, payload);
    }
}
