package com.jogogloria.engine;

import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.jogogloria.config.GameConfig;
import com.jogogloria.io.PenaltyLoader;
import com.jogogloria.model.Penalty;

/**
 * Gestor de Penalidades do jogo
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class PenaltyManager {

    // Usamos Lista em vez de Queue para permitir acesso aleatório (.get)
    private ArrayUnorderedList<Penalty> penaltyList;

    /**
     * Inicia o gestor de penalidades
     */
    public PenaltyManager() {
        this.penaltyList = PenaltyLoader.loadPenalties(GameConfig.PENALTIES_FILE);

        // Fallback
        if (this.penaltyList.isEmpty()) {
            this.penaltyList.addToRear(new Penalty("Perde a vez (Fallback)", Penalty.PenaltyType.SKIP_TURN, 1));
        }
    }

    /**
     * Obtém uma penalidade aleatória da lista
     * @return Um objeto {@link Penalty} sorteado, ou {@code null} se a lista estiver vazia
     */
    public Penalty getNextPenalty() {
        if (penaltyList.isEmpty()) return null;

        // 1. Gera um índice aleatório
        int size = penaltyList.size();
        int randomIndex = (int) (Math.random() * size);

        // 2. Retorna a penalidade nessa posição
        // Como não a removemos, ela volta a estar disponível (baralho infinito)
        return penaltyList.get(randomIndex);
    }
}