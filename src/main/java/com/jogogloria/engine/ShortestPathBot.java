package com.jogogloria.engine;

import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Lever;
import com.jogogloria.model.Player;
import com.jogogloria.model.Room;
import com.example.Biblioteca.iterators.Iterator;
import com.example.Biblioteca.lists.ArrayUnorderedList;

/**
 * Implementação de uma estratégia de bot inteligente.
 *
 * @author Hugo Gonçalves
 * @version 2.0
 */
public class ShortestPathBot implements BotStrategy {

    /**
     * Calcula o próximo movimento do bot com base no estado do jogo.
     *
     * @param labyrinth O mapa do jogo.
     * @param player    O bot que está a mover.
     * @param rollValue O valor do dado.
     * @return O ID da próxima sala para onde o bot deve ir.
     */
    @Override
    public String nextMove(Labyrinth labyrinth, Player player, int rollValue) {
        String currentRoomId = player.getCurrentRoom().getId();
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
     * Procura a alavanca útil mais próxima usando o BFS.
     *
     * @param labyrinth     O labirinto.
     * @param currentRoomId Posição atual do bot.
     * @return Id da próxima sala em direção à alavanca.
     */
    private String getMoveToLeverBFS(Labyrinth labyrinth, String currentRoomId) {
        try {
            // 1. Obtém o iterador BFS a partir da posição atual
            // O grafo continua a trabalhar com Strings (IDs), por isso iteramos IDs.
            Iterator<String> bfsIterator = labyrinth.iteratorBFS(currentRoomId);

            // 2. Percorre as salas por ordem de proximidade
            while (bfsIterator.hasNext()) {
                String roomId = bfsIterator.next();

                // Ignora a própria sala onde estamos
                if (roomId.equals(currentRoomId)) continue;

                // 3. Obtém o objeto Sala real
                Room r = labyrinth.getRoom(roomId);

                // 4. Verifica se esta sala tem alavanca (Referência Direta)
                if (r != null && r.hasLever()) {
                    Lever l = r.getLever();

                    if (!l.isActivated()) {
                        // Encontrámos a alavanca mais próxima!
                        // Confirma se o caminho até lá está livre
                        if (isPathClear(labyrinth, currentRoomId, roomId)) {
                            System.out.println("-> Alvo encontrado: Alavanca em " + roomId);
                            return getNextStep(labyrinth, currentRoomId, roomId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erro no BFS: " + e.getMessage());
        }

        return null; // Nenhuma alavanca acessível encontrada
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

    /**
     * Obtém um vizinho aleatório válido.
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