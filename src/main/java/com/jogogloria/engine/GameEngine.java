package com.jogogloria.engine;

import com.example.Biblioteca.exceptions.EmptyCollectionException;
import com.example.Biblioteca.queues.LinkedQueue;
import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.iterators.Iterator;
import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Player;
import com.jogogloria.model.Room;
import com.jogogloria.model.Corridor;
import com.jogogloria.model.Penalty;
import com.jogogloria.model.Boost;
import com.jogogloria.model.Room.RoomType;

/**
 * Motor Central do Jogo (Game Engine).
 *
 * @author Hugo Gonçalves
 * @version 2.0
 */
public class GameEngine {

    private final Labyrinth labyrinth;
    private final LinkedQueue<Player> turnQueue;
    private final ArrayUnorderedList<Player> allPlayers;
    private boolean gameRunning;
    private int playerSpawnIndex = 0;
    private int countTurn = 1;

    // Gestores
    private final PenaltyManager penaltyManager;
    private final LeverManager leverManager;
    private final BoostManager boostManager;

    public GameEngine(Labyrinth labyrinth) {
        this.labyrinth = labyrinth;
        this.turnQueue = new LinkedQueue<>();
        this.allPlayers = new ArrayUnorderedList<>();
        this.gameRunning = true;

        this.penaltyManager = new PenaltyManager();
        this.leverManager = new LeverManager();
        this.boostManager = new BoostManager();
    }

    // --- Gestão de Jogadores ---

    public void addPlayer(Player player) throws EmptyCollectionException {
        // [REFATORADO] Verifica se o objeto Room é nulo
        if (player.getCurrentRoom() == null) {
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

        // Obtém o ID e depois o Objeto Room
        String spawnId = entries.get(targetIndex);
        Room spawnRoom = labyrinth.getRoom(spawnId);

        if (spawnRoom != null) {
            // [REFATORADO] Usa objetos
            player.move(spawnRoom);
            player.setInitialPosition(spawnRoom);
            System.out.println("Spawn: " + player.getName() + " sala: " + spawnId);
        }
        playerSpawnIndex++;
    }

    // --- Ciclo de Jogo ---

    public Player getCurrentPlayer() {
        if (turnQueue.isEmpty()) return null;
        try {
            return turnQueue.first();
        } catch (Exception e) { return null; }
    }

    public void nextTurn() {
        if (!gameRunning || turnQueue.isEmpty()) return;

        try {
            Player finishedPlayer = turnQueue.dequeue();
            finishedPlayer.setMovementPoints(0);
            turnQueue.enqueue(finishedPlayer);
        } catch (EmptyCollectionException e) { return; }

        while (true) {
            try {
                if (turnQueue.isEmpty()) break;
                Player nextCandidate = turnQueue.first();

                if (nextCandidate.getSkipTurns() > 0) {
                    nextCandidate.decrementSkipTurn();
                    System.out.println(nextCandidate.getName() + " perdeu a vez!");
                    turnQueue.enqueue(turnQueue.dequeue());
                } else {
                    break;
                }
            } catch (EmptyCollectionException e) { break; }
        }
        countTurn++;
    }

    // --- Lógica de Movimento ---

    /**
     * Tenta mover o jogador para uma sala de destino.
     *
     */
    public boolean tryMove(Player player, Room targetRoom) throws EmptyCollectionException {
        if (!gameRunning || targetRoom == null) return false;

        // Obtém a sala atual (Objeto)
        Room currentRoom = player.getCurrentRoom();
        if (currentRoom == null) return false;

        // Precisamos dos IDs para validar no Labirinto (que usa grafo de strings)
        String currentId = currentRoom.getId();
        String targetId = targetRoom.getId();

        // 1. Validar Vizinhança (usando IDs no grafo)
        boolean isNeighbor = false;
        ArrayUnorderedList<String> neighbors = labyrinth.getNeighbors(currentId);
        Iterator<String> it = neighbors.iterator();
        while (it.hasNext()) {
            if (it.next().equals(targetId)) {
                isNeighbor = true;
                break;
            }
        }

        if (!isNeighbor) {
            System.out.println("Movimento inválido.");
            return false;
        }

        // 2. Validar Corredor
        Corridor corridor = labyrinth.getCorridor(currentId, targetId);
        if (corridor != null && corridor.isLocked()) {
            System.out.println("O corredor está trancado!");
            return false;
        }

        // 3. Executar Movimento (Passando o Objeto)
        player.move(targetRoom);
        player.logEvent(countTurn, "MOVE", "Moveu-se para: " + targetId);
        player.decrementMovementPoints();

        // Verificar efeitos
        if (player.getMovementPoints() == 0 || targetId.equals(labyrinth.getTreasureRoom())) {
            checkRoomEffects(player, targetRoom);
        }

        return true;
    }

    /**
     * Verifica efeitos da sala (Objeto Room).
     */
    private void checkRoomEffects(Player player, Room room) throws EmptyCollectionException {
        String roomId = room.getId();

        if (roomId.equals(labyrinth.getTreasureRoom())) {
            gameRunning = false;
            System.out.println("JOGO ACABOU! Vencedor: " + player.getName());
            return;
        }

        switch (room.getType()) {
            case PENALTY:
                handlePenaltyEvent(player);
                break;
            case LEVER:
                // LeverManager já foi refatorado para ser stateless
                leverManager.checkLever(player, room);
                player.logEvent(countTurn, "LEVER", "Ativou a alavanca no: " + roomId);
                break;
            case BOOST:
                handleBoostEvent(player);
                break;
        }
    }

    // --- Eventos Especiais ---

    private void handleBoostEvent(Player player) {
        Boost b = boostManager.getNextBoost();
        if (b != null) {
            System.out.println("BOOST! " + b.getDescription());
            player.addBoost();
        }
    }

    private void handlePenaltyEvent(Player victim) throws EmptyCollectionException {
        Penalty p = penaltyManager.getNextPenalty();
        if (p == null) return;
        victim.logEvent(countTurn, "PENALTY", p.getDescription());
        System.out.println("PENALIDADE: " + p.getDescription());

        switch (p.getType()) {
            case RETREAT:
                applyAutoMove(victim, -Math.abs(p.getValue()));
                break;
            case SKIP_TURN:
                victim.setSkipTurns(p.getValue());
                break;
            case PLAYERS_BENEFITS:
                Iterator<Player> it = allPlayers.iterator();
                while (it.hasNext()) {
                    Player other = it.next();
                    if (!other.getId().equals(victim.getId())) {
                        applyAutoMove(other, Math.abs(p.getValue()));
                    }
                }
                break;
        }
    }

    public void applyAutoMove(Player p, int steps) {
        if (steps == 0) return;

        // Voltar ao início
        if (steps == -99) {
            Room startRoom = p.getInitialRoom();
            if (startRoom == null) {
                // Fallback se não tiver initialRoom definido
                startRoom = labyrinth.getRoom(labyrinth.getStartRoomId());
            }
            if (startRoom != null) {
                p.move(startRoom);
                System.out.println(p.getName() + " voltou ao início!");
            }
            return;
        }

        String targetId = (steps > 0) ? labyrinth.getTreasureRoom() : labyrinth.getStartRoomId();
        if (targetId == null || p.getCurrentRoom() == null) return;

        int moves = Math.abs(steps);

        // Pathfinding usa Strings
        Iterator<String> path = labyrinth.getShortestPath(p.getCurrentRoom().getId(), targetId);

        if (path == null || !path.hasNext()) return;
        path.next(); // Ignora atual

        String nextRoomId = null;
        while (moves > 0 && path.hasNext()) {
            nextRoomId = path.next();
            moves--;
        }

        if (nextRoomId != null) {
            Room r = labyrinth.getRoom(nextRoomId);
            if (r != null) {
                p.move(r);
                p.logEvent(countTurn, "AUTO_MOVE", "Movimento para: " + nextRoomId);
            }
        }
    }

    // --- Bots ---

    public void executeBotTurn() throws EmptyCollectionException {
        Player bot = getCurrentPlayer();
        if (bot == null || !bot.isBot() || !gameRunning) return;

        if (bot.getBotStrategy() == null || bot.getMovementPoints() <= 0) return;

        // Bot decide e retorna um ID (String)
        String targetId = bot.getBotStrategy().nextMove(labyrinth, bot, bot.getMovementPoints());

        if (targetId != null) {
            Room targetRoom = labyrinth.getRoom(targetId);
            if (targetRoom != null) {
                boolean moved = tryMove(bot, targetRoom);
                if (moved) System.out.println("Bot " + bot.getName() + " moveu para " + targetId);
            }
        } else {
            bot.setMovementPoints(0);
        }
    }

    public boolean isGameRunning() { return gameRunning; }
}