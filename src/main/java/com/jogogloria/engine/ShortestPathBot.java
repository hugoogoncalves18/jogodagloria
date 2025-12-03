package com.jogogloria.engine;

import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Player;
import java.util.Iterator;

public class ShortestPathBot implements BotStrategy {

    @Override
    public String nextMove(Labyrinth labyrinth, Player player, int rollValue) {
        String currentRoomId = player.getCurrentRoomId();
        String treasureRoomId = labyrinth.getTreasureRoom(); // Certifica-te que tens este método no Labyrinth

        if (currentRoomId.equals(treasureRoomId)) {
            return null; // Já ganhou
        }

        // 1. Obter o iterador do caminho mais curto do Grafo
        // Este método usa o BFS interno do AdjListGraph
        Iterator<String> pathIterator = labyrinth.getShortestPath(currentRoomId, treasureRoomId);

        if (pathIterator == null || !pathIterator.hasNext()) {
            return null; // Não há caminho possível
        }

        // 2. O primeiro elemento do iterador é a sala atual (onde o bot está)
        pathIterator.next();

        // 3. O próximo elemento é a sala para onde ele deve ir
        if (pathIterator.hasNext()) {
            String nextStepId = pathIterator.next();
            return nextStepId;
        }

        return null; // Caminho vazio ou erro
    }
}