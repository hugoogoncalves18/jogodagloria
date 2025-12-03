package com.jogogloria.model;

import com.jogogloria.engine.BotStrategy;

public class Player implements Comparable<Player> {
    private final String id;
    private final String name;
    private final boolean isBot;
    private final BotStrategy botStrategy;

    private String currentRoomId;
    private int skipTurns = 0;
    private int movementPoints = 0;

    // Construtor para Humano
    public Player(String id, String name) {
        this.id = id;
        this.name = name;
        this.isBot = false;
        this.botStrategy = null;
    }

    // Construtor para Bot
    public Player(String id, String name, BotStrategy strategy) {
        this.id = id;
        this.name = name;
        this.isBot = true;
        this.botStrategy = strategy;
    }

    // --- Lógica de Movimento ---
    public void move(String roomId) {
        this.currentRoomId = roomId;
    }

    // --- Getters e Setters ---
    public String getId() { return id; }
    public String getName() { return name; }
    public boolean isBot() { return isBot; }

    public String getCurrentRoomId() { return currentRoomId; } // Renomeado para clareza

    public BotStrategy getBotStrategy() { return botStrategy; }

    public int getSkipTurns() { return skipTurns; }
    public void setSkipTurns(int n) { skipTurns = n; } // Simplificado
    public void addSkipTurns(int n) { skipTurns += n; }
    public void decrementSkipTurn() { if (skipTurns > 0) skipTurns--; }

    public int getMovementPoints() { return movementPoints; }
    public void setMovementPoints(int n) { movementPoints = n; }
    public void decrementMovementPoints() { if (movementPoints > 0) movementPoints--; }

    @Override
    public int compareTo(Player other) {
        // Ordenação por ID (útil para consistência de turnos)
        return this.id.compareTo(other.id);
    }

    @Override
    public String toString() {
        return name + " (" + currentRoomId + ")";
    }
}