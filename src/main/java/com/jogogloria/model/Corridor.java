package com.jogogloria.model;
import com.example.Biblioteca.lists.LinkedList;

public class Corridor implements Comparable<Corridor> {
    private final String id;
    private final String roomAId;
    private final String roomBId;
    private boolean locked;
    private LinkedList<String> events;
    private int weight;

    public Corridor(String id, String roomAId, String roomBId) {
        this.id = id;
        this.roomAId = roomAId;
        this.roomBId = roomBId;
        this.locked = false;
        this.weight = 1;
        this.events = new LinkedList<>();
    }

    public String getId() { return id; }
    public String getRoomAId() { return roomAId; }
    public String getRoomBId() { return roomBId; }
    public boolean isLocked() { return locked; }
    public int getWeight() { return weight; }
    public LinkedList<String> getEvents() { return events; }

    public void setLocked(boolean locked) { this.locked = locked; } // Corrigido camelCase
    public void setWeight(int weight) { this.weight = weight; }

    public String getOtherRoomId(String currentRoomId) {
        if (currentRoomId.equals(roomAId)) return roomBId;
        if (currentRoomId.equals(roomBId)) return roomAId;
        return null;
    }

    @Override
    public int compareTo(Corridor o) {
        return 0;
    }
}