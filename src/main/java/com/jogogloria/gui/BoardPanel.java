package com.jogogloria.gui;

import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Room;
import com.jogogloria.model.Corridor;
import com.jogogloria.model.Player;
import com.example.Biblioteca.lists.ArrayUnorderedList;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.util.Iterator; // Apenas para iterar as tuas listas personalizadas

public class BoardPanel extends JPanel {

    private final Labyrinth labyrinth;
    private final ArrayUnorderedList<Player> players;
    private final int rows;
    private final int cols;
    private final int CELL_SIZE = 50; // Tamanho de cada quadrado em pixels

    public BoardPanel(Labyrinth labyrinth, ArrayUnorderedList<Player> players, int rows, int cols) {
        this.labyrinth = labyrinth;
        this.players = players;
        this.rows = rows;
        this.cols = cols;

        // Define o tamanho do painel baseado na grelha
        setPreferredSize(new Dimension(cols * CELL_SIZE, rows * CELL_SIZE));
        setBackground(Color.BLACK); // Fundo preto para áreas vazias
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 1. Desenhar as Salas e Corredores
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

        // Se não houver sala (espaço vazio na matriz), não desenha nada (fica preto)
        if (room == null) return;

        int px = x * CELL_SIZE;
        int py = y * CELL_SIZE;

        // --- A. Preenchimento (Cor do tipo de sala) ---
        g2.setColor(getRoomColor(room.getType()));
        g2.fillRect(px, py, CELL_SIZE, CELL_SIZE);

        // --- B. Label (Texto: S, F, ?, !) ---
        g2.setColor(Color.BLACK);
        if (room.getLabel() != null && !room.getLabel().isEmpty()) {
            g2.drawString(room.getLabel(), px + 15, py + 30);
        }

        // --- C. Paredes (Verifica conexões no Grafo) ---
        g2.setStroke(new BasicStroke(3)); // Linha grossa para paredes
        g2.setColor(Color.DARK_GRAY);

        String currentId = room.getId();

        // Verificar parede à DIREITA
        // Se estamos na borda ou não existe corredor para a direita -> Desenha Parede
        if (x + 1 < cols) {
            String rightId = (x + 1) + "-" + y;
            Corridor c = labyrinth.getCorridor(currentId, rightId);
            if (c == null) {
                g2.drawLine(px + CELL_SIZE, py, px + CELL_SIZE, py + CELL_SIZE);
            } else if (c.isLocked()) {
                // Desenha uma "porta" vermelha se estiver trancado
                g2.setColor(Color.RED);
                g2.drawLine(px + CELL_SIZE, py, px + CELL_SIZE, py + CELL_SIZE);
                g2.setColor(Color.DARK_GRAY); // Reseta
            }
        } else {
            g2.drawLine(px + CELL_SIZE, py, px + CELL_SIZE, py + CELL_SIZE); // Borda do mapa
        }

        // Verificar parede ABAIXO
        if (y + 1 < rows) {
            String downId = x + "-" + (y + 1);
            Corridor c = labyrinth.getCorridor(currentId, downId);
            if (c == null) {
                g2.drawLine(px, py + CELL_SIZE, px + CELL_SIZE, py + CELL_SIZE);
            } else if (c.isLocked()) {
                g2.setColor(Color.RED);
                g2.drawLine(px, py + CELL_SIZE, px + CELL_SIZE, py + CELL_SIZE);
                g2.setColor(Color.DARK_GRAY);
            }
        } else {
            g2.drawLine(px, py + CELL_SIZE, px + CELL_SIZE, py + CELL_SIZE); // Borda do mapa
        }
    }

    private void drawPlayers(Graphics2D g2) {
        Iterator<Player> it = players.iterator();

        // Offset para separar jogadores se estiverem na mesma sala
        int offset = 5;

        while (it.hasNext()) {
            Player p = it.next();
            Room r = labyrinth.getRoom(p.getCurrentRoomId());

            if (r != null) {
                int px = r.getX() * CELL_SIZE + 10 + offset;
                int py = r.getY() * CELL_SIZE + 10 + offset;

                // Cor diferente para Bots e Humanos
                g2.setColor(p.isBot() ? Color.BLUE : Color.MAGENTA);
                g2.fillOval(px, py, 20, 20); // Token do jogador

                // Borda do token
                g2.setColor(Color.WHITE);
                g2.drawOval(px, py, 20, 20);

                // Nome pequeno
                g2.setColor(Color.BLACK);
                g2.setFont(g2.getFont().deriveFont(10f));
                g2.drawString(p.getName().substring(0, 1), px + 6, py + 14);

                offset = (offset + 10) % 20; // Evitar sobreposição total
            }
        }
    }

    private Color getRoomColor(Room.RoomType type) {
        switch (type) {
            case START:   return new Color(144, 238, 144); // Verde claro
            case EXIT:    return new Color(255, 215, 0);   // Dourado
            case PENALTY: return new Color(255, 99, 71);   // Vermelho Tomate
            case BOOST:   return new Color(135, 206, 250); // Azul Céu
            case RIDDLE:  return new Color(221, 160, 221); // Roxo ameixa
            default:      return Color.WHITE;
        }
    }
}