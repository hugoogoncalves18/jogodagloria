package com.jogogloria.gui;

import com.example.Biblioteca.exceptions.NoElementFoundException;
import com.example.Biblioteca.exceptions.EmptyCollectionException;
import com.jogogloria.engine.GameEngine;
import com.jogogloria.engine.RiddleManager;
import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Player;
import com.jogogloria.model.Room;
import com.jogogloria.model.Riddle;
import com.example.Biblioteca.lists.ArrayUnorderedList;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GameWindow extends JFrame implements KeyListener {

    private final GameEngine engine;
    private final Labyrinth labyrinth;
    private final BoardPanel boardPanel;
    private final JLabel statusLabel;
    private final RiddleManager riddleManager;
    private final Timer botTimer;

    public GameWindow(Labyrinth labyrinth, GameEngine engine, ArrayUnorderedList<Player> allPlayers, int rows, int cols) {
        this.labyrinth = labyrinth;
        this.engine = engine;

        setTitle("Jogo da Glória - Labirinto Tático");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        this.boardPanel = new BoardPanel(labyrinth, allPlayers, rows, cols);
        add(boardPanel, BorderLayout.CENTER);

        this.statusLabel = new JLabel("Bem-vindo! Prime ESPAÇO para rolar o dado.");
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

    private void updateStatus() {
        if (!engine.isGameRunning()) {
            statusLabel.setText("JOGO TERMINADO!");
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

            statusLabel.setText(txt);

            if (current.isBot()) {
                if (!botTimer.isRunning()) botTimer.start();
            } else {
                botTimer.stop();
            }
        }
    }

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

    @Override
    public void keyPressed(KeyEvent e) {
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

    private void moveHuman(Player p, int dx, int dy) throws Exception {
        Room currentRoom = labyrinth.getRoom(p.getCurrentRoomId());
        if (currentRoom == null) return;

        int targetX = currentRoom.getX() + dx;
        int targetY = currentRoom.getY() + dy;
        Room targetRoom = labyrinth.getRoomAt(targetX, targetY);

        if (targetRoom != null) {
            // Tenta mover (decrementa 1 ponto)
            boolean success = engine.tryMove(p, targetRoom.getId());

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
                System.out.println("Bot acertou enigma! Bónus: " + riddle.getBonus());
                try {
                    engine.applyAutoMove(player, riddle.getBonus());
                } catch (Exception e) {}
            } else {
                System.out.println("Bot errou enigma.");
                player.setSkipTurns(player.getSkipTurns() + riddle.getPenalty());
                player.setMovementPoints(0);
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}
}