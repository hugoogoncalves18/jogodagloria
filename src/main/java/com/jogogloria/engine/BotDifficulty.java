package com.jogogloria.engine;

/**
 * Enumeração de dificuldades dos Bots
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public enum BotDifficulty {
    EASY("Fácil"), //Bot falha muito
    MEDIUM("Médio"), //Bot falha ás vezes
    HARD("Dificil"); //Bot joga perfeitamente

    private final String label;

    BotDifficulty(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
