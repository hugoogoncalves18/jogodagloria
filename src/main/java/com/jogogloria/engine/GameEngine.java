package com.jogogloria.engine;

import com.example.Biblioteca.exceptions.EmptyCollectionException;
import com.example.Biblioteca.queues.LinkedQueue;
import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Player;
import com.jogogloria.model.Room;
import com.jogogloria.model.Corridor;
import com.jogogloria.model.Penalty;
import com.jogogloria.model.Room.RoomType; // Importante para o switch

import java.util.Iterator;

public class GameEngine {

    private final Labyrinth labyrinth;
    private final LinkedQueue<Player> turnQueue;
    private boolean gameRunning;
    private int playerSpawnIndex = 0;
    private final PenaltyManager penaltyManager;
    private final ArrayUnorderedList<Player> allPlayers;
    private final LeverManager leverManager;

    public GameEngine(Labyrinth labyrinth) {
        this.labyrinth = labyrinth;
        this.turnQueue = new LinkedQueue<>();
        this.gameRunning = true;
        this.penaltyManager = new PenaltyManager();
        this.allPlayers = new ArrayUnorderedList<>();
        this.leverManager = new LeverManager();
    }

    // --- Gestão de Jogadores ---

    public void addPlayer(Player player) throws EmptyCollectionException {
        // Define a posição inicial se ainda não tiver
        if (player.getCurrentRoomId() == null) {
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
        String spawnId = entries.get(targetIndex);

        if(spawnId != null) {
            player.move(spawnId);
            // Certifica-te que adicionaste este método setInitialPosition na classe Player!
            player.setInitialPosition(spawnId);
            System.out.println("Spawn: " + player.getName() + " sala: " + spawnId);
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
        while (true) {
            try {
                if (turnQueue.isEmpty()) break;

                Player nextCandidate = turnQueue.first();

                if (nextCandidate.getSkipTurns() > 0) {
                    nextCandidate.decrementSkipTurn();
                    System.out.println(nextCandidate.getName() + " perdeu a vez! (Restam: " + nextCandidate.getSkipTurns() + ")");
                    // Roda este jogador para o fim da fila imediatamente
                    turnQueue.enqueue(turnQueue.dequeue());
                } else {
                    // Encontrámos um jogador válido!
                    break;
                }
            } catch (EmptyCollectionException e) {
                break;
            }
        }
    }

    // --- Lógica de Movimento ---

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

        // Só verifica efeitos se parou na casa ou chegou ao tesouro
        if (player.getMovementPoints() == 0 || targetRoomId.equals(labyrinth.getTreasureRoom())) {
            checkRoomEffects(player, targetRoomId);
        }

        return true;
    }

    private void checkRoomEffects(Player player, String roomId) throws EmptyCollectionException {
        Room room = labyrinth.getRoom(roomId);
        if (room == null) return;

        // Vitória
        if (roomId.equals(labyrinth.getTreasureRoom())) {
            gameRunning = false;
            System.out.println("JOGO ACABOU! Vencedor: " + player.getName());
            return;
        }

        // Verifica o tipo de sala
        switch (room.getType()) {
            case PENALTY:
                handlePenaltyEvent(player);
                break;
            case LEVER:
                leverManager.checkLever(player, roomId, labyrinth);
                break;
            case BOOST:
                System.out.println("Boost! (Lógica por implementar)");
                break;
            // O caso RIDDLE deve ser tratado na UI (GameWindow) ou aqui se tiveres lógica automática
        }
    }

    // --- Lógica de Bots ---

    public void executeBotTurn() throws EmptyCollectionException {
        Player bot = getCurrentPlayer();

        if (bot == null || !bot.isBot()) return;
        if (!gameRunning) return;

        if (bot.getBotStrategy() == null || bot.getMovementPoints() <= 0) {
            return;
        }

        String targetRoomId = bot.getBotStrategy().nextMove(
                labyrinth,
                bot,
                bot.getMovementPoints()
        );

        if (targetRoomId != null) {
            boolean moved = tryMove(bot, targetRoomId);
            if (moved) {
                System.out.println("Bot " + bot.getName() + " moveu-se para " + targetRoomId);
            }
        } else {
            bot.setMovementPoints(0);
        }
    }

    public boolean isGameRunning() { return gameRunning; }

    // --- Lógica de Penalidades ---

    private void graphMove(Player p, int steps) {
        if (steps == 0) return;

        // Caso especial: Voltar ao Início
        if (steps == -99) {
            String start = p.getInitialPosition(); // Garante que este método existe em Player
            if (start == null) {
                start = labyrinth.getStartRoomId(); // Fallback
            }
            p.move(start);
            System.out.println(p.getName() + " recuou até ao início!");
            return;
        }

        // Define alvo: Positivo = Tesouro, Negativo = Início
        String targetId = (steps > 0) ? labyrinth.getTreasureRoom() : labyrinth.getStartRoomId();
        int moves = Math.abs(steps);

        Iterator<String> path = labyrinth.getShortestPath(p.getCurrentRoomId(), targetId);
        if (path == null || !path.hasNext()) return;
        path.next(); // Ignora a sala atual

        String nextRoom = null;
        while (moves > 0 && path.hasNext()) {
            nextRoom = path.next();
            moves--;
        }

        if (nextRoom != null) {
            p.move(nextRoom);
            System.out.println("O jogador " + p.getName() + " foi forçado a mover-se para " + nextRoom);
        }
    }

    private void handlePenaltyEvent(Player victim) throws EmptyCollectionException {
        Penalty p = penaltyManager.getNextPenalty();
        if (p == null) return;

        System.out.println("PENALIDADE: " + p.getDescription());

        // Usa os Enums definidos na classe Penalty
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