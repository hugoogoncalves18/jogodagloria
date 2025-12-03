package com.jogogloria.model;

public class Penalty {

    public enum PenaltyType {
        RETREAT, //Recua o próprio jogador
        PLAYERS_BENEFITS, //Benificia o/s jogador/es que estiverem a jogar contra
        SKIP_TURN //perde a vez
    }

    private String description;
    private PenaltyType type;
    private int value;

    public Penalty(String description, PenaltyType type, int value) {
        this.description = description;
        this.type = type;
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public PenaltyType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return description;
    }
}
