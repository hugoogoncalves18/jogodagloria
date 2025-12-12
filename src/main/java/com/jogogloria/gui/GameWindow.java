package com.jogogloria.gui;

import com.example.Biblioteca.exceptions.NoElementFoundException;
import com.example.Biblioteca.exceptions.EmptyCollectionException;
import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.lists.ArrayOrderedList;
import com.example.Biblioteca.iterators.Iterator;

import com.jogogloria.config.GameConfig;
import com.jogogloria.engine.GameEngine;
import com.jogogloria.engine.RiddleManager;
import com.jogogloria.io.MapLoader;
import com.jogogloria.io.GameStorage;
import com.jogogloria.model.*;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Janela principal onde decorre a partida do jogo
 *
 * @author Hugo Gonçalves
 * @version 3.0
 */
public class GameWindow extends JFrame implements KeyListener {

    private final GameEngine engine;
    private final Labyrinth labyrinth;
    private final BoardPanel boardPanel;
    private final JLabel statusLabel;
    private final RiddleManager riddleManager;
    private final Timer botTimer;
    private final ArrayUnorderedList<Player> allPlayers;
    private final int rows;
    private final int cols;

    /**
     * Cria e inicia a janela de jogo
     * @param labyrinth objeto labyrinth carregado
     * @param engine o motor do jogo
     * @param allPlayers lista de jogadores
     * @param rows numero de linhas da grelha visual
     * @param cols numero de colunas da grelha visual
     */
    public GameWindow(Labyrinth labyrinth, GameEngine engine, ArrayUnorderedList<Player> allPlayers, int rows, int cols) {
        this.labyrinth = labyrinth;
        this.engine = engine;
        this.allPlayers = allPlayers;
        this.rows = rows;
        this.cols = cols;

        setTitle(GameConfig.GAME_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        this.boardPanel = new BoardPanel(labyrinth, allPlayers, rows, cols);
        add(boardPanel, BorderLayout.CENTER);

        this.statusLabel = new JLabel("Bem-vindo! Prime ESPAÇO para rolar o dado. [Z] Voltar atrás, [S] Gravar");
        this.statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(statusLabel, BorderLayout.SOUTH);

        addKeyListener(this);
        setFocusable(true);
        pack();
        setLocationRelativeTo(null);

        this.botTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processBotTurn();
            }
        });

        // Inicializa RiddleManager com o caminho configurado
        this.riddleManager = new RiddleManager(com.jogogloria.config.GameConfig.RIDDLES_FILE);

        updateStatus();
    }

    /**
     * Atualiza a barra de estado inferior com informações do turno atual
     */
    private void updateStatus() {
        if (!engine.isGameRunning()) {
            statusLabel.setText("JOGO TERMINADO!");
            botTimer.stop();
            handleGameOver();
            return;
        }

        Player current = engine.getCurrentPlayer();
        if (current != null) {
            String txt = "Turno de: " + current.getName();

            if (current.getMovementPoints() > 0) {
                txt += " | Movimentos: " + current.getMovementPoints();
            } else {
                txt += " | Rolar Dado (Espaço)";
            }

            txt += " || [Z] Voltar Atrás  [S] Gravar";
            statusLabel.setText(txt);

            if (current.isBot()) {
                if (!botTimer.isRunning()) botTimer.start();
            } else {
                botTimer.stop();
            }
        }
    }

    /**
     * Executa a lógica de um turno do Bot
     */
    private void processBotTurn() {
        if (!engine.isGameRunning()) {
            botTimer.stop();
            return;
        }

        Player current = engine.getCurrentPlayer();
        if (current != null && current.isBot()) {
            if (current.getMovementPoints() <= 0) {
                int dice = (int)(Math.random() * 6) + 1;
                current.setMovementPoints(dice);
                System.out.println("Bot " + current.getName() + " rolou: " + dice);
            }

            try {
                engine.executeBotTurn();
            } catch (EmptyCollectionException e) {
                e.printStackTrace();
            }

            boardPanel.repaint();

            if (current.getMovementPoints() <= 0) {
                // VERIFICA BOOST
                if (current.getBoost() > 0) {
                    current.decrementBoost();
                    System.out.println("Bot tem turno extra!");
                } else {
                    engine.nextTurn();
                }
            }
            updateStatus();
        }
    }

    /**
     * Gere inputs de teclado no jogador humano
     * @param e the event to be processed
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_S) {
            GameStorage.saveGame(engine, "savegame.json");
            JOptionPane.showMessageDialog(this, "jogo guardado com sucesso em savegame.json");
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_Z) {
           boolean success = engine.undo();
           if (success) {
               boardPanel.repaint();
               updateStatus();
               System.out.println("Voltaste atrás");
           } else {
               JOptionPane.showMessageDialog(this, "Não podes recuar mais");
           }
           return;
        }

        if (!engine.isGameRunning()) return;

        Player current = engine.getCurrentPlayer();
        if (current == null || current.isBot()) return;

        // Rolar Dado
        if (current.getMovementPoints() <= 0) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                int dice = (int)(Math.random() * 6) + 1;
                current.setMovementPoints(dice);
                updateStatus(); // Atualiza texto para mostrar pontos
                return;
            } else {
                // Se carregar noutra tecla, avisa
                // statusLabel.setText("Prime ESPAÇO para rolar!");
                // (Comentado para não spammar se carregar setas sem querer)
                return;
            }
        }

        // Movimento
        int dx = 0, dy = 0;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:    dy = -1; break;
            case KeyEvent.VK_DOWN:  dy = 1; break;
            case KeyEvent.VK_LEFT:  dx = -1; break;
            case KeyEvent.VK_RIGHT: dx = 1; break;
            // Removed ENTER skip for safety
        }

        if (dx != 0 || dy != 0) {
            try {
                moveHuman(current, dx, dy);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Tenta mover o jogador humano para a casa adjacente
     * @param p
     * @param dx
     * @param dy
     * @throws Exception
     */
    private void moveHuman(Player p, int dx, int dy) throws Exception {
        Room currentRoom = p.getCurrentRoom();
        if (currentRoom == null) return;

        int targetX = currentRoom.getX() + dx;
        int targetY = currentRoom.getY() + dy;
        Room targetRoom = labyrinth.getRoomAt(targetX, targetY);

        if (targetRoom != null) {
            // Tenta mover (decrementa 1 ponto)
            boolean success = engine.tryMove(p, targetRoom);

            if (success) {
                boardPanel.repaint();

                // Se parou na casa (pontos == 0), verifica Riddle
                if (p.getMovementPoints() <= 0) {

                    if (targetRoom.getType() == Room.RoomType.RIDDLE) {
                        handleRiddleEvent(p);
                        // Nota: O handleRiddleEvent pode dar bónus de movimento!
                        // Se der bónus, p.getMovementPoints() > 0 outra vez.
                    }

                    // Se AINDA tiver 0 pontos (não ganhou bónus ou já gastou)
                    if (p.getMovementPoints() <= 0) {
                        // Verifica Boost (Turno Extra)
                        if (p.getBoost() > 0) {
                            p.decrementBoost();
                            statusLabel.setText("BOOST! " + p.getName() + " joga novamente! (Espaço)");
                        } else {
                            engine.nextTurn();
                        }
                    }
                }
                updateStatus();
            } else {
            }
        }
    }

    /**
     * Dispara um enigma
     * @param player
     * @throws NoElementFoundException
     */
    private void handleRiddleEvent(Player player) throws NoElementFoundException {
        Riddle riddle = riddleManager.getRandomRiddle();

        if (riddle == null) {
            JOptionPane.showMessageDialog(this, "A sala de enigmas está vazia.");
            return;
        }

        if (!player.isBot()) {
            String resposta = JOptionPane.showInputDialog(this,
                    "ENIGMA:\n" + riddle.getQuestion(),
                    "Responde Sabiamente",
                    JOptionPane.QUESTION_MESSAGE);

            if (riddle.checkAnswer(resposta)) {
                player.logEvent(1, "RIDDLE", "Acertou o enigma: " + riddle.getId());
                int bonus = riddle.getBonus();
                JOptionPane.showMessageDialog(this, "Correto! Avanças " + bonus + " casas.");

                // Aplica Bónus Imediato
                try {
                    engine.applyAutoMove(player, bonus);
                    boardPanel.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            } else {
                player.logEvent(1, "RIDDLE", "Errou o enigma: " + riddle.getId());
                int penalty = riddle.getPenalty();
                JOptionPane.showMessageDialog(this, "Errado! A resposta era: " + riddle.getAnswer() +
                        "\nPerdes a vez.");
                player.setSkipTurns(player.getSkipTurns() + penalty);
                player.setMovementPoints(0); // Garante que turno acaba
            }
        } else {
            // Lógica Bot
            boolean acertou = Math.random() > 0.5;
            if (acertou) {
                player.logEvent(1, "RIDDLE", "Bot acertou o enigma: " + riddle.getId());
                System.out.println("Bot acertou enigma! Bónus: " + riddle.getBonus());
                try {
                    engine.applyAutoMove(player, riddle.getBonus());
                } catch (Exception e) {}
            } else {
                player.logEvent(1, "RIDDLE", "Bot errou o enigma: " + riddle.getId());
                System.out.println("Bot errou enigma.");
                player.setSkipTurns(player.getSkipTurns() + riddle.getPenalty());
                player.setMovementPoints(0);
            }
        }
    }

    /**
     * Trata do fim do jogo
     */
    public void handleGameOver() {
        Player winner = null;
        Iterator<Player> it = allPlayers.iterator();

        String treasureId = labyrinth.getTreasureRoom();

        while (it.hasNext()) {
            Player p = it.next();
            if (p.getCurrentRoom() != null && p.getCurrentRoom().getId().equals(treasureId)) {
                winner = p;
                break;
            }
        }
        if (winner != null) winner.incrementWins();

        String winnerName = (winner != null) ? winner.getName() : "Ninguém";
        com.jogogloria.io.History.generateDoc(allPlayers, winnerName);

        ArrayOrderedList<PlayerScore> ranking = new ArrayOrderedList<>();
        it = allPlayers.iterator();
        while (it.hasNext()) {
            ranking.add(new PlayerScore(it.next()));
        }

        StringBuilder score = new StringBuilder();
        score.append("Vencedor: ").append(winnerName).append("\n\n");
        score.append("--- CLASSIFICAÇÃO ---\n");

        Iterator<PlayerScore> rankIt = ranking.iterator();
        int pos = 1;
        while (rankIt.hasNext()) {
            score.append(pos++).append("º - ").append(rankIt.next()).append("\n");
        }

        String[] options = {"Jogar novamente", "Menu", "Sair"};
        int choice = JOptionPane.showOptionDialog(this, score.toString(), "Fim",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            restartGame();
        } else if (choice == 1) {
            this.dispose();
            new MainMenu().setVisible(true);
        } else {
            System.exit(0);
        }
    }

    /**
     * Reinicia a partida com as mesmas configurações
     */
    private void restartGame() {
        this.dispose();

        try{
            Labyrinth labyrint = MapLoader.loadLabyrinth(GameConfig.MAP_FILE);

            GameEngine engine = new GameEngine(labyrinth);

            Iterator<Player> it = allPlayers.iterator();
            while (it.hasNext()) {
                Player p = it.next();
                p.resetForNewMatch();
                engine.addPlayer(p);
            }
            GameWindow window = new GameWindow(labyrinth, engine, allPlayers, rows, cols);
            window.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro ao reiniciar: " + e.getMessage());
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}
}