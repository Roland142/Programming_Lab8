package commands;

import elements.HumanBeing;
import managers.CollectionManager;
import network.HumanBeingEntry;
import network.Request;
import network.Response;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Команда show — возвращает все элементы коллекции.
 * В message — текстовый дамп для CLI-клиента (как раньше).
 * В payload — List&lt;HumanBeingEntry&gt; со всеми ключами и владельцами для GUI-клиента.
 */
public class Show extends Command {
    private final CollectionManager collectionManager;

    public Show(CollectionManager collectionManager) {
        super("show");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(Request request) {
        List<HumanBeingEntry> entries;
        collectionManager.lock();
        try {
            TreeMap<Long, HumanBeing> snapshot = collectionManager.getCollection();
            entries = snapshot.entrySet().stream()
                    .sorted(Comparator.comparing(e -> e.getValue().getName()))
                    .map(e -> new HumanBeingEntry(
                            e.getKey(),
                            e.getValue(),
                            collectionManager.getOwner(e.getKey())))
                    .collect(Collectors.toCollection(ArrayList::new));
        } finally {
            collectionManager.unlock();
        }

        if (entries.isEmpty()) {
            return new Response("Коллекция пуста", true, new ArrayList<HumanBeingEntry>());
        }

        String message = entries.stream()
                .map(e -> e.getHumanBeing().toString())
                .collect(Collectors.joining("\n-----\n"));
        return new Response(message, true, entries);
    }
}
