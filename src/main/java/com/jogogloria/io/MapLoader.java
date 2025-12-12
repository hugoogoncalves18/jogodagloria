package com.jogogloria.io;

import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Lever;
import com.jogogloria.model.Room;
import com.jogogloria.model.Room.RoomType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Responsável pela construção do labirinto a partir de ficheiro de config JSON.
 *
 * @author Hugo Gonçalves
 * @version 3.0
 */
public class MapLoader {

    private static final int EMPTY = 0;
    private static final int START = 1;
    private static final int NORMAL = 2;
    private static final int RIDDLE = 3;
    private static final int PENALTY = 4;
    private static final int BOOST = 5;
    private static final int LEVER = 6;
    private static final int EXIT = 9;

    /**
     * Carrega um labirinto completo a partir de um ficheiro JSON
     */
    public static Labyrinth loadLabyrinth(String jsonFilePath) {
        String jsonContent = readJsonFile(jsonFilePath);
        if (jsonContent.isEmpty()) {
            System.err.println("Erro: Ficheiro JSON vazio.");
            return new Labyrinth();
        }

        int[][] grid = parseGridData(jsonContent);

        // 1. Cria Salas e Conexões (Estrutura do Grafo)
        Labyrinth labyrinth = createLabyrinthFromGrid(grid);

        // 2. Aplica Lógica (Trancas e Alavancas)
        applyLocks(jsonContent, labyrinth);
        applyLevers(jsonContent, labyrinth);

        return labyrinth;
    }

    private static String readJsonFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) content.append(line.trim());
        } catch (IOException e) {
            System.err.println("Erro ao ler mapa: " + e.getMessage());
            return "";
        }
        return content.toString();
    }

    private static int[][] parseGridData(String jsonContent) {
        int startIndex = jsonContent.indexOf("[[");
        int endIndex = jsonContent.lastIndexOf("]]");

        if (startIndex == -1 || endIndex == -1) return new int[0][0];

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

    /**
     * Aplica trancas às arestas do grafo.
     */
    private static void applyLocks(String jsonContent, Labyrinth labyrinth) {
        int keyIndex = jsonContent.indexOf("\"locked\"");
        if (keyIndex == -1) return;

        int startArr = jsonContent.indexOf("[", keyIndex);
        int endArr = jsonContent.indexOf("]", startArr);
        if (startArr == -1 || endArr == -1) return;

        String content = jsonContent.substring(startArr + 1, endArr);
        if (content.trim().isEmpty()) return;

        String[] locks = content.split("},");

        int count = 0;
        for (String item : locks) {
            String roomA = extractValue(item, "roomA");
            String roomB = extractValue(item, "roomB");

            if (roomA != null && roomB != null) {
                // [REFATORADO] Em vez de buscar um objeto Corridor,
                // altera o peso da aresta no grafo.
                labyrinth.setConnectionLocked(roomA, roomB, true);
                count++;
            }
        }
        System.out.println("Portas trancadas: " + count);
    }

    /**
     * Cria alavancas e associa-as às salas.
     */
    private static void applyLevers(String jsonContent, Labyrinth labyrinth) {
        int keyIndex = jsonContent.indexOf("\"levers\"");
        if (keyIndex == -1) return;

        int startArr = jsonContent.indexOf("[", keyIndex);
        int endArr = jsonContent.indexOf("]", startArr);
        if (startArr == -1 || endArr == -1) return;

        String content = jsonContent.substring(startArr + 1, endArr);
        if (content.trim().isEmpty()) return;

        String[] items = content.split("},");

        int count = 0;
        for (String item : items) {
            String roomId = extractValue(item, "roomId");
            String id = extractValue(item, "id");
            String doorAId = extractValue(item, "doorRoomA");
            String doorBId = extractValue(item, "doorRoomB");

            if (roomId != null && doorAId != null && doorBId != null) {
                Room room = labyrinth.getRoom(roomId);
                Room rA = labyrinth.getRoom(doorAId);
                Room rB = labyrinth.getRoom(doorBId);

                if (room != null && rA != null && rB != null) {
                    // 3. Criar a Lever com referências às Salas alvo
                    Lever lever = new Lever(id != null ? id : "L" + count, rA, rB);

                    // 4. Guardar a Lever dentro da Sala
                    room.setLever(lever);
                    count++;
                }
            }
        }
        System.out.println("Alavancas carregadas: " + count);
    }

    private static String extractValue(String source, String key) {
        String searchKey = "\"" + key + "\":";
        int start = source.indexOf(searchKey);
        if (start == -1) return null;

        start += searchKey.length();
        int firstQuote = source.indexOf("\"", start);
        if (firstQuote != -1 && firstQuote < start + 5) {
            int secondQuote = source.indexOf("\"", firstQuote + 1);
            if (secondQuote != -1) return source.substring(firstQuote + 1, secondQuote);
        } else {
            int comma = source.indexOf(",", start);
            int brace = source.indexOf("}", start);
            int end = -1;
            if (comma == -1) end = brace;
            else if (brace == -1) end = comma;
            else end = Math.min(comma, brace);

            if (end != -1) return source.substring(start, end).trim();
        }
        return null;
    }

    /**
     * Cria o grafo de conexões.
     */
    private static Labyrinth createLabyrinthFromGrid(int[][] gridData) {
        Labyrinth labyrinth = new Labyrinth();
        int rows = gridData.length;
        if (rows == 0) return labyrinth;
        int cols = gridData[0].length;

        // FASE 1: Criar todas as Salas (Vértices)
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

        // FASE 2: Criar Conexões (Arestas)
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Room current = labyrinth.getRoomAt(x, y);
                if (current == null) continue;

                // Liga com vizinho de CIMA
                if (y > 0) {
                    Room up = labyrinth.getRoomAt(x, y - 1);
                    if (up != null) {
                        // [REFATORADO] Adiciona conexão ao grafo
                        labyrinth.addConnection(current, up);
                    }
                }

                // Liga com vizinho da ESQUERDA
                if (x > 0) {
                    Room left = labyrinth.getRoomAt(x - 1, y);
                    if (left != null) {
                        // [REFATORADO] Adiciona conexão ao grafo
                        labyrinth.addConnection(current, left);
                    }
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
            case START:   type = RoomType.START; label = ""; break;
            case EXIT:    type = RoomType.EXIT; label = ""; break;
            case RIDDLE:  type = RoomType.RIDDLE; label = ""; break;
            case PENALTY: type = RoomType.PENALTY; label = ""; break;
            case BOOST:   type = RoomType.BOOST; label = ""; break;
            case LEVER:   type = RoomType.LEVER; label = ""; break;
            default:      type = RoomType.NORMAL; label = ""; break;
        }
        return new Room(id, type, label);
    }
}