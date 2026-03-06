import java.util.LinkedList;


public class GameHashTable {
    private class Entry {
        String key;
        Item value;
        public Entry(String key, Item value) { this.key = key; this.value = value; }
    }

    private int capacity = 20;
    private LinkedList<Entry>[] buckets;

    @SuppressWarnings("unchecked")
    public GameHashTable() {
        buckets = new LinkedList[capacity];
        for (int i = 0; i < capacity; i++) buckets[i] = new LinkedList<>();
    }

    private int getHash(String key) {
        return Math.abs(key.hashCode()) % capacity;
    }

    public void put(String key, Item value) {
        int index = getHash(key);
        for (Entry e : buckets[index]) {
            if (e.key.equals(key)) {
                e.value = value; 
                return;
            }
        }
        buckets[index].add(new Entry(key, value));
    }

    public Item get(String key) {
        int index = getHash(key);
        for (Entry e : buckets[index]) {
            if (e.key.equals(key)) return e.value;
        }
        return null;
    }
}