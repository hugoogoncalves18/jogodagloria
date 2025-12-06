package com.jogogloria.gui;

import com.example.Biblioteca.exceptions.EmptyCollectionException;
import com.example.Biblioteca.exceptions.NoElementFoundException;
import com.jogogloria.config.GameConfig;
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

        this.statusLabel = new JLabel("Bem-vindo! Prime ENTER para começar.");
        this.statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(statusLabel, BorderLayout.SOUTH);

        addKeyListener(this);
        setFocusable(true);
        pack();
        setLocationRelativeTo(null);

        this.botTimer = new Timer(GameConfig.BOT_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    processBotTurn();
                } catch (EmptyCollectionException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        updateStatus();
        this.riddleManager = new RiddleManager(GameConfig.RIDDLES_FILE);
    }

    // --- Lógica de Turnos ---

    private void updateStatus() {
        if (!engine.isGameRunning()) {
            statusLabel.setText("JOGO TERMINADO!");
            return;
        }

        Player current = engine.getCurrentPlayer();
        if (current != null) {
            statusLabel.setText("Turno de: " + current.getName() +
                    " | Movimentos: " + current.getMovementPoints());

            if (current.isBot()) {
                if (!botTimer.isRunning()) botTimer.start();
            } else {
                botTimer.stop();
            }
        }
    }

    private void processBotTurn() throws EmptyCollectionException {
        if (!engine.isGameRunning()) {
            botTimer.stop();
            return;
        }

        Player current = engine.getCurrentPlayer();
        if (current != null && current.isBot()) {

            if (current.getMovementPoints() <= 0) {
                int dice = (int)(Math.random() * 6) + 1;
                current.setMovementPoints(dice);
                System.out.println("Bot rolou: " + dice);
            }

            engine.executeBotTurn();
            boardPanel.repaint();

            if (current.getMovementPoints() <= 0) {
                engine.nextTurn();
            }
            updateStatus();
        }
    }

    // --- Input do Teclado (Para Humanos) ---

    @Override
    public void keyPressed(KeyEvent e) {
        if (!engine.isGameRunning()) return;

        Player current = engine.getCurrentPlayer();

        if (current == null || current.isBot()) return;

        if (current.getMovementPoints() <= 0) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                int dice = (int)(Math.random() * 6) + 1;
                current.setMovementPoints(dice);
                statusLabel.setText(current.getName() + " rolou um " + dice + "! Use as setas.");
                return;
            } else {
                statusLabel.setText("Prime ESPAÇO para rolar o dado!");
                return;
            }
        }

        // Lógica de Movimento Manual
        int dx = 0, dy = 0;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:    dy = -1; break;
            case KeyEvent.VK_DOWN:  dy = 1; break;
            case KeyEvent.VK_LEFT:  dx = -1; break;
            case KeyEvent.VK_RIGHT: dx = 1; break;
            case KeyEvent.VK_ENTER:
                engine.nextTurn();
                updateStatus();
                return;
        }

        if (dx != 0 || dy != 0) {
            try {
                moveHuman(current, dx, dy);
            } catch (NoElementFoundException | EmptyCollectionException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void moveHuman(Player p, int dx, int dy) throws NoElementFoundException, EmptyCollectionException {
        Room currentRoom = labyrinth.getRoom(p.getCurrentRoomId());
        if (currentRoom == null) return;

        int targetX = currentRoom.getX() + dx;
        int targetY = currentRoom.getY() + dy;

        Room targetRoom = labyrinth.getRoomAt(targetX, targetY);

        if (targetRoom != null) {
            boolean success = engine.tryMove(p, targetRoom.getId());
            if (success) {
                boardPanel.repaint();

                if (p.getMovementPoints() <= 0) {
                    engine.nextTurn();
                }
                if (targetRoom.getType() == Room.RoomType.RIDDLE) {
                    handleRiddleEvent(p);
                }
                updateStatus();
            } else {
                statusLabel.setText("Movimento Inválido (Parede ou Trancado)!");
            }
        }
    }

    private void handleRiddleEvent(Player player) throws NoElementFoundException {
        Riddle riddle = riddleManager.getRandomRiddle();

        if (riddle == null) {
            JOptionPane.showMessageDialog(this, "A sala de enigmas está vazia");
            return;
        }

        if (!player.isBot()) {
            String resposta = JOptionPane.showInputDialog(this, "ENIGMA:\n" + riddle.getQuestion(), "Responde Sabiamente", JOptionPane.QUESTION_MESSAGE);

            if (resposta != null && resposta.equalsIgnoreCase(riddle.getAnswer())) {
                int bonus = riddle.getBonus();
                JOptionPane.showMessageDialog(this, "Correto! Ganhaste um movimento extra.");
                player.setMovementPoints(player.getMovementPoints() + bonus); // Exemplo de prémio
            } else {
                int penalty = riddle.getPenalty();
                JOptionPane.showMessageDialog(this, "Errado! A resposta era: " + riddle.getAnswer() + "\nPerdes o resto do turno.");
                player.setMovementPoints(player.getMovementPoints() - penalty);
            }
        }
        else {
            boolean acertou = Math.random() > 0.5;
            if (acertou) {
                System.out.println("Bot " + player.getName() + " acertou o enigma!");
                player.setMovementPoints(player.getMovementPoints() + 1);
            } else {
                System.out.println("Bot " + player.getName() + " errou o enigma.");
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