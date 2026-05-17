package commands;

import managers.CollectionManager;
import network.Request;
import network.Response;

import java.util.List;
import java.util.stream.Collectors;

/** Команда print_field_ascending_impact_speed — выводит значения impactSpeed в порядке возрастания. */
public class PrintFieldAscendingImpactSpeed extends Command {
    private final CollectionManager collectionManager;

    public PrintFieldAscendingImpactSpeed(CollectionManager collectionManager) {
        super("print_field_ascending_impact_speed", "print_field_ascending_impact_speed",
                "help.command.print_field_ascending_impact_speed",
                "вывести значения impactSpeed в порядке возрастания");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(Request request) {
        List<Double> speeds = collectionManager.getImpactSpeedsSorted();
        if (speeds.isEmpty()) return new Response("Коллекция пуста", true);
        String result = speeds.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
        return new Response(result, true);
    }
}
