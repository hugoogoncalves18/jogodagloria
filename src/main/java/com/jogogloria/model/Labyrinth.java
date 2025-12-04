package com.jogogloria.model;

import com.example.Biblioteca.Graphs.AdjListGraph;
import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.exceptions.EmptyCollectionException;
import com.jogogloria.utils.SimpleMap; // Assumindo que existe
import java.util.Iterator;
import com.jogogloria.model.Lever;

public class Labyrinth {
    // Grafo principal: Vértices = IDs das Salas, Arestas = Corredores
    public final AdjListGraph<String> graphStructure;

    // Mapas para acesso rápido O(1) aos objetos de dados
    private final SimpleMap<String, Room> roomMap;
    private final SimpleMap<String, Corridor> corridorMap;
    private final SimpleMap<String, Lever> leverMap;

    private final ArrayUnorderedList<String> entryPoints;
    private String startRoomId;
    private String endRoomId;

    public Labyrinth() {
        this.graphStructure = new AdjListGraph<>();
        this.roomMap = new SimpleMap<>();
        this.corridorMap = new SimpleMap<>();
        this.entryPoints = new ArrayUnorderedList<>();
        this.leverMap = new SimpleMap<>();
    }

    /**
     * Adiciona uma sala ao grafo e ao mapa de lookup.
     */
    public void addRoom(Room room) {
        if (room == null) return;

        String id = room.getId();
        if (!roomMap.containsKey(id)) {
            roomMap.put(id, room);
            graphStructure.addVertex(id);
        }
    }

    /**
     * Cria e adiciona um corredor entre duas salas baseando-se nos seus IDs.
     * Gera uma chave canónica (ex: "A-B" e não "B-A") para evitar duplicados no mapa.
     */
    public void addCorridor(String idA, String idB) {
        if (!roomMap.containsKey(idA) || !roomMap.containsKey(idB)) return;

        // Adiciona aresta ao Grafo (bidirecional)
        graphStructure.addEdge(idA, idB);

        // Cria o objeto de dados Corridor
        String canonicalKey = getCanonicalCorridorKey(idA, idB);
        Corridor corridor = new Corridor(canonicalKey, idA, idB);
        corridorMap.put(canonicalKey, corridor);
    }

    public void addLever(String roomId, Lever lever) {
        leverMap.put(roomId, lever);
    }

    public Lever getLever(String roomId) {
        return leverMap.get(roomId);
    }

    /**
     * Retorna a Sala baseada nas coordenadas X e Y.
     * Útil para o MapLoader e para o BoardPanel (interface gráfica).
     */
    public Room getRoomAt(int x, int y) {
        String id = x + "-" + y; // Formato padrão acordado
        return roomMap.get(id);
    }

    public Room getRoom(String id) {
        return roomMap.get(id);
    }

    public Corridor getCorridor(String idA, String idB) {
        return corridorMap.get(getCanonicalCorridorKey(idA, idB));
    }

    // --- Métodos de Grafo e Pathfinding (Para os Bots) ---

    /**
     * Retorna um iterador com o caminho mais curto entre A e B.
     * Essencial para a IA dos Bots.
     */
    public Iterator<String> getShortestPath(String startId, String targetId) {
        try {
            return graphStructure.iteratorShortestPath(startId, targetId);
        } catch (EmptyCollectionException | IllegalArgumentException e) {
            // Retorna iterador vazio em caso de erro para não quebrar o jogo
            return new ArrayUnorderedList<String>().iterator();
        }
    }

    /**
     * Retorna vizinhos imediatos (para validar movimento humano).
     */
    public ArrayUnorderedList<String> getNeighbors(String roomId) {
        try {
            return graphStructure.getAdjacencyList(roomId);
        } catch (Exception e) {
            return new ArrayUnorderedList<>();
        }
    }

    public Iterator<String> iteratorBFS(String startId) throws EmptyCollectionException {
        // Delega para o método já existente na tua biblioteca de grafos
        return graphStructure.iteratorBFS(startId);
    }

    // --- Helpers ---

    public void setStartRoom(String id) { this.startRoomId = id; }
    public String getStartRoomId() { return startRoomId; }

    public void setTreasureRoom(String id) { this.endRoomId = id; }
    public String getTreasureRoom() { return endRoomId; }

    public void addEntryPoint(String id) { entryPoints.addToRear(id); }
    public ArrayUnorderedList<String> getEntryPoints() { return entryPoints; }

    private String getCanonicalCorridorKey(String idA, String idB) {
        return (idA.compareTo(idB) < 0) ? idA + "-" + idB : idB + "-" + idA;
    }
}