package managers;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import exceptions.EmptyCollectionException;
import exceptions.InvalidDataException;
import elements.*;
import java.time.LocalDate;

public class CollectionManager {
    private final TreeMap<Long, HumanBeing> collection = new TreeMap<>();
    private final Map<Long, String> ownerByKey = new HashMap<>();
    private final LocalDate creationDate;
    private final ReentrantLock lock = new ReentrantLock();

    public CollectionManager() {
        this.creationDate = LocalDate.now();
    }

    public void lock()   { lock.lock(); }
    public void unlock() { lock.unlock(); }

    public String getOwner(long key) {
        lock.lock();
        try {
            return ownerByKey.get(key);
        } finally {
            lock.unlock();
        }
    }

    public void setOwner(long key, String login) {
        lock.lock();
        try {
            ownerByKey.put(key, login);
        } finally {
            lock.unlock();
        }
    }

    public boolean isOwner(long key, String login) {
        lock.lock();
        try {
            return login.equals(ownerByKey.get(key));
        } finally {
            lock.unlock();
        }
    }

    public TreeMap<Long, HumanBeing> getCollection() {
        lock.lock();
        try {
            return this.collection;
        } finally {
            lock.unlock();
        }
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public List<HumanBeing> getSortedByName() {
        lock.lock();
        try {
            return collection.values().stream()
                    .sorted(Comparator.comparing(HumanBeing::getName))
                    .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    public List<HumanBeing> getSortedAscending() {
        lock.lock();
        try {
            return collection.values().stream()
                    .sorted()
                    .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    public List<Double> getImpactSpeedsSorted() {
        lock.lock();
        try {
            return collection.values().stream()
                    .mapToDouble(HumanBeing::getImpactSpeed)
                    .sorted()
                    .boxed()
                    .collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    public boolean isIDUsed(long id) {
        lock.lock();
        try {
            return collection.values().stream()
                    .anyMatch(hb -> hb.getId() == id);
        } finally {
            lock.unlock();
        }
    }

    public void insert(long key, HumanBeing hb, String ownerLogin) {
        lock.lock();
        try {
            collection.put(key, hb);
            ownerByKey.put(key, ownerLogin);
        } finally {
            lock.unlock();
        }
    }

    public void update(long id, HumanBeing newHb) throws EmptyCollectionException, InvalidDataException {
        lock.lock();
        try {
            if (collection.isEmpty()) throw new EmptyCollectionException();
            Map.Entry<Long, HumanBeing> entry = collection.entrySet().stream()
                    .filter(e -> e.getValue().getId() == id)
                    .findFirst()
                    .orElse(null);
            if (entry != null) {
                newHb.setId(id);
                newHb.setCreationDate(entry.getValue().getCreationDate());
                collection.put(entry.getKey(), newHb);
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeKey(long key) throws EmptyCollectionException {
        lock.lock();
        try {
            if (collection.isEmpty()) throw new EmptyCollectionException();
            collection.remove(key);
            ownerByKey.remove(key);
        } finally {
            lock.unlock();
        }
    }

    public void removeByKeys(List<Long> keys) {
        lock.lock();
        try {
            keys.forEach(key -> {
                collection.remove(key);
                ownerByKey.remove(key);
            });
        } finally {
            lock.unlock();
        }
    }
}
