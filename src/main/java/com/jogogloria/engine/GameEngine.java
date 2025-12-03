package com.jogogloria.engine;

import com.example.Biblioteca.exceptions.EmptyCollectionException;
import com.example.Biblioteca.queues.LinkedQueue;
import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Player;
import com.jogogloria.model.Room;
import com.jogogloria.model.Corridor;

import java.util.Iterator;

public class GameEngine {

    private final Labyrinth labyrinth;
    private final LinkedQueue<Player> turnQueue;
    private boolean gameRunning;
    private int playerSpawnIndex = 0;

    public GameEngine(Labyrinth labyrinth) {
        this.labyrinth = labyrinth;
        this.turnQueue = new LinkedQueue<>();
        this.gameRunning = true;
    }

    // --- Gestão de Jogadores ---

    public void addPlayer(Player player) throws EmptyCollectionException {
        // Define a posição inicial se ainda não tiver
        if (player.getCurrentRoomId() == null) {
            // Pega o primeiro ponto de entrada disponível (pode ser melhorado)
            distributePlayerSpawn(player);
        }
        turnQueue.enqueue(player);
    }

    private void distributePlayerSpawn(Player player) {
        ArrayUnorderedList<String> entries = labyrinth.getEntryPoints();

        if (entries.isEmpty()) return;

        int totalEntries = entries.size();
        int targetIndex = playerSpawnIndex % totalEntries;
        String spawmId = entries.get(targetIndex);

        if(spawmId != null) {
            player.move(spawmId);
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
     * Termina o turno do jogador atual e passa para o próximo.
     * Coloca o jogador atual no fim da fila.
     */
    public void nextTurn() {
        if (!gameRunning || turnQueue.isEmpty()) return;

        try {
            Player current = turnQueue.dequeue();

            // Lógica de "Perder a vez" (Penalidade)
            if (current.getSkipTurns() > 0) {
                current.decrementSkipTurn();
                System.out.println(current.getName() + " perdeu a vez!");
                turnQueue.enqueue(current); // Vai para o fim da fila
                return; // O próximo jogador joga no próximo ciclo
            }

            // Reseta pontos de movimento para o próximo turno (ex: rolar dado)
            // Nota: Na UI, deves chamar setMovementPoints() com o valor do dado.

            turnQueue.enqueue(current); // Volta para a fila

        } catch (Exception e) {
            System.err.println("Erro ao processar turno: " + e.getMessage());
        }
    }

    // --- Lógica de Movimento ---

    /**
     * Tenta mover o jogador atual para uma sala vizinha.
     * @param targetRoomId ID da sala de destino
     * @return true se o movimento foi válido e realizado
     */
    public boolean tryMove(Player player, String targetRoomId) {
        if (!gameRunning) return false;

        String currentId = player.getCurrentRoomId();

        // 1. Validar se são vizinhos (Usando o Grafo)
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

        // 2. Validar Corredor (Se está trancado, etc)
        Corridor corridor = labyrinth.getCorridor(currentId, targetRoomId);
        if (corridor != null && corridor.isLocked()) {
            System.out.println("O corredor está trancado!");
            return false;
        }

        // 3. Executar Movimento
        player.move(targetRoomId);
        player.decrementMovementPoints();

        // 4. Verificar Efeitos da Sala (Vitória ou Eventos)
        checkRoomEffects(player, targetRoomId);

        return true;
    }

    private void checkRoomEffects(Player player, String roomId) {
        Room room = labyrinth.getRoom(roomId);
        if (room == null) return;

        // Verifica Vitória
        if (roomId.equals(labyrinth.getTreasureRoom())) {
            gameRunning = false;
            System.out.println("JOGO ACABOU! Vencedor: " + player.getName());
            // Aqui podes disparar um evento para a UI
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
    public void executeBotTurn() {
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
}