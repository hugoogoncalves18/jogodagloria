package com.jogogloria.io;
import com.example.Biblioteca.lists.ArrayUnorderedList;

/**
 * Estrutura de dados intermédia utilizada para representar um mapa em memória
 * É utilizada pelo editor de mapas
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class MapData {
    public String name;
    public int[][] grid;
    public ArrayUnorderedList<LockData> locked;
    public ArrayUnorderedList<LeverData> levers;

    /**
     * Cria uma nova estrutura de dados para um mapa vazio
     * @param name Nome do Mapa
     * @param width Largura da grelha
     * @param height Altura da grelha
     */
    public MapData(String name, int width, int height) {
        this.name = name;
        this.grid = new int[height][width];
        this.locked = new ArrayUnorderedList<>();
        this.levers = new ArrayUnorderedList<>();

        for (int i = 0; i < height; i++) {
            for (int j = 0 ; j < width; j++) {
                grid[i][j] = 0;
            }
        }
    }

    /**
     * Classe auxiliar para armazenar dados de uma porta
     */
    public static class LockData implements Comparable<LockData> {
        public String roomA;
        public String roomB;

        public LockData(String a, String b) {
            roomA = a;
            roomB = b;
        }

        @Override
        public int compareTo(LockData o) {
            return 0;
        }

        @Override
        public String toString() {
            return roomA + " - " + roomB;
        }
    }

    /**
     * Classe auxiliar para armazenar dados de configuração de uma alavanca
     */
    public static class LeverData implements  Comparable<LeverData> {
            public String roomId, id, doorRoomA, doorRoomB;
            public LeverData(String r, String i, String da, String db) {
                roomId = r;
                id = i;
                doorRoomA = da;
                doorRoomB = db;
            }
            @Override
            public int compareTo(LeverData o) {
                return 0;
            }

            @Override
            public String toString() {
                return "Alavanca " + id;
            }
        }
}

