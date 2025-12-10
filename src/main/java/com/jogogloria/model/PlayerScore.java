package com.jogogloria.model;

/**
 * Representa a pontuação de um jogador para efeitos de classificação
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class PlayerScore implements Comparable<PlayerScore> {

    /** O jogador associado a esta pontuação. */
    private final Player player;

    /**
     * Cria um novo registo de pontuaçã
     * @param player O jogador a registar
     */
    public PlayerScore(Player player) {

        this.player = player;
    }

    /**
     * Obtém o objeto jogador
     * @return o jogador
     */
    public Player getPlayer() {

        return player;
    }

    /**
     * Retorna uma representação textual da pontuação
     * @return String formatada com o nome e nº de vitórias
     */
    @Override
    public String toString() {

        return player.getName() + ": " + player.getWins() + " vitórias";
    }

    /**
     * Compara duas pontuações para ordenação.
     * @param other A outra pontuação a comparar.
     * @return Um valor negativo se este jogador tiver mais vitórias, positivo se tiver menos, ou 0 se tiverem as mesmas.
     *
     */
    @Override
    public int compareTo(PlayerScore other) {

        return other.player.getWins() - this.player.getWins();
    }
}
