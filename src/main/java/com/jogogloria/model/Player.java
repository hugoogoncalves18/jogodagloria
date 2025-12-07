package com.jogogloria.model;

import com.jogogloria.engine.BotStrategy;
import com.jogogloria.engine.GameEngine;
import com.example.Biblioteca.lists.ArrayUnorderedList;

public class Player implements Comparable<Player> {
    private final String id;
    private final String name;
    private int wins = 0;
    private String initialPosition;
    private final boolean isBot;
    private final BotStrategy botStrategy;
    private String currentRoomId;
    private int skipTurns = 0;
    private int movementPoints = 0;
    private int boost = 0;
    private ArrayUnorderedList<GameEvent> logs;

    // Construtor para Humano
    public Player(String id, String name) {
        this.id = id;
        this.name = name;
        this.isBot = false;
        this.botStrategy = null;
        this.logs = new ArrayUnorderedList<>();
    }

    // Construtor para Bot
    public Player(String id, String name, BotStrategy strategy) {
        this.id = id;
        this.name = name;
        this.isBot = true;
        this.botStrategy = strategy;
        this.logs = new ArrayUnorderedList<>();
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

    public String getInitialPosition() { return initialPosition;}
    public void setInitialPosition(String id) {this.initialPosition = id; }

    public int getBoost() {
        return boost;
    }

    public void addBoost() {
        this.boost++;
    }

    public void decrementBoost() {
        if (boost > 0)
            boost--;
    }

    public int getWins() {
        return wins;
    }

    public void incrementWins() {
        this.wins++;
    }

    //logs
    public void logEvent(int turn, String type, String description) {
        logs.addToRear(new GameEvent(turn, type, description));
    }

    public ArrayUnorderedList<GameEvent> getLogs() {
        return logs;
    }

    public void resetForNewMatch() {
        this.movementPoints = 0;
        this.skipTurns = 0;
        this.boost = 0;
        if (initialPosition != null) {
            this.currentRoomId = initialPosition;
        } else {
            this.currentRoomId = null;
        }
        this.logs = new ArrayUnorderedList<>();
    }

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