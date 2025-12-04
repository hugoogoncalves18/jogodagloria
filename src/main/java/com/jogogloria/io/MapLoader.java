package com.jogogloria.io;

import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Lever;
import com.jogogloria.model.Room;
import com.jogogloria.model.Corridor;
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
    private static final int LEVER = 6;
    private static final int EXIT = 9;

    public static Labyrinth loadLabyrinth(String jsonFilePath) {
        String jsonContent = readJsonFile(jsonFilePath);

        if (jsonContent.isEmpty()) {
            System.err.println("Erro: Ficheiro JSON vazio ou não encontrado.");
            return new Labyrinth();
        }

        int[][] grid = parseGridData(jsonContent);
        Labyrinth labyrinth = createLabyrinthFromGrid(grid);

        applyLocks(jsonContent, labyrinth);
        applyLevers(jsonContent, labyrinth); // Carrega as alavancas

        return labyrinth;
    }

    private static String readJsonFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler ficheiro mapa: " + e.getMessage());
            return "";
        }
        return content.toString();
    }

    private static int[][] parseGridData(String jsonContent) {
        int startIndex = jsonContent.indexOf("[[");
        int endIndex = jsonContent.lastIndexOf("]]");

        if (startIndex == -1 || endIndex == -1) {
            System.err.println("Aviso: Matriz 'grid' não encontrada no JSON.");
            return new int[0][0];
        }

        String rawMatrix = jsonContent.substring(startIndex + 2, endIndex);
        String[] rowsRaw = rawMatrix.split("\\],\\s*\\[");

        int rows = rowsRaw.length;
        int cols = rowsRaw[0].split(",").length;
        int[][] grid = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            String[] colsRaw = rowsRaw[i].split(",");
            for (int j = 0; j < colsRaw.length; j++) {
                try {
                    grid[i][j] = Integer.parseInt(colsRaw[j].trim());
                } catch (NumberFormatException e) {
                    grid[i][j] = 0;
                }
            }
        }
        System.out.println("Grelha carregada: " + rows + "x" + cols);
        return grid;
    }

    private static void applyLocks(String jsonContent, Labyrinth labyrinth) {
        int keyIndex = jsonContent.indexOf("\"locked\"");
        if (keyIndex == -1) return;

        int startArr = jsonContent.indexOf("[", keyIndex);
        int endArr = jsonContent.indexOf("]", startArr);

        if (startArr == -1 || endArr == -1) return;

        String lockedContent = jsonContent.substring(startArr + 1, endArr);
        if (lockedContent.trim().isEmpty()) return;

        String[] locks = lockedContent.split("},");

        int count = 0;
        for (String lockJson : locks) {
            String roomA = extractValue(lockJson, "roomA");
            String roomB = extractValue(lockJson, "roomB");

            if (roomA != null && roomB != null) {
                Corridor c = labyrinth.getCorridor(roomA, roomB);
                if (c != null) {
                    c.setLocked(true);
                    count++;
                }
            }
        }
        System.out.println("Portas trancadas carregadas: " + count);
    }

    private static void applyLevers(String jsonContent, Labyrinth labyrinth) {
        // CORREÇÃO: Procura por "levers" (minúsculas)
        int keyIndex = jsonContent.indexOf("\"levers\"");
        if (keyIndex == -1) return;

        int startArr = jsonContent.indexOf("[", keyIndex);
        int endArr = jsonContent.indexOf("]", startArr);
        if (startArr == -1 || endArr == -1) return;

        String content = jsonContent.substring(startArr + 1, endArr);
        if (content.trim().isEmpty()) return;

        // CORREÇÃO: Split por "}," para separar objetos
        String[] items = content.split("},");

        int count = 0;
        for (String item : items) {
            String roomId = extractValue(item, "roomId");
            String id = extractValue(item, "id");
            // CORREÇÃO: Usa as chaves do teu mapa.json (doorRoomA/doorRoomB)
            String doorA = extractValue(item, "doorRoomA");
            String doorB = extractValue(item, "doorRoomB");

            if (roomId != null && doorA != null && doorB != null) {
                Lever lever = new Lever(id != null ? id : "L" + count, doorA, doorB);
                labyrinth.addLever(roomId, lever);
                count++;
            }
        }
        System.out.println("Alavancas carregadas: " + count);
    }

    private static String extractValue(String source, String key) {
        String searchKey = "\"" + key + "\"";
        int keyPos = source.indexOf(searchKey);
        if (keyPos == -1) return null;

        int startQuote = source.indexOf("\"", keyPos + searchKey.length() + 1);
        int endQuote = source.indexOf("\"", startQuote + 1);

        if (startQuote != -1 && endQuote != -1) {
            return source.substring(startQuote + 1, endQuote);
        }
        return null;
    }

    private static Labyrinth createLabyrinthFromGrid(int[][] gridData) {
        Labyrinth labyrinth = new Labyrinth();
        int rows = gridData.length;
        if (rows == 0) return labyrinth;
        int cols = gridData[0].length;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int code = gridData[y][x];
                if (code != EMPTY) {
                    Room room = createRoom(x, y, code);
                    labyrinth.addRoom(room);

                    if (code == START) {
                        labyrinth.addEntryPoint(room.getId());
                        labyrinth.setStartRoom(room.getId());
                    }
                    if (code == EXIT) labyrinth.setTreasureRoom(room.getId());
                }
            }
        }

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (labyrinth.getRoomAt(x, y) == null) continue;
                String currentId = x + "-" + y;

                if (y > 0) {
                    String upId = x + "-" + (y - 1);
                    if (labyrinth.getRoom(upId) != null) labyrinth.addCorridor(currentId, upId);
                }
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
            case LEVER:   type = RoomType.LEVER; label = "L"; break;
            default:      type = RoomType.NORMAL; label = ""; break;
        }
        return new Room(id, type, label);
    }
}