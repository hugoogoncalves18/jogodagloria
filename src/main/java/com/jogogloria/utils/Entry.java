package com.jogogloria.utils;

    //classe interna para guardar o par
    public class Entry<K, V> implements Comparable<Entry<K, V>> {
        K key;
        V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }

        //Necessário para instanciar o ArrayList
        @Override
        public int compareTo(Entry<K, V> o) {
            return 0;
        }
    }
