package com.jogogloria.engine;

import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Lever;
import com.jogogloria.model.Player;
import com.example.Biblioteca.iterators.Iterator;
import com.example.Biblioteca.lists.ArrayUnorderedList;

public class ShortestPathBot implements BotStrategy {

    @Override
    public String nextMove(Labyrinth labyrinth, Player player, int rollValue) {
        String currentRoomId = player.getCurrentRoomId();
        String treasureRoomId = labyrinth.getTreasureRoom();

        if (currentRoomId.equals(treasureRoomId)) {
            return null; // Já ganhou
        }

        // --- PLANO A: Tentar ir direto ao Tesouro ---
        // A DIFERENÇA ESTÁ AQUI: Usamos isPathClear para ver o caminho TODO
        if (isPathClear(labyrinth, currentRoomId, treasureRoomId)) {
            String nextStep = getNextStep(labyrinth, currentRoomId, treasureRoomId);
            if (nextStep != null) return nextStep;
        }

        // --- PLANO B: Caminho bloqueado? Vamos às Alavancas! ---
        System.out.println("Bot " + player.getName() + ": Caminho bloqueado. A procurar alavancas...");

        String nextStepToLever = getMoveToClosestAccessibleLever(labyrinth, currentRoomId);

        if (nextStepToLever != null) {
            return nextStepToLever;
        }

        // --- PLANO C: Fallback (Movimento de segurança) ---
        // Se não houver nada para fazer, move-se aleatoriamente para não bloquear o jogo
        return getAnyValidNeighbor(labyrinth, currentRoomId);
    }

    /**
     * [NOVO] Verifica se TODO o caminho até ao destino está destrancado.
     * Isto impede o loop de "ir e voltar" quando a porta está longe.
     */
    private boolean isPathClear(Labyrinth lab, String start, String target) {
        Iterator<String> path = lab.getShortestPath(start, target);

        if (path == null || !path.hasNext()) return false;

        String current = path.next(); // Começa na sala atual

        while(path.hasNext()) {
            String next = path.next();
            // Se houver ALGUMA porta trancada no meio do trajeto, o caminho não serve!
            if (!lab.isValidMove(current, next)) {
                return false;
            }
            current = next;
        }
        return true;
    }

    /**
     * Helper para obter apenas o próximo passo imediato.
     */
    private String getNextStep(Labyrinth lab, String start, String target) {
        Iterator<String> path = lab.getShortestPath(start, target);
        if (path != null && path.hasNext()) {
            path.next(); // Ignora atual
            if (path.hasNext()) return path.next();
        }
        return null;
    }

    /**
     * Procura a alavanca mais próxima que:
     * 1. Ainda não foi ativada.
     * 2. Tem o caminho até lá DESIMPEDIDO (isPathClear).
     */
    private String getMoveToClosestAccessibleLever(Labyrinth labyrinth, String currentRoomId) {
        Iterator<com.jogogloria.model.Room> roomIt = labyrinth.getRoomsIterator();

        String bestNextStep = null;
        int shortestDistance = Integer.MAX_VALUE;

        while (roomIt.hasNext()) {
            com.jogogloria.model.Room r = roomIt.next();

            // Verifica se a sala tem alavanca
            Lever l = labyrinth.getLever(r.getId());

            // Só interessa se a alavanca existe e NÃO foi puxada
            if (l != null && !l.isActivated()) {

                // Só vale a pena ir se conseguirmos CHEGAR lá!
                if (isPathClear(labyrinth, currentRoomId, r.getId())) {

                    int dist = getDistance(labyrinth, currentRoomId, r.getId());

                    // Encontra a mais próxima
                    if (dist != -1 && dist < shortestDistance) {
                        shortestDistance = dist;
                        bestNextStep = getNextStep(labyrinth, currentRoomId, r.getId());
                    }
                }
            }
        }
        return bestNextStep;
    }

    private int getDistance(Labyrinth lab, String start, String end) {
        Iterator<String> path = lab.getShortestPath(start, end);
        int count = 0;
        if (path == null || !path.hasNext()) return -1;
        while(path.hasNext()) {
            path.next();
            count++;
        }
        return count;
    }

    private String getAnyValidNeighbor(Labyrinth labyrinth, String currentRoomId) {
        try {
            ArrayUnorderedList<String> neighbors = labyrinth.getNeighbors(currentRoomId);
            Iterator<String> it = neighbors.iterator();
            while (it.hasNext()) {
                String neighborId = it.next();
                if (labyrinth.isValidMove(currentRoomId, neighborId)) {
                    return neighborId;
                }
            }
        } catch (Exception e) {}
        return null;
    }
}