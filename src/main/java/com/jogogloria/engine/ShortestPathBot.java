package com.jogogloria.engine;

import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Lever;
import com.jogogloria.model.Player;
import com.example.Biblioteca.iterators.Iterator;
import com.example.Biblioteca.lists.ArrayUnorderedList;

/**
 * Implementação de uma estratégia de bot inteligente
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class ShortestPathBot implements BotStrategy {

    /**
     * Calcula o próximo movimento do bot com base no estado do jogo
     * @param labyrinth O mapa do jogo.
     * @param player O bot que está a mover.
     * @param rollValue O valor do dado (quantos passos pode dar).
     * @return O ID da próxima sala para onde o bot deve ir
     */
    @Override
    public String nextMove(Labyrinth labyrinth, Player player, int rollValue) {
        String currentRoomId = player.getCurrentRoomId();
        String treasureRoomId = labyrinth.getTreasureRoom();

        if (currentRoomId.equals(treasureRoomId)) {
            return null; // Já ganhou
        }

        // --- PLANO A: Tentar ir direto ao Tesouro ---
        if (isPathClear(labyrinth, currentRoomId, treasureRoomId)) {
            String nextStep = getNextStep(labyrinth, currentRoomId, treasureRoomId);
            if (nextStep != null) return nextStep;
        }

        // --- PLANO B: Caminho bloqueado? Usar BFS para achar a alavanca mais perto! ---
        System.out.println("Bot " + player.getName() + ": Caminho bloqueado. A usar BFS para encontrar alavancas...");

        String nextStepToLever = getMoveToLeverBFS(labyrinth, currentRoomId);

        if (nextStepToLever != null) {
            return nextStepToLever;
        }

        // --- PLANO C: Fallback (Movimento de segurança) ---
        return getAnyValidNeighbor(labyrinth, currentRoomId);
    }

    /**
     * Procura a alavanca útil mais próxima usando o BFS
     * @param labyrinth O labirinto
     * @param currentRoomId Posição atual do bot
     * @return Id da próxima sala em direção á alavanca
     */
    private String getMoveToLeverBFS(Labyrinth labyrinth, String currentRoomId) {
        try {
            // 1. Obtém o iterador BFS a partir da posição atual
            Iterator<String> bfsIterator = labyrinth.iteratorBFS(currentRoomId);

            // 2. Percorre as salas por ordem de proximidade
            while (bfsIterator.hasNext()) {
                String roomId = bfsIterator.next();

                // Ignora a própria sala onde estamos
                if (roomId.equals(currentRoomId)) continue;

                // 3. Verifica se esta sala tem uma alavanca útil
                Lever l = labyrinth.getLever(roomId);

                if (l != null && !l.isActivated()) {
                    // Encontrámos a alavanca mais próxima!
                    // Agora só precisamos de confirmar se conseguimos chegar lá (se o caminho está livre)
                    if (isPathClear(labyrinth, currentRoomId, roomId)) {
                        System.out.println("-> Alvo encontrado: Alavanca em " + roomId);
                        return getNextStep(labyrinth, currentRoomId, roomId);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erro no BFS: " + e.getMessage());
        }

        return null; // Nenhuma alavanca acessível encontrada
    }


    /**
     * Verifica se o caminho completo entre duas salas está livre de portas trancadas
     * @param lab Labirinto
     * @param start Origem
     * @param target Destino
     * @return {@code true} se o caminho for percorrível, {@code false} caso contrário
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
     * Calcula o primeiro passo do caminho mais curto para um destino
     * @param lab
     * @param start
     * @param target
     * @return
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
     * Obtém um vizinho aleatório válido
     */
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