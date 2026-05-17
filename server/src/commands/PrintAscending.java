package commands;

import elements.HumanBeing;
import managers.CollectionManager;
import network.Request;
import network.Response;

import java.util.stream.Collectors;

/** Команда print_ascending — выводит элементы в порядке возрастания. */
public class PrintAscending extends Command {
    private final CollectionManager collectionManager;

    public PrintAscending(CollectionManager collectionManager) {
        super("print_ascending", "print_ascending", "help.command.print_ascending",
                "вывести элементы в порядке возрастания");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(Request request) {
        if (collectionManager.getCollection().isEmpty())
            return new Response("Коллекция пуста", true);
        String result = collectionManager.getCollection().values().stream()
                .sorted()
                .map(HumanBeing::toString)
                .collect(Collectors.joining("\n---\n"));
        return new Response(result, true);
    }
}
