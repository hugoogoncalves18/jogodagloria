package com.jogogloria.utils;

import java.util.Iterator;
import com.jogogloria.utils.LinkedQueue;
import com.example.Biblioteca.stacks.LinkedStack;
import com.jogogloria.utils.ArrayUnorderedList;
import com.jogogloria.utils.EmptyCollectionException;

/**
 * Representa um grafo não pesado utilizando uma estrutura de
 * lista de adjacências. Cada vértice é identificado pelo seu valor,
 * sendo armazenado num array, enquanto as ligações são guardadas
 * em listas não ordenadas correspondentes a cada índice.
 *
 * <p>Este grafo suporta:
 * <ul>
 *     <li>Inserção e remoção de vértices</li>
 *     <li>Inserção e remoção de arestas</li>
 *     <li>Travessias BFS (largura) e DFS (profundidade)</li>
 *     <li>Verificação de conectividade</li>
 * </ul>
 *
 * @param <T> Tipo dos elementos armazenados no grafo
 *            (deve implementar Comparable).
 */
public class AdjListGraph<T extends Comparable<T>> {

    /** Capacidade inicial dos arrays internos. */
    private final int DEFAULT_CAPACITY = 10;

    /** Número atual de vértices armazenados no grafo. */
    private int numVertices;

    /** Array que armazena os vértices do grafo. */
    private T[] vertices;

    /**
     * Array de listas de adjacência. Cada posição representa
     * um vértice e contém uma lista com os seus vizinhos.
     */
    private ArrayUnorderedList<T>[] adjList;

    /**
     * Constrói um grafo vazio, inicializando estrutura
     * interna com capacidade inicial fixa.
     */
    @SuppressWarnings("unchecked")
    public AdjListGraph() {
        numVertices = 0;
        vertices = (T[]) new Comparable[DEFAULT_CAPACITY];
        adjList = (ArrayUnorderedList<T>[]) new ArrayUnorderedList[DEFAULT_CAPACITY];

        for (int i = 0; i < DEFAULT_CAPACITY; i++)
            adjList[i] = new ArrayUnorderedList<>();
    }

    // -------------------------------------------------------
    // MÉTODOS AUXILIARES (privados)
    // -------------------------------------------------------

    /**
     * Obtém o índice associado a um dado vértice.
     *
     * @param vertex o vértice procurado
     * @return índice do vértice ou -1 caso não exista
     */
    private int getIndex(T vertex) {
        for (int i = 0; i < numVertices; i++)
            if (vertices[i].equals(vertex))
                return i;
        return -1;
    }

    /**
     * Verifica se um índice pertence ao intervalo válido.
     *
     * @param index índice a verificar
     * @return true se estiver dentro dos limites, false caso contrário
     */
    private boolean indexValid(int index) {
        return index >= 0 && index < numVertices;
    }

    /**
     * Duplica a capacidade das estruturas internas do grafo,
     * permitindo inserir novos vértices.
     */
    @SuppressWarnings("unchecked")
    private void expandCapacity() {
        // Expande array de vértices
        T[] newVertices = (T[]) new Comparable[vertices.length * 2];
        for (int i = 0; i < numVertices; i++)
            newVertices[i] = vertices[i];
        vertices = newVertices;

        // Expande a lista de adjacências
        ArrayUnorderedList<T>[] newAdj =
                (ArrayUnorderedList<T>[]) new ArrayUnorderedList[adjList.length * 2];

        for (int i = 0; i < newAdj.length; i++)
            newAdj[i] = new ArrayUnorderedList<>();

        for (int i = 0; i < numVertices; i++)
            newAdj[i] = adjList[i];

        adjList = newAdj;
    }

    // -------------------------------------------------------
    // INSERÇÃO E REMOÇÃO DE VÉRTICES / ARESTAS
    // -------------------------------------------------------

    /**
     * Adiciona um novo vértice ao grafo. Caso a capacidade atual
     * esteja esgotada, esta é expandida automaticamente.
     *
     * @param vertex o vértice a inserir
     */
    public void addVertex(T vertex) {
        if (numVertices == vertices.length)
            expandCapacity();

        vertices[numVertices] = vertex;
        adjList[numVertices] = new ArrayUnorderedList<>();
        numVertices++;
    }

    /**
     * Cria uma aresta não direcionada entre dois vértices.
     * Caso algum deles não exista, será criado automaticamente.
     *
     * @param v1 vértice 1
     * @param v2 vértice 2
     */
    public void addEdge(T v1, T v2) {
        int i1 = getIndex(v1);
        int i2 = getIndex(v2);

        if (i1 == -1) { addVertex(v1); i1 = numVertices - 1; }
        if (i2 == -1) { addVertex(v2); i2 = numVertices - 1; }

        adjList[i1].addToRear(v2);
        adjList[i2].addToRear(v1);
    }

    /**
     * Remove a aresta entre dois vértices, caso esta exista.
     *
     * @param v1 vértice 1
     * @param v2 vértice 2
     */
    public void removeEdge(T v1, T v2) throws NoElementFoundException {
        int i1 = getIndex(v1);
        int i2 = getIndex(v2);

        if (!indexValid(i1) || !indexValid(i2)) return;

        adjList[i1].remove(v2);
        adjList[i2].remove(v1);
    }

    /**
     * Remove um vértice do grafo, bem como todas as arestas que o incluem.
     *
     * @param vertex vértice a remover
     */
    public void removeVertex(T vertex) throws NoElementFoundException {
        int index = getIndex(vertex);
        if (index == -1) return;

        // Remover referências ao vértice
        for (int i = 0; i < numVertices; i++)
            adjList[i].remove(vertex);

        // Reorganizar arrays internos
        for (int i = index; i < numVertices - 1; i++) {
            vertices[i] = vertices[i + 1];
            adjList[i] = adjList[i + 1];
        }

        numVertices--;
    }

    // -------------------------------------------------------
    // BFS - Breadth First Search
    // -------------------------------------------------------

    /**
     * Devolve um iterador que realiza uma travessia BFS (em largura)
     * a partir de um vértice inicial.
     *
     * @param start vértice de início
     * @return iterador com ordem de visita
     */
    public Iterator<T> iteratorBFS(T start) throws EmptyCollectionException {
        int startIndex = getIndex(start);

        ArrayUnorderedList<T> result = new ArrayUnorderedList<>();
        if (!indexValid(startIndex)) return result.iterator();

        boolean[] visited = new boolean[numVertices];
        LinkedQueue<Integer> queue = new LinkedQueue<>();

        visited[startIndex] = true;
        queue.enqueue(startIndex);

        while (!queue.isEmpty()) {
            int current = queue.dequeue();
            result.addToRear(vertices[current]);

            for (T neigh : adjList[current]) {
                int idx = getIndex(neigh);
                if (!visited[idx]) {
                    visited[idx] = true;
                    queue.enqueue(idx);
                }
            }
        }

        return result.iterator();
    }

    // -------------------------------------------------------
    // DFS - Depth First Search
    // -------------------------------------------------------

    /**
     * Devolve um iterador que realiza uma travessia DFS (em profundidade)
     * a partir de um vértice inicial.
     *
     * @param start vértice inicial
     * @return iterador com ordem de visita
     */
    public Iterator<T> iteratorDFS(T start) throws com.example.Biblioteca.exceptions.EmptyCollectionException {
        int startIndex = getIndex(start);

        ArrayUnorderedList<T> result = new ArrayUnorderedList<>();
        if (!indexValid(startIndex)) return result.iterator();

        boolean[] visited = new boolean[numVertices];
        LinkedStack<Integer> stack = new LinkedStack<>();

        stack.push(startIndex);

        while (!stack.isEmpty()) {
            int current = stack.pop();

            if (!visited[current]) {
                visited[current] = true;
                result.addToRear(vertices[current]);

                // Copiar vizinhos para array temporário (ordem inversa)
                ArrayUnorderedList<T> neighs = adjList[current];
                int count = neighs.size();
                Object[] temp = new Object[count];

                int k = 0;
                for (T n : neighs)
                    temp[k++] = n;

                for (int i = temp.length - 1; i >= 0; i--) {
                    int idx = getIndex((T) temp[i]);
                    if (!visited[idx]) stack.push(idx);
                }
            }
        }

        return result.iterator();
    }

    // -------------------------------------------------------
    // SHORTEST PATH (BFS)
    // -------------------------------------------------------

    /**
     * Calcula o caminho mais curto entre dois vértices usando o algoritmo BFS.
     * Este método assume que o grafo é não pesado, pelo que a BFS garante
     * automaticamente o menor número de arestas percorridas.
     *
     * <p>O método utiliza:
     * <ul>
     *     <li>um array {@code visited[]} para marcar os vértices visitados</li>
     *     <li>um array {@code parent[]} para reconstruir o caminho final</li>
     *     <li>uma {@code LinkedQueue} para a travessia em largura</li>
     * </ul>
     *
     * @param startVertex vértice de origem
     * @param targetVertex vértice de destino
     * @return um iterador contendo o caminho mais curto
     *         (ou um iterador vazio caso não exista caminho)
     */
    public Iterator<T> iteratorShortestPath(T startVertex, T targetVertex) throws EmptyCollectionException, com.example.Biblioteca.exceptions.EmptyCollectionException {
        int start = getIndex(startVertex);
        int target = getIndex(targetVertex);

        ArrayUnorderedList<T> pathList = new ArrayUnorderedList<>();

        // Se vértices não existem → caminho vazio
        if (!indexValid(start) || !indexValid(target))
            return pathList.iterator();

        boolean[] visited = new boolean[numVertices];
        int[] parent = new int[numVertices];

        for (int i = 0; i < numVertices; i++)
            parent[i] = -1;

        LinkedQueue<Integer> queue = new LinkedQueue<>();

        visited[start] = true;
        queue.enqueue(start);

        boolean found = false;

        // ---------- BFS normal para descobrir caminho ----------
        while (!queue.isEmpty() && !found) {
            int current = queue.dequeue();

            // Chegámos ao destino → parar
            if (current == target) {
                found = true;
                break;
            }

            // Percorrer vizinhos através da lista de adjacências
            for (T neigh : adjList[current]) {
                int idx = getIndex(neigh);

                if (!visited[idx]) {
                    visited[idx] = true;
                    parent[idx] = current;
                    queue.enqueue(idx);
                }
            }
        }

        // ---------- Reconstrução do caminho ----------
        if (found) {
            LinkedStack<Integer> stack = new LinkedStack<>();
            int crawl = target;

            // Sobe pela cadeia de pais até ao início
            while (crawl != -1) {
                stack.push(crawl);
                crawl = parent[crawl];
            }

            // Passa do stack para o resultado (ordem correta)
            while (!stack.isEmpty()) {
                pathList.addToRear(vertices[stack.pop()]);
            }
        }

        return pathList.iterator();
    }


    // -------------------------------------------------------
    // VERIFICAR CONECTIVIDADE
    // -------------------------------------------------------

    /**
     * Determina se o grafo é conexo, ou seja, se todos os vértices
     * são alcançáveis a partir de um vértice inicial.
     *
     * @return true se o grafo for conexo, false caso contrário
     */
    public boolean isConnected() throws EmptyCollectionException {
        if (numVertices == 0) return true;

        Iterator<T> it = iteratorBFS(vertices[0]);
        int count = 0;
        while (it.hasNext()) { it.next(); count++; }

        return count == numVertices;
    }

    // -------------------------------------------------------
    // TO STRING
    // -------------------------------------------------------

    /**
     * Produz uma representação textual da lista de adjacências,
     * listando cada vértice seguido dos seus vizinhos diretos.
     *
     * @return string que representa a estrutura do grafo
     */
    public String toString() {
        StringBuilder sb = new StringBuilder("Lista de Adjacências:\n");

        for (int i = 0; i < numVertices; i++) {
            sb.append(vertices[i]).append(" -> ");
            Iterator<T> it = adjList[i].iterator();
            while (it.hasNext()) sb.append(it.next()).append(" ");
            sb.append("\n");
        }
        return sb.toString();
    }

    // -------------------------------------------------------
    // MÉTODOS SIMPLES: EMPTY & SIZE
    // -------------------------------------------------------

    /**
     * Indica se o grafo está vazio (sem vértices).
     *
     * @return true se o grafo estiver vazio
     */
    public boolean isEmpty() {
        return numVertices == 0;
    }

    /**
     * Devolve o número total de vértices presentes no grafo.
     *
     * @return número de vértices
     */
    public int size() {
        return numVertices;
    }

    /**
     * Retorna a lista de adjacência (vizinhos) do vértice especificado.
     * Esta função é essencial para saber os movimentos possíveis no Labyrinth.
     */
    public ArrayUnorderedList<T> getAdjacencyList(T vertex) {
        int index = this.getIndex(vertex);
        if (this.indexValid(index)) {
            return this.adjList[index];
        }
        // Retorna uma lista vazia se o vértice não for encontrado
        return new ArrayUnorderedList<T>();
    }
}
