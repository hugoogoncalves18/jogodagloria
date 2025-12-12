package com.jogogloria.utils;

import com.example.Biblioteca.Graphs.WeightedGraph;
import com.example.Biblioteca.lists.ArrayUnorderedList;

/**
 * Extensão do Grafo para adicionar funcionalidades em falta (Vizinhos e Peso direto).
 *
 */
public class ExtendedWeightedGraph<T extends Comparable<T>> extends WeightedGraph<T> {

    /**
     * Obtém os vizinhos de um vértice consultando a matriz.
     * @param vertex O vértice de origem.
     * @return Lista de vértices conectados.
     */
    public ArrayUnorderedList<T> getNeighbors(T vertex) {
        ArrayUnorderedList<T> result = new ArrayUnorderedList<>();

        // Chama o getIndex da classe pai (WeightedGraph)
        int index = getIndex(vertex);

        // Chama o indexValid da classe pai
        if (!indexValid(index)) return result;

        for (int i = 0; i < numVertices; i++) {
            // Acede diretamente à matriz (protected)
            if (adjMatrix[index][i] < Double.POSITIVE_INFINITY) {
                result.addToRear(vertices[i]);
            }
        }
        return result;
    }

    /**
     * Obtém o peso direto da aresta entre dois vértices.
     * Essencial para verificar se uma porta está trancada sem correr Dijkstra.
     */
    public double getEdgeWeight(T vertex1, T vertex2) {
        int i = getIndex(vertex1);
        int j = getIndex(vertex2);

        if (!indexValid(i) || !indexValid(j)) return Double.POSITIVE_INFINITY;

        return adjMatrix[i][j];
    }
}