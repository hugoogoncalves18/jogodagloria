package com.jogogloria.model;

import com.jogogloria.engine.BotStrategy;
import com.jogogloria.engine.GameEngine;
import com.example.Biblioteca.lists.ArrayUnorderedList;

/**
 * Representa um jogador
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class Player implements Comparable<Player> {

    /** Identificador do jogador*/
    private final String id;

    /** Nome de exibição do jogador. */
    private final String name;

    /** Contagem de vitórias acumuladas em múltiplas partidas. */
    private int wins = 0;

    /** ID da sala onde o jogador começou*/
    private Room initialRoom;

    /** Flag que indica se é controlado pelo computador. */
    private final boolean isBot;

    /** A estratégia de IA do bot */
    private final BotStrategy botStrategy;

    /** ID da sala onde o jogador se encontra atualmente. */
    private Room currentRoom;

    /** Número de turnos que o jogador tem de esperar. */
    private int skipTurns = 0;

    /** Pontos de movimento restantes no turno atual. */
    private int movementPoints = 0;

    /** Quantidade de itens "Boost" acumulados. */
    private int boost = 0;

    /** Histórico de ações do jogador na partida atual. */
    private ArrayUnorderedList<GameEvent> logs;

    /**
     * Construtor para jogador Humano
     * @param id Identificador único
     * @param name Nome do jogador
     */
    public Player(String id, String name) {
        this.id = id;
        this.name = name;
        this.isBot = false;
        this.botStrategy = null;
        this.logs = new ArrayUnorderedList<>();
    }

    /**
     * Construtor para Bot
     * @param id Identificador
     * @param name Nome do Bot
     * @param strategy A estratégia a utilizar
     */
    public Player(String id, String name, BotStrategy strategy) {
        this.id = id;
        this.name = name;
        this.isBot = true;
        this.botStrategy = strategy;
        this.logs = new ArrayUnorderedList<>();
    }

    // --- Lógica de Movimento ---

    /**
     * Atualiza a posição do jogador
     * @param room O ID da sala destino
     */
    public void move(Room room) {
        this.currentRoom = room;
    }

    // --- Getters e Setters ---
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * Verifica se é Bot
     * @return true se sim, false se naõ
     */
    public boolean isBot() {
        return isBot;
    }

    /**
     * Obtém o ID da sala atual
     * @return Id da sala
     */
    public Room getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Obtém a estratégia de IA
     * @return A estratégia
     */
    public BotStrategy getBotStrategy() {
        return botStrategy;
    }

    public int getSkipTurns() {
        return skipTurns;
    }

    /** Define diretamente quantos turnos o jogador deve saltar*/
    public void setSkipTurns(int n) {
        skipTurns = n;
    }

    /** Adiciona turnos à penalidade existente . */
    public void addSkipTurns(int n) {
        skipTurns += n;
    }

    /** Reduz a penalidade de turnos. */
    public void decrementSkipTurn() {
        if (skipTurns > 0)
            skipTurns--;
    }

    /** Gestão de movimento*/
    public int getMovementPoints() {
        return movementPoints;
    }
    public void setMovementPoints(int n) {
        movementPoints = n;
    }

    /** Consome 1 ponto de movimento. */
    public void decrementMovementPoints() {
        if (movementPoints > 0)
            movementPoints--;
    }

    public Room getInitialRoom() {
        return initialRoom;
    }

    public void setInitialPosition(Room room) {
        this.initialRoom = room;
    }

    public int getBoost() {
        return boost;
    }

    /** Adiciona um boost ao inventário. */
    public void addBoost() {
        this.boost++;
    }

    /** Consome um boost do inventário. */
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

    /**
     * Regista um evento no histórico
     * @param turn Turno atual
     * @param type Tipo de evento
     * @param description Detalhes do evento
     */
    public void logEvent(int turn, String type, String description) {
        logs.addToRear(new GameEvent(turn, type, description));
    }

    /**
     * Obtém a lista completa de eventos da partida
     * @return lista de {@link GameEvent}
     */
    public ArrayUnorderedList<GameEvent> getLogs() {
        return logs;
    }

    /**
     * Prepara o jogador para uma nova partida
     */
    public void resetForNewMatch() {
        this.movementPoints = 0;
        this.skipTurns = 0;
        this.boost = 0;
        if (initialRoom != null) {
            this.currentRoom = initialRoom;
        } else {
            this.currentRoom = null;
        }
        this.logs = new ArrayUnorderedList<>();
    }

    /**
     * Comparação de jogadores baseado em ID
     * @param other Outro jogador
     * @return Resultado da comparação
     */
    @Override
    public int compareTo(Player other) {
        // Ordenação por ID (útil para consistência de turnos)
        return this.id.compareTo(other.id);
    }

    @Override
    public String toString() {
        return name + " (" + currentRoom + ")";
    }
}