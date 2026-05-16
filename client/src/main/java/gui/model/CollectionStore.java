package gui.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import network.HumanBeingEntry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Наблюдаемое локальное хранилище коллекции HumanBeing.
 * Хранит элементы в ObservableList (для TableView и канваса),
 * параллельно поддерживает Map по ключу для быстрого lookup
 *
 * При каждом poll-тике sync(List) делает diff и обновляет
 * содержимое: добавляет новые, обновляет существующие через HumanBeingFx.applyFrom, удаляет исчезнувшие
 */
public class CollectionStore {

    private final ObservableList<HumanBeingFx> items = FXCollections.observableArrayList();
    private final Map<Long, HumanBeingFx> byKey = new HashMap<>();

    public ObservableList<HumanBeingFx> items() {
        return items;
    }

    public HumanBeingFx getByKey(long key) {
        return byKey.get(key);
    }

    public int size() {
        return items.size();
    }

    /** Полная синхронизация с серверным снимком. */
    public void sync(List<HumanBeingEntry> entries) {
        Set<Long> incoming = new HashSet<>();
        for (HumanBeingEntry e : entries) {
            incoming.add(e.getKey());
            HumanBeingFx existing = byKey.get(e.getKey());
            if (existing == null) {
                HumanBeingFx fresh = HumanBeingFx.from(e);
                byKey.put(e.getKey(), fresh);
                items.add(fresh);
            } else {
                existing.applyFrom(e);
            }
        }
        items.removeIf(fx -> {
            if (!incoming.contains(fx.getKey())) {
                byKey.remove(fx.getKey());
                return true;
            }
            return false;
        });
    }

    public void clear() {
        items.clear();
        byKey.clear();
    }
}
