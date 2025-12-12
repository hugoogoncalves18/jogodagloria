package com.jogogloria.engine;

import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Player;
import com.jogogloria.model.Room;

import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.iterators.Iterator;

/**
 * Classe que gere a inteligência do Bot Covarde, bot que evita caminhos que passe por penalidades
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class CowardBot implements BotStrategy{

    @Override
    public String nextMove(Labyrinth labyrinth, Player player, int rollValue) {
        String current = player.getCurrentRoom().getId();
        String treasure = labyrinth.getTreasureRoom();

        String idealStep = getNextStep(labyrinth, current, treasure);

        if (idealStep == null) return null; // Sem caminho

        // 2. O passo ideal é perigoso?
        if (isDangerous(labyrinth, idealStep)) {
            System.out.println("CowardBot: Que medo! " + idealStep + " tem uma penalidade. Vou contornar.");

            // 3. Tentar encontrar um vizinho seguro
            String safeNeighbor = findSafeNeighbor(labyrinth, current, treasure);
            if (safeNeighbor != null) {
                return safeNeighbor;
            } else {
                System.out.println("CowardBot: Estou encurralado! Tenho de arriscar.");
            }
        }

        return idealStep;
    }

    /**
     * Método verifica se o caminho que o Bot vai utilizar é seguro
     * @param labyrinth
     * @param room
     * @return
     */
    private boolean isDangerous(Labyrinth labyrinth, String room) {
        Room r = labyrinth.getRoom(room);
        return r != null && r.getType() == Room.RoomType.PENALTY;
    }

    /**
     * Método que procura um vizinho que seja seguro para o Bot seguir o caminho
     * @param labyrinth
     * @param current
     * @param target
     * @return
     */
    private String findSafeNeighbor(Labyrinth labyrinth, String current, String target) {
        try {
            ArrayUnorderedList<String> neighbors = labyrinth.getNeighbors(current);
            Iterator<String> it = neighbors.iterator();

            String bestAlternative = null;
            double bestDist = Double.POSITIVE_INFINITY;

            while (it.hasNext()) {
                String neighbor = it.next();


                if (labyrinth.isValidMove(current, neighbor) && !isDangerous(labyrinth, neighbor)) {
                    double dist = labyrinth.graphStructure.shortestPathWeight(neighbor, target);

                    if (dist < bestDist) {
                        bestDist = dist;
                        bestAlternative = neighbor;
                    }
                }
            }
            return bestAlternative;
        } catch (Exception e) { return null; }
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
