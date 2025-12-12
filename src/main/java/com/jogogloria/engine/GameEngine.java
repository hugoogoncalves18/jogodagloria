package com.jogogloria.engine;

import com.example.Biblioteca.exceptions.EmptyCollectionException;
import com.example.Biblioteca.queues.LinkedQueue;
import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.iterators.Iterator;
import com.example.Biblioteca.stacks.LinkedStack;
import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Player;
import com.jogogloria.model.Room;
import com.jogogloria.model.Penalty;
import com.jogogloria.model.Boost;
import com.jogogloria.model.GameSnapshot;
import com.jogogloria.model.Lever;

/**
 * Motor Central do Jogo (Game Engine).
 *
 * @author Hugo Gonçalves
 * @version 4.0
 */
public class GameEngine {

    private final Labyrinth labyrinth;
    private final LinkedQueue<Player> turnQueue;
    private final ArrayUnorderedList<Player> allPlayers;
    private final LinkedStack<GameSnapshot> history;
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
        this.history = new LinkedStack<>();
        this.gameRunning = true;

        this.penaltyManager = new PenaltyManager();
        this.leverManager = new LeverManager();
        this.boostManager = new BoostManager();
    }

    // --- Gestão de Jogadores ---

    public void addPlayer(Player player) throws EmptyCollectionException {
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

        String spawnId = entries.get(targetIndex);
        Room spawnRoom = labyrinth.getRoom(spawnId);

        if (spawnRoom != null) {
            player.move(spawnRoom);
            // [CORRIGIDO] Usa setInitialRoom conforme refatorização do Player
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

    public Labyrinth getLabyrinth() {
        return labyrinth;
    }

    public Iterator<Player> getAllPlayersIterator() {
        return allPlayers.iterator();
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
     */
    public boolean tryMove(Player player, Room targetRoom) throws EmptyCollectionException {
        if (!gameRunning || targetRoom == null) return false;

        Room currentRoom = player.getCurrentRoom();
        if (currentRoom == null) return false;

        String currentId = currentRoom.getId();
        String targetId = targetRoom.getId();

        if (!labyrinth.isValidMove(currentId, targetId)) {
            System.out.println("Movimento inválido (Parede ou Porta Trancada).");
            return false;
        }

        //Guardar estado ants de mover
        saveSnapshot();

        // Executar Movimento
        player.move(targetRoom);
        player.logEvent(countTurn, "MOVE", "Moveu-se para: " + targetId);
        player.decrementMovementPoints();

        // Verificar efeitos
        if (player.getMovementPoints() == 0 || targetId.equals(labyrinth.getTreasureRoom())) {
            checkRoomEffects(player, targetRoom);
        }

        return true;
    }

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
                // [CORRIGIDO] Passamos o labyrinth porque o LeverManager precisa dele
                // para alterar o peso da aresta no grafo.
                leverManager.checkLever(player, room, labyrinth);
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
                int stepsBack = -Math.abs(p.getValue());
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

    //Lógica de UNDO

    /**
     *
     */
    public void saveSnapshot() {
        //Guarda quem é o jogador atual
        Player current = getCurrentPlayer();
        GameSnapshot snapshot = new GameSnapshot(current);

        //Guarda os estado dos jogadores
        Iterator<Player> it = allPlayers.iterator();
        while (it.hasNext()) {
            Player p = it.next();
            snapshot.playerState.addToRear(new GameSnapshot.PlayerMoment(p));
        }

        //Guarda as referências das alavancas ativadas
        Iterator<Room> roomIt = labyrinth.getRoomsIterator();

        while ((roomIt.hasNext())) {
            Room r = roomIt.next();
            if (r.hasLever()) {
                Lever l = r.getLever();
                if (l.isActivated()) {
                    snapshot.activatedLevers.addToRear(l);
                }
            }
        }
        history.push(snapshot);
        System.out.println("Snapshot guardado em Stack: " + history.size());
    }

    /**
     * Restaura o estado anterior.
     */
    public boolean undo() {
        if (history.isEmpty()) return false;

        try {
            GameSnapshot snapshot = history.pop();
            restoreState(snapshot);
            return true;
        } catch (EmptyCollectionException e) {
            return false;
        }
    }

    private void restoreState(GameSnapshot snapshot) {
        // 1. Restaurar Jogadores (Usando referências diretas)
        Iterator<GameSnapshot.PlayerMoment> it = snapshot.playerState.iterator();
        while (it.hasNext()) {
            GameSnapshot.PlayerMoment memento = it.next();

            Player p = memento.playerRef; // Temos o objeto real aqui!

            // Restaura posição (Objeto Room)
            if (memento.roomRef != null) {
                p.move(memento.roomRef);
            }

            p.setMovementPoints(memento.movementPoints);
            p.setSkipTurns(memento.skipTurns);
            // p.setBoost(memento.boostCount);
        }

        // 2. Restaurar TurnQueue
        rebuildTurnQueue(snapshot.currentPlayer);

        // 3. Restaurar Alavancas e Grafo
        // Passo A: Reset total (Trancar tudo)
        resetAllLevers();

        // Passo B: Ativar apenas o que estava no snapshot
        Iterator<Lever> leverIt = snapshot.activatedLevers.iterator();
        while (leverIt.hasNext()) {
            Lever l = leverIt.next();

            // Ativa o objeto Lever
            l.setActivated(true);

            // Destranca no Grafo (Aqui temos de extrair ID porque o Grafo é de Strings)
            // Mas a lógica veio toda de objetos
            labyrinth.setConnectionLocked(l.getRoomA().getId(), l.getRoomB().getId(), false);
        }

        System.out.println("[UNDO] Voltámos para o turno de: " + snapshot.currentPlayer.getName());
    }

    //Helpers

    private void resetAllLevers() {
        Iterator<Room> roomIt = labyrinth.getRoomsIterator();
        while (roomIt.hasNext()) {
            Room r = roomIt.next();
            if (r.hasLever()) {
                Lever l = r.getLever();
                if (l.isActivated()) {
                    l.setActivated(false);
                    // Manda trancar a aresta no grafo
                    labyrinth.setConnectionLocked(l.getRoomA().getId(), l.getRoomB().getId(), true);
                }
            }
        }
    }

    private void rebuildTurnQueue(Player targetCurrent) {
        // Limpa a fila
        while (!turnQueue.isEmpty()) {
            try { turnQueue.dequeue(); } catch (Exception e) {}
        }

        // Reconstrói a fila a começar no jogador certo
        boolean found = false;

        // 1. Adiciona do atual até ao fim da lista
        Iterator<Player> it = allPlayers.iterator();
        while(it.hasNext()) {
            Player p = it.next();
            // Comparação de objetos (referências)
            if (p == targetCurrent) found = true;
            if (found) turnQueue.enqueue(p);
        }

        // 2. Adiciona do início até ao atual
        it = allPlayers.iterator();
        while(it.hasNext()) {
            Player p = it.next();
            if (p == targetCurrent) break;
            turnQueue.enqueue(p);
        }
    }

    /**
     *
     * @return
     */
    public boolean isGameRunning() { return gameRunning; }
}