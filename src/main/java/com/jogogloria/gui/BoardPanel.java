package com.jogogloria.gui;

import com.jogogloria.config.GameConfig;
import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Room;
import com.jogogloria.model.Player;
import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.iterators.Iterator;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

/**
 * Painel gráfico responsável por desenhar o tabuleiro do jogo.
 *
 * @author Hugo Gonçalves
 * @version 3.0
 */
public class BoardPanel extends JPanel {

    private final Labyrinth labyrinth;
    private final ArrayUnorderedList<Player> players;
    private final int rows;
    private final int cols;
    private final ImageManager imageManager;

    public BoardPanel(Labyrinth labyrinth, ArrayUnorderedList<Player> players, int rows, int cols) {
        this.labyrinth = labyrinth;
        this.players = players;
        this.rows = rows;
        this.cols = cols;

        this.imageManager = new ImageManager();

        int width = cols * GameConfig.CELL_SIZE;
        int height = rows * GameConfig.CELL_SIZE;
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 1. Desenhar as Salas
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                drawCell(g2d, x, y);
            }
        }

        // 2. Desenhar os Jogadores por cima
        drawPlayers(g2d);
    }

    private void drawCell(Graphics2D g2, int x, int y) {
        Room room = labyrinth.getRoomAt(x, y);
        if (room == null) return;

        int px = x * GameConfig.CELL_SIZE;
        int py = y * GameConfig.CELL_SIZE;
        int size = GameConfig.CELL_SIZE;

        // --- 1. Desenhar Imagem de Fundo ---
        String typeKey = room.getType().toString();
        BufferedImage img = imageManager.getImage(typeKey);

        if (img != null) {
            g2.drawImage(img, px, py, size, size, null);
        } else {
            g2.setColor(Color.DARK_GRAY);
            g2.drawRect(px, py, size, size);
        }

        // --- 2. Label ---
        if (room.getLabel() != null && !room.getLabel().isEmpty()) {
            g2.setColor(Color.BLACK);
            g2.setFont(g2.getFont().deriveFont(java.awt.Font.BOLD, 14f));
            g2.drawString(room.getLabel(), px + (size / 2) - 5, py + (size / 2) + 5);
        }

        // --- 3. Paredes e Portas ---
        drawWallsAndDoors(g2, x, y, px, py, size, room.getId());
    }

    /**
     * Desenha as paredes ou portas nas bordas da sala.
     *
     */
    private void drawWallsAndDoors(Graphics2D g2, int x, int y, int px, int py, int size, String currentId) {

        // --- Parede à DIREITA ---
        if (x + 1 < cols) {
            String rightId = (x + 1) + "-" + y;
            int wallX = px + size;

            try {
                // Verifica conexão no Grafo
                boolean connected = labyrinth.graphStructure.isConnected();

                if (!connected) {
                    // Sem aresta = Parede Sólida
                    drawWall(g2, wallX, py, wallX, py + size);
                }
                else if (labyrinth.isLocked(currentId, rightId)) {
                    // Com aresta mas peso alto = Porta Trancada
                    drawLockedDoor(g2, wallX, py, wallX, py + size);
                }
            } catch (Exception e) {
                // Em caso de erro no grafo, desenha parede por segurança
                drawWall(g2, wallX, py, wallX, py + size);
            }
        } else {
            // Borda do mapa
            drawWall(g2, px + size, py, px + size, py + size);
        }

        // --- Parede ABAIXO ---
        if (y + 1 < rows) {
            String downId = x + "-" + (y + 1);
            int wallY = py + size;

            try {
                boolean connected = labyrinth.graphStructure.isConnected();

                if (!connected) {
                    drawWall(g2, px, wallY, px + size, wallY);
                }
                else if (labyrinth.isLocked(currentId, downId)) {
                    drawLockedDoor(g2, px, wallY, px + size, wallY);
                }
            } catch (Exception e) {
                drawWall(g2, px, wallY, px + size, wallY);
            }
        } else {
            // Borda do mapa
            drawWall(g2, px, py + size, px + size, py + size);
        }
    }

    private void drawWall(Graphics2D g2, int x1, int y1, int x2, int y2) {
        g2.setColor(new Color(50, 50, 50));
        g2.setStroke(new BasicStroke(4));
        g2.drawLine(x1, y1, x2, y2);
    }

    private void drawLockedDoor(Graphics2D g2, int x1, int y1, int x2, int y2) {
        g2.setColor(new Color(200, 100, 0)); // Laranja
        g2.setStroke(new BasicStroke(8));
        g2.drawLine(x1, y1, x2, y2);

        // Fechadura
        g2.setColor(Color.BLACK);
        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;
        g2.fillOval(midX - 3, midY - 3, 6, 6);
    }

    private void drawPlayers(Graphics2D g2) {
        Iterator<Player> it = players.iterator();
        int offset = 0;
        int size = GameConfig.CELL_SIZE;

        BufferedImage playerImg = imageManager.getImage("PLAYER");

        while (it.hasNext()) {
            Player p = it.next();
            // Referência direta já corrigida
            Room r = p.getCurrentRoom();

            if (r != null) {
                int px = r.getX() * size + 10 + offset;
                int py = r.getY() * size + 10 + offset;
                int pSize = size - 20;

                if (playerImg != null) {
                    g2.drawImage(playerImg, px, py, pSize, pSize, null);
                } else {
                    g2.setColor(p.isBot() ? Color.BLUE : Color.MAGENTA);
                    g2.fillOval(px, py, pSize, pSize);
                }

                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(10f));
                g2.drawString(p.getName().substring(0, 1), px + pSize / 2 - 3, py - 2);

                offset = (offset + 5) % 15;
            }
        }
    }
}