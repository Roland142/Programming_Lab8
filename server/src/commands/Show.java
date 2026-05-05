package commands;

import elements.HumanBeing;
import managers.CollectionManager;
import network.Request;
import network.Response;

import java.util.List;
import java.util.stream.Collectors;

/** Команда show — возвращает все элементы коллекции, отсортированные по имени. */
public class Show extends Command {
    private final CollectionManager collectionManager;

    public Show(CollectionManager collectionManager) {
        super("show");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(Request request) {
        List<HumanBeing> sorted = collectionManager.getSortedByName();
        if (sorted.isEmpty()) return new Response("Коллекция пуста", true);
        String result = sorted.stream()
                .map(HumanBeing::toString)
                .collect(Collectors.joining("\n-----\n"));
        return new Response(result, true);
    }
}
