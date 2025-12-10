package com.jogogloria.utils;

import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.iterators.Iterator;

/**
 * Implementação simples de HashMap que associa chaves a valores
 * Esta estrutura permite armazenar dois objetos e recuperar rapidamente o valor a partir da chave
 * Utiliza {@link ArrayUnorderedList} para armazenar as entradas, ao contrário de uma HashMap tradicional
 * esta estrutura requer uma iteração linear sobre a lista logo tem complexidade O(n)
 * @param <K> O tipo de dados de chave (Key)
 * @param <V> O tipo de dados do valor (Value) armazenado
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class SimpleMap<K, V> {

    /**
     * Classe interna que representa um par Chave-valor
     * @param <K> Tipo de chave
     * @param <V> Tipo de valor
     */
    private static class Entry<K, V> implements Comparable<Entry<K, V>> {
        K key;
        V value;

        /**
         * Cria uma nova entrada
         * @param key Chave única
         * @param value O valor associado
         */
        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Comparação baseada na estrutura
         * Necessário para a estrutura da interface da biblioteca
         * @param o Entrada a comparar
         * @return 0
         */
        @Override
        public int compareTo(Entry<K, V> o) {
            return 0;
        }
    }

    /** Lista interna que armazena as entradas do mapa */
    private ArrayUnorderedList<Entry<K, V>> entries;

    /**
     * Construtor padrão
     * Inicia o mapa ao criar a lista vazia
     */
    public SimpleMap() {
        this.entries = new ArrayUnorderedList<>();
    }

    /**
     * Insere um par chave-valor no mapa
     * Se a chave existir, o valor anterior é substituido pelo novo valor
     * Se a chave não existir, cria uma nova entrada e adiciona-a á lista
     * @param key A chave a ser inserida ou atualizada
     * @param value O valor a associa à chave
     */
    public void put(K key, V value) {
        Iterator<Entry<K, V>> it = entries.iterator();
        while (it.hasNext()) {
            Entry<K, V> entry = it.next();
            //Verifica se a chave já existe
            if (entry.key.equals(key)) {
                entry.value = value; //Atualiza o valor existente
                return;
            }
        }
        //Se não encontrou, cria nova entrada
        Entry<K, V> newEntry = new Entry<>(key, value);
        entries.addToRear(newEntry);
    }

    /**
     * Recupera o valor associado a uma determinada chave
     * @param key A chave cujo valor se pretende obter
     * @return O valor associado á chave, ou se a chave não existir no mapa
     */
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

    /**
     * Verifica se o mapa contém uma determinada chave
     * @param key A chave a procurar
     * @return true se a chave existir, false caso contrário
     */
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    /**
     * Retorna o número de entradas armazenadas no mapa
     * @return Tamanho atual do mapa
     */
    public int size() {
        return entries.size();
    }
}