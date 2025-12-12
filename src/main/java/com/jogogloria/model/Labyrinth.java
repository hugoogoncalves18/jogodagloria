package com.jogogloria.model;

import com.jogogloria.utils.ExtendedWeightedGraph;
import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.exceptions.EmptyCollectionException;
import com.jogogloria.utils.SimpleMap;
import com.example.Biblioteca.iterators.Iterator;

/**
 * Labyrinth implementado com Grafo Pesado (WeightedGraph).
 *
 * @author Hugo Gonçalves
 * @version 5.0
 */
public class Labyrinth {

    // --- Constantes de Peso ---
    private static final double COST_OPEN = 1.0;
    private static final double COST_LOCKED = 10000.0;

    // --- Estruturas ---

    // O Teu WeightedGraph
    public final ExtendedWeightedGraph<String> graphStructure;

    // Mapa auxiliar para guardar os dados das Salas (Room)
    private final SimpleMap<String, Room> roomMap;

    //Lista para iterar sobre as salas de forma sequencial
    private final ArrayUnorderedList<Room> allRooms;

    // Auxiliares de Jogo
    private final ArrayUnorderedList<String> entryPoints;
    private String startRoomId;
    private String endRoomId;

    public Labyrinth() {
        this.graphStructure = new ExtendedWeightedGraph<>();
        this.roomMap = new SimpleMap<>();
        this.allRooms = new ArrayUnorderedList<>();
        this.entryPoints = new ArrayUnorderedList<>();
    }

    /**
     * Adiciona uma sala (Vértice).
     */
    public void addRoom(Room room) {
        if (room == null) return;
        String id = room.getId();

        if (!roomMap.containsKey(id)) {
            roomMap.put(id, room);
            graphStructure.addVertex(id);
            allRooms.addToRear(room);
        }
    }

    /**
     * Método essencial para o GameEngine percorrer as alavancas
     */
    public Iterator<Room> getRoomsIterator() {
        return allRooms.iterator();
    }

    /**
     * Adiciona uma conexão (Aresta) entre duas salas.
     * Por defeito, cria-se aberta (Peso 1.0).
     */
    public void addConnection(Room rA, Room rB) {
        if (rA == null || rB == null) return;

        graphStructure.addEdge(rA.getId(), rB.getId(), COST_OPEN);
    }

    /**
     * Tranca ou destranca uma passagem alterando o PESO da aresta.
     */
    public void setConnectionLocked(String idA, String idB, boolean locked) {
        double weight = locked ? COST_LOCKED : COST_OPEN;
        graphStructure.addEdge(idA, idB, weight);
    }

    /**
     * Verifica se o movimento é válido.
     * Agora consulta o PESO da aresta no grafo.
     */
    public boolean isValidMove(String fromId, String toId) {
        // 1. Verifica o peso da aresta entre as duas salas vizinhas
        double weight = graphStructure.shortestPathWeight(fromId, toId);

        // 2. Se for Infinito, não há conexão (Parede)
        if (weight == Double.POSITIVE_INFINITY) return false;

        // 3. Se for >= COST_LOCKED, está trancado
        if (weight >= COST_LOCKED) return false;

        // 4. Caso contrário (peso 1.0), é válido
        return true;
    }

    /**
     * Verifica se uma conexão está trancada (usado pela GUI para desenhar portas).
     */
    public boolean isLocked(String idA, String idB) {
        double weight = graphStructure.getEdgeWeight(idA, idB);
        return weight >= COST_LOCKED && weight != Double.POSITIVE_INFINITY;
    }

    //Metodo getNeighbors

    /**
     * Obtém a lista de vizinhos diretos de uma sala.
     * Agora delega diretamente para a subclasse do grafo.
     */
    public ArrayUnorderedList<String> getNeighbors(String roomId) {
        // Chama o método que criaste na ExtendedWeightedGraph
        return graphStructure.getNeighbors(roomId);
    }

    // --- Pathfinding & Iteradores ---

    public Iterator<String> getShortestPath(String startId, String targetId) {
        try {
            return graphStructure.iteratorShortestPath(startId, targetId);
        } catch (EmptyCollectionException | IllegalArgumentException e) {
            return new ArrayUnorderedList<String>().iterator();
        }
    }

    public Iterator<String> iteratorBFS(String startId) throws EmptyCollectionException {
        return graphStructure.iteratorBFS(startId);
    }

    // --- Getters Simples ---

    public Room getRoom(String id) { return roomMap.get(id); }
    public Room getRoomAt(int x, int y) { return roomMap.get(x + "-" + y); }

    public void setStartRoom(String id) { this.startRoomId = id; }
    public String getStartRoomId() { return startRoomId; }
    public void setTreasureRoom(String id) { this.endRoomId = id; }
    public String getTreasureRoom() { return endRoomId; }
    public void addEntryPoint(String id) { entryPoints.addToRear(id); }
    public ArrayUnorderedList<String> getEntryPoints() { return entryPoints; }
}