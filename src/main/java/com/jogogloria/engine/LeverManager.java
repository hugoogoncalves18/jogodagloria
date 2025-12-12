package com.jogogloria.engine;

import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Lever;
import com.jogogloria.model.Player;
import com.jogogloria.model.Room;

public class LeverManager {

    public LeverManager() {}

    public void checkLever(Player player, Room currentRoom, Labyrinth labyrinth) {
        // Validações básicas
        if (currentRoom == null || !currentRoom.hasLever()) return;

        Lever lever = currentRoom.getLever();

        if (lever.isActivated()) {
            System.out.println(">> Esta alavanca (" + lever + ") já estava ativada.");
            return;
        }

        Room rA = lever.getRoomA();
        Room rB = lever.getRoomB();

        if (rA == null || rB == null) {
            System.err.println("ERRO CRÍTICO: A alavanca " + lever + " aponta para salas nulas!");
            return;
        }

        String idA = rA.getId();
        String idB = rB.getId();

        System.out.println("ACTION: A tentar destrancar porta entre [" + idA + "] e [" + idB + "]...");

        boolean wasLocked = labyrinth.isLocked(idA, idB);


        labyrinth.setConnectionLocked(idA, idB, false);
        lever.setActivated(true);

        if (wasLocked) {
            System.out.println("SUCESSO: CLACK! A porta destrancou.");
        } else {
            System.out.println("AVISO: A alavanca foi puxada, mas a porta já estava aberta (ou não existia tranca).");
            System.out.println("Isto pode indicar um erro no JSON (IDs das portas incorretos).");
        }
    }
}