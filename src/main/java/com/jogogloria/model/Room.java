package com.jogogloria.model;

public class Room implements Comparable<Room> {

    public enum RoomType {
        START, NORMAL, RIDDLE, LEVER, PENALTY, BOOST, EXIT
    }

    private final String id;
    private final int x; // Coordenada Visual X
    private final int y; // Coordenada Visual Y
    private final RoomType type;
    private final String label;
    private String extraData;

    public Room(String id, RoomType type, String label) {
        this.id = id;
        this.type = type;
        this.label = label;
        this.extraData = null;

        // Tenta extrair coordenadas do ID (formato "x-y") para facilitar a GUI
        int tempX = 0, tempY = 0;
        try {
            String[] parts = id.split("-");
            if (parts.length == 2) {
                tempX = Integer.parseInt(parts[0]);
                tempY = Integer.parseInt(parts[1]);
            }
        } catch (NumberFormatException e) {
            // Se o ID não for "x-y", fica 0,0 (fallback)
        }
        this.x = tempX;
        this.y = tempY;
    }

    public String getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public RoomType getType() { return type; }
    public String getLabel() { return label; }
    public String getExtraData() { return extraData; }
    public void setExtraData(String extraData) { this.extraData = extraData; }

    public boolean isSpecial() {
        return type == RoomType.RIDDLE || type == RoomType.BOOST ||
                type == RoomType.PENALTY || type == RoomType.LEVER;
    }

    @Override
    public String toString() {
        return "Room [" + id + "] (" + x + "," + y + ") Type: " + type;
    }

    @Override
    public int compareTo(Room o) {
        return 0;
    }
}