package com.jogogloria.utils;

import com.example.Biblioteca.lists.ArrayUnorderedList; // Import necessário
import java.util.Iterator;

public class SimpleMap<K, V> {

    // Adicionei esta classe interna que estava em falta
    private static class Entry<K, V> implements Comparable<Entry<K, V>> {
        K key;
        V value;
        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public int compareTo(Entry<K, V> o) {
            return 0;
        }
    }

    private ArrayUnorderedList<Entry<K, V>> entries;

    public SimpleMap() {
        this.entries = new ArrayUnorderedList<>();
    }

    public void put(K key, V value) {
        Iterator<Entry<K, V>> it = entries.iterator();
        while (it.hasNext()) {
            Entry<K, V> entry = it.next();
            if (entry.key.equals(key)) {
                entry.value = value;
                return;
            }
        }
        Entry<K, V> newEntry = new Entry<>(key, value);
        entries.addToRear(newEntry);
    }

    public V get(K key) {
        Iterator<Entry<K, V>> it = entries.iterator();
        while (it.hasNext()) {
            Entry<K, V> entry = it.next();
            if (entry.key.equals(key)) {
                return entry.value;
            }
        }
        return null;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public int size() {
        return entries.size();
    }
}