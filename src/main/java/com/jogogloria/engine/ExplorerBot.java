package com.jogogloria.engine;

import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Player;
import com.jogogloria.model.Room;
import com.example.Biblioteca.iterators.Iterator;

/**
 * Classe da inteligência do Bot explorador
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class ExplorerBot implements BotStrategy {

    @Override
    public String nextMove(Labyrinth labyrinth, Player player, int rollValue) {
        String currentId = player.getCurrentRoom().getId();

        // 1. Procurar o Boost mais próximo
        String nearestBoostId = findNearestBoost(labyrinth, currentId);

        if (nearestBoostId != null) {
            System.out.println("ExplorerBot: Vi um Boost em " + nearestBoostId + "! Vou buscá-lo.");
            // Usa o caminho mais curto para chegar ao item
            return getNextStep(labyrinth, currentId, nearestBoostId);
        }

        // 2. Se não houver boosts, vai para o tesouro
        System.out.println("ExplorerBot: Sem boosts. A ir para o tesouro.");
        return getNextStep(labyrinth, currentId, labyrinth.getTreasureRoom());
    }

    /**
     * Classe que utiliza o algoritmo BFS para encontrar a sala de Boost mais próxima
     * @param labyrinth
     * @param startId
     * @return
     */
    private String findNearestBoost(Labyrinth labyrinth, String startId) {
        try {
            Iterator<String> it = labyrinth.iteratorBFS(startId);
            while (it.hasNext()) {
                String roomId = it.next();
                Room r = labyrinth.getRoom(roomId);

                // Se for Boost e não for a sala onde já estou
                if (r != null && r.getType() == Room.RoomType.LEVER && !roomId.equals(startId)) {
                    // Verifica se consigo lá chegar
                    if (isPathClear(labyrinth, startId, roomId)) {
                        return roomId;
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Verifica se o caminho completo entre duas salas está livre de portas trancadas.
     */
    private boolean isPathClear(Labyrinth lab, String start, String target) {
        Iterator<String> path = lab.getShortestPath(start, target);

        if (path == null || !path.hasNext()) return false;

        String current = path.next();

        while(path.hasNext()) {
            String next = path.next();
            if (!lab.isValidMove(current, next)) {
                return false;
            }
            current = next;
        }
        return true;
    }

    /**
     * Calcula o primeiro passo do caminho mais curto para um destino.
     */
    private String getNextStep(Labyrinth lab, String start, String target) {
        Iterator<String> path = lab.getShortestPath(start, target);
        if (path != null && path.hasNext()) {
            path.next(); // Ignora atual
            if (path.hasNext()) return path.next();
        }
        return null;
    }
}
