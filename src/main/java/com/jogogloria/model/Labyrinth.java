package com.jogogloria.model;

import com.example.Biblioteca.Graphs.AdjListGraph;
import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.exceptions.EmptyCollectionException;
import com.jogogloria.utils.SimpleMap;
import com.example.Biblioteca.iterators.Iterator;

public class Labyrinth {

    // --- Estruturas de Dados ---
    public final AdjListGraph<String> graphStructure;

    private final SimpleMap<String, Room> roomMap;
    private final SimpleMap<String, Corridor> corridorMap;
    private final SimpleMap<String, Lever> leverMap;

    // Listas Auxiliares (Para iteração sequencial)
    private final ArrayUnorderedList<Room> storedRooms;
    private final ArrayUnorderedList<Corridor> storedCorridors;
    private final ArrayUnorderedList<Lever> storedLevers; // [NOVO] Lista de alavancas

    private final ArrayUnorderedList<String> entryPoints;
    private String startRoomId;
    private String endRoomId;

    public Labyrinth() {
        this.graphStructure = new AdjListGraph<>();

        this.roomMap = new SimpleMap<>();
        this.corridorMap = new SimpleMap<>();
        this.leverMap = new SimpleMap<>();

        this.storedRooms = new ArrayUnorderedList<>();
        this.storedCorridors = new ArrayUnorderedList<>();
        this.storedLevers = new ArrayUnorderedList<>();
        this.entryPoints = new ArrayUnorderedList<>();
    }

    public void addRoom(Room room) {
        if (room == null) return;
        String id = room.getId();
        if (!roomMap.containsKey(id)) {
            roomMap.put(id, room);
            graphStructure.addVertex(id);
            storedRooms.addToRear(room);
        }
    }

    public void addCorridor(String idA, String idB) {
        if (!roomMap.containsKey(idA) || !roomMap.containsKey(idB)) return;
        String canonicalKey = getCanonicalCorridorKey(idA, idB);
        if (corridorMap.containsKey(canonicalKey)) return;

        graphStructure.addEdge(idA, idB);
        Corridor corridor = new Corridor(canonicalKey, idA, idB);
        corridorMap.put(canonicalKey, corridor);
        storedCorridors.addToRear(corridor);
    }

    public void addLever(String roomId, Lever lever) {
        leverMap.put(roomId, lever);
        storedLevers.addToRear(lever); // [NOVO] Guardar na lista
    }

    // [NOVO] Permite ao Bot iterar sobre todas as alavancas para encontrar uma útil
    public Iterator<Lever> getLeversIterator() {
        return storedLevers.iterator();
    }

    public boolean isValidMove(String fromId, String toId) {
        boolean isConnected = false;
        try {
            isConnected = graphStructure.isConnected();
        } catch (Exception e) { return false; }

        if (!isConnected) return false;
        Corridor c = getCorridor(fromId, toId);
        return c != null && !c.isLocked();
    }

    public void setCorridorLocked(String idA, String idB, boolean locked) {
        Corridor c = getCorridor(idA, idB);
        if (c != null) c.setLocked(locked);
    }

    // --- Getters e Iteradores ---

    public Iterator<Room> getRoomsIterator() { return storedRooms.iterator(); }
    public Iterator<Corridor> getCorridorIterator() { return storedCorridors.iterator(); }

    public Lever getLever(String roomId) { return leverMap.get(roomId); }
    public Room getRoomAt(int x, int y) { return roomMap.get(x + "-" + y); }
    public Room getRoom(String id) { return roomMap.get(id); }
    public Corridor getCorridor(String idA, String idB) { return corridorMap.get(getCanonicalCorridorKey(idA, idB)); }

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