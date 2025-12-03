package com.jogogloria.engine;

import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Player;

/**
 * Interface para definir o comportamento de um Bot (IA) no jogo.
 */
public interface BotStrategy {

    /**
     * Determina o próximo movimento do bot.
     * @param labyrinth O mapa do jogo.
     * @param player O bot que está a mover.
     * @param rollValue O valor do dado (quantos passos pode dar).
     * @return O ID da Sala para onde o bot deve mover.
     */
    String nextMove(Labyrinth labyrinth, Player player, int rollValue);
}