package com.jogogloria.engine;

import com.example.Biblioteca.exceptions.EmptyCollectionException;
import com.example.Biblioteca.queues.LinkedQueue;
import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Player;
import com.jogogloria.model.Room;
import com.jogogloria.model.Corridor;
import com.jogogloria.model.Penalty;

import java.util.Iterator;

public class GameEngine {

    private final Labyrinth labyrinth;
    private final LinkedQueue<Player> turnQueue;
    private boolean gameRunning;
    private int playerSpawnIndex = 0;
    private final PenaltyManager penaltyManager;
    private final ArrayUnorderedList<Player> allPlayers;

    public GameEngine(Labyrinth labyrinth) {
        this.labyrinth = labyrinth;
        this.turnQueue = new LinkedQueue<>();
        this.gameRunning = true;
        this.penaltyManager = new PenaltyManager();
        this.allPlayers = new ArrayUnorderedList<>();
    }

    // --- Gestão de Jogadores ---

    public void addPlayer(Player player) throws EmptyCollectionException {
        // Define a posição inicial se ainda não tiver
        if (player.getCurrentRoomId() == null) {
            // Pega o primeiro ponto de entrada disponível (pode ser melhorado)
            distributePlayerSpawn(player);
        }
        turnQueue.enqueue(player);
        allPlayers.addToRear(player);
    }

    private void distributePlayerSpawn(Player player) {
        ArrayUnorderedList<String> entries = labyrinth.getEntryPoints();

        if (entries.isEmpty()) return;

        int totalEntries = entries.size();
        int targetIndex = playerSpawnIndex % totalEntries;
        String spawmId = entries.get(targetIndex);

        if(spawmId != null) {
            player.move(spawmId);
            player.setInitialPosition(spawmId);
            System.out.println("Spawn: " + player.getName() + "sala: " + spawmId);
        }

        playerSpawnIndex++;
    }

    // --- Ciclo de Jogo (Turnos) ---

    public Player getCurrentPlayer() {
        if (turnQueue.isEmpty()) return null;
        try {
            return turnQueue.first();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Passa o turno. Roda a fila e processa penalidades de turno (skip)
     * até encontrar um jogador que possa jogar.
     */
    public void nextTurn() {
        if (!gameRunning || turnQueue.isEmpty()) return;

        // 1. O jogador que acabou de jogar (está na frente) vai para o fim da fila
        try {
            Player finishedPlayer = turnQueue.dequeue();
            turnQueue.enqueue(finishedPlayer);
        } catch (EmptyCollectionException e) {
            System.err.println("Erro crítico: Fila ficou vazia ao rodar turno.");
            return;
        }

        // 2. Agora procuramos o próximo jogador válido (que não perdeu a vez)
        // Usamos um ciclo para saltar automaticamente quem tem penalidades
        while (true) {
            try {
                if (turnQueue.isEmpty()) break; // Segurança

                // Espreitar quem é o próximo (está na frente da fila)
                Player nextCandidate = turnQueue.first();

                if (nextCandidate.getSkipTurns() > 0) {
                    // Tem penalidade: decrementa e salta a vez dele
                    nextCandidate.decrementSkipTurn();
                    System.out.println(nextCandidate.getName() + " perdeu a vez! (Restam: " + nextCandidate.getSkipTurns() + ")");

                    // Roda este jogador para o fim da fila imediatamente
                    turnQueue.enqueue(turnQueue.dequeue());

                    // O loop continua para testar o próximo candidato
                } else {
                    // Encontrámos um jogador válido! É a vez dele.
                    // Paramos o loop e deixamo-lo na frente da fila para jogar.
                    break;
                }
            } catch (EmptyCollectionException e) {
                break;
            }
        }
    }
    // --- Lógica de Movimento ---

    /**
     * Tenta mover o jogador atual para uma sala vizinha.
     * @param targetRoomId ID da sala de destino
     * @return true se o movimento foi válido e realizado
     */
    public boolean tryMove(Player player, String targetRoomId) throws EmptyCollectionException {
        if (!gameRunning) return false;

        String currentId = player.getCurrentRoomId();

        // 1. Validar Vizinhança
        boolean isNeighbor = false;
        ArrayUnorderedList<String> neighbors = labyrinth.getNeighbors(currentId);
        Iterator<String> it = neighbors.iterator();
        while (it.hasNext()) {
            if (it.next().equals(targetRoomId)) {
                isNeighbor = true;
                break;
            }
        }

        if (!isNeighbor) {
            System.out.println("Movimento inválido: Salas não são adjacentes.");
            return false;
        }

        // 2. Validar Corredor
        Corridor corridor = labyrinth.getCorridor(currentId, targetRoomId);
        if (corridor != null && corridor.isLocked()) {
            System.out.println("O corredor está trancado!");
            return false;
        }

        // 3. Executar Movimento
        player.move(targetRoomId);
        player.decrementMovementPoints();

        if (player.getMovementPoints() == 0 || targetRoomId.equals(labyrinth.getTreasureRoom())) {
            checkRoomEffects(player, targetRoomId);
        }

        return true;
    }

    private void checkRoomEffects(Player player, String roomId) throws EmptyCollectionException {
        Room room = labyrinth.getRoom(roomId);
        if (room == null) return;

        // Verifica Vitória
        if (roomId.equals(labyrinth.getTreasureRoom())) {
            gameRunning = false;
            System.out.println("JOGO ACABOU! Vencedor: " + player.getName());
            // Aqui podes disparar um evento para a UI
        }

        if (room.getType() == Room.RoomType.PENALTY) {
            handlePenaltyEvent(player);
        }

        // Outros tipos (Penalidade, Boost) seriam tratados aqui
        switch (room.getType()) {
            case PENALTY:
                player.setSkipTurns(1);
                System.out.println("Penalidade! Perdes o próximo turno.");
                break;
            // Adicionar outros cases conforme necessário
        }
    }

    // --- Lógica de Bots ---

    /**
     * Executa a jogada do Bot automaticamente.
     * Deve ser chamado pela UI quando getCurrentPlayer().isBot() for true.
     */
    public void executeBotTurn() throws EmptyCollectionException {
        Player bot = getCurrentPlayer();

        // Validações de segurança
        if (bot == null || !bot.isBot()) return;
        if (!gameRunning) return;

        // Se o bot não tiver estratégia ou movimentos, passa a vez
        if (bot.getBotStrategy() == null || bot.getMovementPoints() <= 0) {
            // O loop de jogo principal tratará de passar o turno se os pontos acabarem
            return;
        }

        // Pede à estratégia o próximo passo
        String targetRoomId = bot.getBotStrategy().nextMove(
                labyrinth,
                bot,
                bot.getMovementPoints()
        );

        // Se a estratégia devolveu um destino válido, tenta mover
        if (targetRoomId != null) {
            boolean moved = tryMove(bot, targetRoomId);

            if (moved) {
                System.out.println("Bot " + bot.getName() + " moveu-se para " + targetRoomId);
                // Se ainda tiver pontos, pode tentar mover-se novamente no próximo ciclo do loop de jogo
                // ou podes chamar recursivamente se quiseres movimento instantâneo:
                // if (bot.getMovementPoints() > 0) executeBotTurn();
            }
        } else {
            // Estratégia não encontrou caminho (ex: bloqueado), perde os pontos restantes
            bot.setMovementPoints(0);
        }
    }

    public boolean isGameRunning() { return gameRunning; }

    private void graphMove(Player p, int steps) {
        if (steps == 0) {
            return;
        }
        if (steps == -99) {
            String start = p.getInitialPosition();
            if (start == null) {
                start = labyrinth.getStartRoomId();
            }
            p.move(start);
            System.out.println(p.getName() + " recuou até ao inicio");
            return;
        }

        String targetId = (steps > 0) ? labyrinth.getTreasureRoom() : labyrinth.getStartRoomId();
        int moves = Math.abs(steps);

        java.util.Iterator<String> path = labyrinth.getShortestPath(p.getCurrentRoomId(), targetId);
        if (path == null || !path.hasNext()) return;
        path.next();

        String nextRoom = null;
        while (moves > 0 && path.hasNext()) {
            nextRoom = path.next();
            moves--;
        }
        if (nextRoom != null) {
            p.move(nextRoom);
            System.out.println("O jogador " + p.getName() + "moveu-se para a casa " + nextRoom);
        }
    }

    private void handlePenaltyEvent(Player victim) throws EmptyCollectionException {
        Penalty p = penaltyManager.getNextPenalty();
        if (p == null) {
            return;
        }
        System.out.println("Penalidade " + p.getDescription());

        switch (p.getType()) {
            case RETREAT:
                graphMove(victim, p.getValue());
                break;
            case SKIP_TURN:
                victim.setSkipTurns(p.getValue());
                break;
            case PLAYERS_BENEFITS:
                for(Player other : allPlayers) {
                    if (!other.getId().equals(victim.getId())) {
                        graphMove(other, p.getValue());
                    }
                }
                break;
        }
    }
}