package com.jogogloria.model;

import com.example.Biblioteca.Graphs.AdjListGraph;
import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.exceptions.EmptyCollectionException;
import com.jogogloria.utils.SimpleMap;
import com.example.Biblioteca.iterators.Iterator;

/**
 * Representa a estrutura física e lógica do Labirinto.
 *
 * @author Hugo Gonçalves
 * @version 2.0
 */
public class Labyrinth {

    // --- Estruturas de Dados ---

    /**
     * O grafo que define a conexão entre as salas (Pathfinding).
     */
    public final AdjListGraph<String> graphStructure;

    /** Mapa para acesso O(1) às salas pelo seu ID. */
    private final SimpleMap<String, Room> roomMap;

    /** Mapa para acesso O(1) aos corredores pela sua chave canónica. */
    private final SimpleMap<String, Corridor> corridorMap;

    /** Lista de IDs das salas onde os jogadores começam o jogo. */
    private final ArrayUnorderedList<String> entryPoints;

    /** ID da sala inicial. */
    private String startRoomId;

    /** ID da sala do tesouro. */
    private String endRoomId;

    /**
     * Construtor padrão.
     */
    public Labyrinth() {
        this.graphStructure = new AdjListGraph<>();
        this.roomMap = new SimpleMap<>();
        this.corridorMap = new SimpleMap<>();
        this.entryPoints = new ArrayUnorderedList<>();
    }

    /**
     * Adiciona uma sala ao labirinto.
     * @param room O objeto Room a adicionar.
     */
    public void addRoom(Room room) {
        if (room == null) return;
        String id = room.getId();

        // Verifica se já existe para não duplicar
        if (!roomMap.containsKey(id)) {
            roomMap.put(id, room);
            graphStructure.addVertex(id);
        }
    }

    /**
     * Adiciona um corredor entre dois objetos Sala.
     *
     * @param rA Objeto da primeira sala.
     * @param rB Objeto da segunda sala.
     */
    public void addCorridor(Room rA, Room rB) {
        if (rA == null || rB == null) return;

        String idA = rA.getId();
        String idB = rB.getId();

        String canonicalKey = getCanonicalCorridorKey(idA, idB);
        if (corridorMap.containsKey(canonicalKey)) return;

        // 1. Grafo (Matemático)
        graphStructure.addEdge(idA, idB);

        // 2. Objeto Lógico (Referência Direta)
        Corridor corridor = new Corridor(canonicalKey, rA, rB);
        corridorMap.put(canonicalKey, corridor);
    }

    // Nota: O método addLever foi removido.
    // Agora deves fazer: room.setLever(lever) diretamente no MapLoader.

    /**
     * Obtém a alavanca localizada numa sala específica.
     *
     *  @param roomId ID da sala.
     * @return O objeto Lever ou {@code null}.
     */
    public Lever getLever(String roomId) {
        Room r = getRoom(roomId);
        if (r != null && r.hasLever()) {
            return r.getLever();
        }
        return null;
    }

    /**
     * Verifica se é possível mover de uma sala para a outra.
     */
    public boolean isValidMove(String fromId, String toId) {
        boolean isConnected = false;
        try {
            // CORREÇÃO: isConnected precisa de argumentos
            isConnected = graphStructure.isConnected();
        } catch (Exception e) { return false; }

        if (!isConnected) return false;

        Corridor c = getCorridor(fromId, toId);
        // Verifica se o corredor existe e se está aberto
        return c != null && !c.isLocked();
    }

    // --- Getters e Consultas ---

    public Room getRoomAt(int x, int y) {
        return roomMap.get(x + "-" + y);
    }

    public Room getRoom(String id) {
        return roomMap.get(id);
    }

    public Corridor getCorridor(String idA, String idB) {
        return corridorMap.get(getCanonicalCorridorKey(idA, idB));
    }

    public Iterator<String> getShortestPath(String startId, String targetId) {
        try {
            return graphStructure.iteratorShortestPath(startId, targetId);
        } catch (EmptyCollectionException | IllegalArgumentException e) {
            return new ArrayUnorderedList<String>().iterator();
        }
    }

    public ArrayUnorderedList<String> getNeighbors(String roomId) {
        try {
            return graphStructure.getAdjacencyList(roomId);
        } catch (Exception e) {
            return new ArrayUnorderedList<>();
        }
    }

    public Iterator<String> iteratorBFS(String startId) throws EmptyCollectionException {
        return graphStructure.iteratorBFS(startId);
    }

    // --- Setters de Configuração ---

    public void setStartRoom(String id) { this.startRoomId = id; }
    public String getStartRoomId() { return startRoomId; }
    public void setTreasureRoom(String id) { this.endRoomId = id; }
    public String getTreasureRoom() { return endRoomId; }
    public void addEntryPoint(String id) { entryPoints.addToRear(id); }
    public ArrayUnorderedList<String> getEntryPoints() { return entryPoints; }

    /**
     * Gera uma chave única para o corredor (independente da ordem A-B ou B-A).
     */
    private String getCanonicalCorridorKey(String idA, String idB) {
        return (idA.compareTo(idB) < 0) ? idA + "-" + idB : idB + "-" + idA;
    }
}