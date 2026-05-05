package gui.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import network.HumanBeingEntry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Наблюдаемое локальное хранилище коллекции HumanBeing.
 * При каждом poll-тике метод {@link #sync(List)} делает diff и обновляет
 * содержимое: добавляет новые, удаляет исчезнувшие, обновляет изменённые.
 * Слушатели карты (например, канвас) получают per-key события.
 */
public class CollectionStore {

    private final ObservableMap<Long, HumanBeingFx> items = FXCollections.observableHashMap();

    public ObservableMap<Long, HumanBeingFx> items() {
        return items;
    }

    public int size() {
        return items.size();
    }

    /** Полная синхронизация с серверным снимком. */
    public void sync(List<HumanBeingEntry> entries) {
        Set<Long> incoming = new HashSet<>();
        for (HumanBeingEntry e : entries) {
            incoming.add(e.getKey());
            HumanBeingFx existing = items.get(e.getKey());
            if (existing == null) {
                items.put(e.getKey(), HumanBeingFx.from(e));
            } else {
                existing.applyFrom(e);
            }
        }
        items.keySet().removeIf(k -> !incoming.contains(k));
    }

    public void clear() {
        items.clear();
    }
}
