package com.jogogloria.engine;
import com.jogogloria.io.MapData;
import com.jogogloria.io.MapManager;
import java.util.Scanner;

public class MapEditor {
    private static final Scanner scanner = new Scanner(System.in);

    public static void start() {
        System.out.println("\n====Editor de mapas====");
        System.out.print("Nome do ficheiro: ");
        String filename = scanner.nextLine();
        System.out.print("Nome do mapa:  ");
        String mapName = scanner.nextLine();
        System.out.print("Dimensão: ");
        int dim = Integer.parseInt(scanner.nextLine());

        MapData map = new MapData(mapName, dim, dim);
        editor(map, filename);
    }

    private static void editor(MapData map, String filename) {
        int cx = 0, cy = 0;
        boolean running = true;

        while (running) {
            System.out.println(map.name);
            for (int i = 0; i < map.grid.length; i++) {
                for ( int j = 0; j < map.grid[i].length; j++) {
                    String value = (i == cy && j == cx) ? "[" + map.grid[i][j] + "]" : " " + map.grid[i][j] + " ";
                    System.out.print(value);
                }
                System.out.println();
            }
            System.out.println("\n[WASD] Mover | [0-9] Set Tile | [L]ock | [K]ey/Lever | [G]ravar");
            System.out.print("> ");
            String input = scanner.nextLine().toUpperCase();
            if (input.isEmpty()) continue;
            char cmd = input.charAt(0);

            if (cmd == 'W' && cy > 0) cy--;
            else if (cmd == 'S' && cy < map.grid.length - 1) cy++;
            else if (cmd == 'A' && cx > 0) cx--;
            else if (cmd == 'D' && cx < map.grid[0].length - 1) cx++;

            else if (Character.isDigit(cmd)) {
                map.grid[cy][cx] = Character.getNumericValue(cmd);
            }
            else if (cmd == 'L') {
                System.out.print("Direção da porta (N/S/E/W): ");
                String dir = scanner.nextLine().toUpperCase();
                int tx = cx, ty = cy;
                if (dir.equals("N")) ty--; else if (dir.equals("S")) ty++;
                else if (dir.equals("E")) tx++; else if (dir.equals("W")) tx--;

                if(tx >= 0 && ty >= 0 && tx < map.grid.length && ty < map.grid.length) {
                    map.locked.addToRear(new MapData.LockData(cx + "-" + cy, tx + "-" + ty));
                    System.out.println("Porta trancada!");
                }
            }
            else if (cmd == 'K') {
                System.out.print("ID Alavanca: ");
                String id = scanner.nextLine();
                System.out.print("Porta A (x-y): ");
                String dA = scanner.nextLine();
                System.out.print("Porta B (x-y): ");
                String dB = scanner.nextLine();

                map.grid[cy][cx] = 6; // Tile da Alavanca
                map.levers.addToRear(new MapData.LeverData(cx + "-" + cy, id, dA, dB));
                System.out.println("Alavanca criada!");
            }
            else if (cmd == 'G') {
                MapManager.saveMap(map, filename);
                running = false;
            }
        }
    }
}