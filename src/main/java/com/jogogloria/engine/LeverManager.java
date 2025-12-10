package com.jogogloria.engine;

import com.jogogloria.model.Corridor;
import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Lever;
import com.jogogloria.model.Player;

/**
 * Gestor da lógica das alavancas
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class LeverManager {

    /**
     * Construtor padrão
     */
    public LeverManager() {
    }

    /**
     * Verifica e ativa a alavanca na sala atual.
     * @param player        O jogador que está na sala (para efeitos de logs/mensagens).
     * @param currentRoomId O ID da sala onde o jogador acabou de aterrar.
     * @param labyrinth     A referência para o labirinto atual (para aceder aos dados das salas e corredores).
     */
    public void checkLever(Player player, String currentRoomId, Labyrinth labyrinth) {
        // 1. Pede a alavanca ao Labirinto (que foi carregada pelo MapLoader)
        Lever lever = labyrinth.getLever(currentRoomId);

        if (lever == null) {
            return; // Não há alavanca aqui
        }

        // 2. Verifica se já foi usada
        if (lever.isActivated()) {
            System.out.println("Esta alavanca (" + lever + ") já foi puxada.");
            return;
        }

        // 3. Tenta encontrar o corredor no labirinto
        Corridor targetCorridor = labyrinth.getCorridor(lever.getDoorA(), lever.getDoorB());

        if (targetCorridor != null) {
            // 4. Destranca
            targetCorridor.setLocked(false);
            lever.setActivated(true);

            System.out.println("CLACK! " + player.getName() + " ativou a alavanca!");
            System.out.println("Passagem aberta entre " + lever.getDoorA() + " e " + lever.getDoorB());
        } else {
            System.out.println("Erro: A alavanca aponta para um corredor inexistente.");
        }
    }
}