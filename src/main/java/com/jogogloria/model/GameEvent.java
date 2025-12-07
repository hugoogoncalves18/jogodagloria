package com.jogogloria.model;

public class GameEvent implements Comparable<GameEvent> {
    private final int turn;
    private final String type;
    private final String description;
    private final String timestamp;

    public GameEvent(int turn, String type, String description) {
        this.turn = turn;
        this.type = type;
        this.description = description;
        this.timestamp = java.time.LocalTime.now().toString();
    }

    public String toJson() {
        String doc = description.replace("\"", "\\\"");
        return String.format("{\"turn\": %d, \"type\": \"%s\", \"description\": \"%s\", \\\"time\\\": \\\"%s\\\"}", turn, type, description, timestamp);
    }

    @Override
    public  String toString() {
        return description;
    }

    @Override
    public int compareTo(GameEvent o) {
        return 0;
    }
}
