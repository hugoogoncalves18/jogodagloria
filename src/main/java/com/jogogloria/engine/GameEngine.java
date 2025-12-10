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
 * Motor central do Jogo
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class GameEngine {

    private final Labyrinth labyrinth;
    private final LinkedQueue<Player> turnQueue;
    private final ArrayUnorderedList<Player> allPlayers; // Lista de todos os jogadores
    private boolean gameRunning;
    private int playerSpawnIndex = 0;
    private int countTurn = 1;

    // Gestores
    private final PenaltyManager penaltyManager;
    private final LeverManager leverManager;
    private final BoostManager boostManager;

    /**
     * Inicia o motor do jogo
     * @param labyrinth O labirinto carregado onde o jogo vai decorrer
     */
    public GameEngine(Labyrinth labyrinth) {
        this.labyrinth = labyrinth;
        this.turnQueue = new LinkedQueue<>();
        this.allPlayers = new ArrayUnorderedList<>();
        this.gameRunning = true;

        // Inicializar Gestores
        this.penaltyManager = new PenaltyManager();
        this.leverManager = new LeverManager();
        this.boostManager = new BoostManager();
    }

    // --- Gestão de Jogadores ---

    /**
     * Adiciona um jogador á partida
     * @param player O jogador a adicionar
     * @throws EmptyCollectionException Se ocorrer erro interno nas listas
     */
    public void addPlayer(Player player) throws EmptyCollectionException {
        if (player.getCurrentRoomId() == null) {
            distributePlayerSpawn(player);
        }
        turnQueue.enqueue(player);
        allPlayers.addToRear(player); // Guarda na lista global para efeitos de área
    }

    /**
     * Distribui os jogadores pelos pontos de entrada
     * @param player
     */
    private void distributePlayerSpawn(Player player) {
        ArrayUnorderedList<String> entries = labyrinth.getEntryPoints();
        if (entries.isEmpty()) return;

        int totalEntries = entries.size();
        int targetIndex = playerSpawnIndex % totalEntries;
        String spawnId = entries.get(targetIndex);

        if (spawnId != null) {
            player.move(spawnId);
            player.setInitialPosition(spawnId); // Guarda o spawn original
            System.out.println("Spawn: " + player.getName() + " sala: " + spawnId);
        }
        playerSpawnIndex++;
    }

    // --- Ciclo de Jogo (Turnos) ---

    /**
     * Obtém o jogador que tem a vez de jogar
     * @return objeto {@link Player} no inicio da fila ou {@code null} se a fila estiver vazia
     */
    public Player getCurrentPlayer() {
        if (turnQueue.isEmpty()) return null;
        try {
            return turnQueue.first();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Avança para o próximo turno
     */
    public void nextTurn() {
        if (!gameRunning || turnQueue.isEmpty()) return;

        // 1. O jogador que acabou de jogar vai para o fim da fila
        try {
            Player finishedPlayer = turnQueue.dequeue();
            finishedPlayer.setMovementPoints(0); // Reset forçado dos pontos
            turnQueue.enqueue(finishedPlayer);
        } catch (EmptyCollectionException e) {
            System.err.println("Erro crítico: Fila vazia.");
            return;
        }

        // 2. Procurar o próximo jogador válido
        while (true) {
            try {
                if (turnQueue.isEmpty()) break;

                Player nextCandidate = turnQueue.first();

                if (nextCandidate.getSkipTurns() > 0) {
                    nextCandidate.decrementSkipTurn();
                    System.out.println(nextCandidate.getName() + " perdeu a vez! (Restam: " + nextCandidate.getSkipTurns() + ")");
                    turnQueue.enqueue(turnQueue.dequeue()); // Roda para o fim
                } else {
                    break; // Jogador válido encontrado
                }
            } catch (EmptyCollectionException e) {
                break;
            }
        }
        countTurn++;
    }

    // --- Lógica de Movimento ---

    /**
     * Tenta mover o jogador atual para uma sala de destino
     * @param player jogador a mover
     * @param targetRoomId ID da sala para onde quer ir
     * @return {@code true} se o movimento foi realizado
     * @throws EmptyCollectionException em caso de erro nas estruturas de dados
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
            System.out.println("Movimento inválido.");
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
        player.logEvent(countTurn, "MOVE", "Moveu-se para: " + targetRoomId);
        player.decrementMovementPoints();

        // Verificar efeitos ao parar ou ganhar
        if (player.getMovementPoints() == 0 || targetRoomId.equals(labyrinth.getTreasureRoom())) {
            checkRoomEffects(player, targetRoomId);
        }

        return true;
    }

    /**
     * Verifica e executa os efeitos especiais da sala onde o ogador aterrou
     * @param player
     * @param roomId
     * @throws EmptyCollectionException
     */
    private void checkRoomEffects(Player player, String roomId) throws EmptyCollectionException {
        Room room = labyrinth.getRoom(roomId);
        if (room == null) return;

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
                leverManager.checkLever(player, roomId, labyrinth);
                player.logEvent(countTurn, "LEVER", "Ativou a alavanca no: " + roomId);
                break;
            case BOOST:
                handleBoostEvent(player);
                break;
            // Riddle é tratado na UI
        }
    }

    // --- Eventos Especiais ---

    /**
     * Processa a lógica de apanhar um Boost
     * @param player
     */
    private void handleBoostEvent(Player player) {
        Boost b = boostManager.getNextBoost();
        if (b != null) {
            System.out.println("BOOST! " + b.getDescription());
            player.addBoost();
        }
    }

    /**
     * Processa a lógica de cair numa penalidade
     * @param victim
     * @throws EmptyCollectionException
     */
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

    /**
     * Aplica movimento automático a um jogador
     */
    public void applyAutoMove(Player p, int steps) {
        if (steps == 0) return;

        if (steps == -99) {
            String start = p.getInitialPosition();
            if (start == null) start = labyrinth.getStartRoomId();
            p.move(start);
            System.out.println(p.getName() + " voltou ao início!");
            return;
        }

        String targetId = (steps > 0) ? labyrinth.getTreasureRoom() : labyrinth.getStartRoomId();
        //Proteção contra mapas mal configurados
        if (targetId == null) {
            System.out.println("Erro: sala de destino não definida");
            return;
        }
        int moves = Math.abs(steps);

        Iterator<String> path = labyrinth.getShortestPath(p.getCurrentRoomId(), targetId);
        if (path == null || !path.hasNext()){
            System.out.println("AutoMove falhou: Não há caminho para " + targetId);
            return;
        }
        path.next(); // Ignora atual

        String nextRoom = null;
        while (moves > 0 && path.hasNext()) {
            nextRoom = path.next();
            moves--;
        }

        if (nextRoom != null) {
            p.move(nextRoom);
            p.logEvent(countTurn, "AUTO_MOVE", "Movimento para: " + nextRoom);
            System.out.println("Movimento Automático: " + p.getName() + " foi para " + nextRoom);
        }
    }

    // --- Bots ---

    /**
     * Executa a lógica de inteligência para o Bot atual
     * @throws EmptyCollectionException Se ocorrer erro nas listas
     */
    public void executeBotTurn() throws EmptyCollectionException {
        Player bot = getCurrentPlayer();
        if (bot == null || !bot.isBot() || !gameRunning) return;

        if (bot.getBotStrategy() == null || bot.getMovementPoints() <= 0) return;

        String targetRoomId = bot.getBotStrategy().nextMove(labyrinth, bot, bot.getMovementPoints());

        if (targetRoomId != null) {
            boolean moved = tryMove(bot, targetRoomId);
            if (moved) System.out.println("Bot " + bot.getName() + " moveu para " + targetRoomId);
        } else {
            bot.setMovementPoints(0);
        }
    }

    /**
     * Verifica se o jogo está a decorrer
     * @return {@code true} se o jogo está ativo, {@code false} se alguém já ganhou
     */
    public boolean isGameRunning() { return gameRunning; }
}