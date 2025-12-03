package com.jogogloria.io;

import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Room;
import com.jogogloria.model.Room.RoomType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MapLoader {

    // Códigos da Matriz
    private static final int EMPTY = 0;
    private static final int START = 1;
    private static final int NORMAL = 2;
    private static final int RIDDLE = 3;
    private static final int PENALTY = 4;
    private static final int BOOST = 5;
    private static final int EXIT = 9;

    /**
     * Lê um ficheiro JSON e retorna o Labirinto.
     */
    public static Labyrinth loadLabyrinth(String jsonFilePath) {
        int[][] grid = parseJsonGrid(jsonFilePath);
        return createLabyrinthFromGrid(grid);
    }

    /**
     * Versão antiga (mantida para compatibilidade ou testes manuais)
     */
    public static Labyrinth loadLabyrinth(int[][] gridData) {
        return createLabyrinthFromGrid(gridData);
    }

    // --- Parser Manual de JSON (Sem bibliotecas externas) ---
    private static int[][] parseJsonGrid(String filePath) {
        StringBuilder jsonContent = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                jsonContent.append(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler ficheiro: " + e.getMessage());
            return new int[0][0];
        }

        String content = jsonContent.toString();

        // 1. Encontrar o início e fim da matriz: "grid": [ [ ... ] ]
        // Procuramos o primeiro "[[" e o último "]]"
        int startIndex = content.indexOf("[[");
        int endIndex = content.lastIndexOf("]]");

        if (startIndex == -1 || endIndex == -1) {
            throw new RuntimeException("Formato JSON inválido: Matriz 'grid' não encontrada.");
        }

        // Conteúdo "bruto" da matriz: 1,2],[3,4
        String rawMatrix = content.substring(startIndex + 2, endIndex); // Remove os [[ e ]] externos

        // 2. Separar por linhas (usa "],[" ou "], [" como delimitador)
        // O regex "],\s*\[" trata de "],[" com ou sem espaços
        String[] rowsRaw = rawMatrix.split("\\],\\s*\\[");

        int rows = rowsRaw.length;
        int cols = rowsRaw[0].split(",").length;
        int[][] grid = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            String[] colsRaw = rowsRaw[i].split(",");
            for (int j = 0; j < colsRaw.length; j++) {
                // Remove espaços e parseia
                grid[i][j] = Integer.parseInt(colsRaw[j].trim());
            }
        }

        System.out.println("Mapa JSON carregado com sucesso: " + rows + "x" + cols);
        return grid;
    }

    // --- Lógica de Construção do Grafo (Refatorada para reutilização) ---
    private static Labyrinth createLabyrinthFromGrid(int[][] gridData) {
        Labyrinth labyrinth = new Labyrinth();
        int rows = gridData.length;
        if (rows == 0) return labyrinth;
        int cols = gridData[0].length;

        // 1. Criar Salas
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int code = gridData[y][x];
                if (code != EMPTY) {
                    Room room = createRoom(x, y, code);
                    labyrinth.addRoom(room);

                    // START (1) ponto de entrada válido
                    if (code == START ) {
                        labyrinth.addEntryPoint(room.getId());
                        labyrinth.setStartRoom(room.getId());
                    }
                    if (code == EXIT) labyrinth.setTreasureRoom(room.getId());
                }
            }
        }

        // 2. Criar Corredores (Conectar Vizinhos)
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (labyrinth.getRoomAt(x, y) == null) continue;
                String currentId = x + "-" + y;

                // Conectar Norte
                if (y > 0) {
                    String upId = x + "-" + (y - 1);
                    if (labyrinth.getRoom(upId) != null) labyrinth.addCorridor(currentId, upId);
                }
                // Conectar Oeste
                if (x > 0) {
                    String leftId = (x - 1) + "-" + y;
                    if (labyrinth.getRoom(leftId) != null) labyrinth.addCorridor(currentId, leftId);
                }
            }
        }
        return labyrinth;
    }

    private static Room createRoom(int x, int y, int code) {
        String id = x + "-" + y;
        RoomType type;
        String label = "";

        switch (code) {
            case START:   type = RoomType.START; label = "S"; break;
            case EXIT:    type = RoomType.EXIT; label = "Fim"; break;
            case RIDDLE:  type = RoomType.RIDDLE; label = "?"; break;
            case PENALTY: type = RoomType.PENALTY; label = "!"; break;
            case BOOST:   type = RoomType.BOOST; label = ">>"; break;
            default:      type = RoomType.NORMAL; label = ""; break;
        }
        return new Room(id, type, label);
    }
}