package com.jogogloria.model;

/**
 * Representa uma penalidade que pode ser aplicada a um jogador
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class Penalty implements Comparable<Penalty> {

    /**
     * Enumeração que define tipos de comportamentos das penalidades
     */
    public enum PenaltyType {
        RETREAT, //Recua o próprio jogador
        PLAYERS_BENEFITS, //Benificia o/s jogador/es que estiverem a jogar contra
        SKIP_TURN //perde a vez
    }

    /** Descrição textual do castigo*/
    private String description;

    /** O tipo de lógica a aplicar*/
    private PenaltyType type;

    /** O valor numérico da intensidade da penalidade*/
    private int value;

    /**
     * Cria nova penalidade
     * @param description Descrição para mostrar ao jogador
     * @param type O tipo de comportamento
     * @param value A intensidade do castigo
     */
    public Penalty(String description, PenaltyType type, int value) {
        this.description = description;
        this.type = type;
        this.value = value;
    }

    /**
     * Obtém a descrição da penalidade
     * @return A descrição textual
     */
    public String getDescription() {
        return description;
    }

    /**
     * Obtém o tipo de penalidade
     * @return O valor de Enum
     */
    public PenaltyType getType() {
        return type;
    }

    /**
     * Obtém o valor numérico associado á penalidade
     * @return O número de casas ou turnos
     */
    public int getValue() {
        return value;
    }

    /**
     * Retorna a descrição da penalidade
     * @return A descrição textual
     */
    @Override
    public String toString() {
        return description;
    }


    /**
     * Comparação de penalidades
     * @param o A outra penalidade a comparar
     * @return 0
     */
    @Override
    public int compareTo(Penalty o) {
        return 0;
    }
}


