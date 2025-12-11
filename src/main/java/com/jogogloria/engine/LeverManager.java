package com.jogogloria.engine;

import com.jogogloria.model.Corridor;
import com.jogogloria.model.Lever;
import com.jogogloria.model.Player;
import com.jogogloria.model.Room;

/**
 * Gestor da lógica das alavancas.
 *
 * @author Hugo Gonçalves
 * @version 2.0
 */
public class LeverManager {

    /**
     * Construtor padrão.
     */
    public LeverManager() {
    }

    /**
     * Verifica e ativa a alavanca na sala atual.
     *
     * @param player      O jogador que interagiu (para logs).
     * @param currentRoom O objeto da Sala onde o jogador está.
     */
    public void checkLever(Player player, Room currentRoom) {
        // 1. Validação inicial
        if (currentRoom == null) return;

        // 2. Verifica se a sala tem alavanca (Referência direta)
        if (!currentRoom.hasLever()) {
            return;
        }

        // 3. Obtém a alavanca diretamente da sala
        Lever lever = currentRoom.getLever();

        // 4. Verifica se já foi usada
        if (lever.isActivated()) {
            System.out.println("Esta alavanca (" + lever + ") já foi puxada.");
            return;
        }

        // 5. Obtém o corredor alvo diretamente da alavanca
        Corridor targetCorridor = lever.getTargetCorridor();

        if (targetCorridor != null) {
            // 6. Destranca e Ativa
            targetCorridor.setLocked(false);
            lever.setActivated(true);

            System.out.println("CLACK! " + player.getName() + " ativou a alavanca!");
            System.out.println("Passagem aberta!");
        } else {
            System.err.println("Erro crítico: A alavanca " + lever + " não tem corredor associado.");
        }
    }
}