package com.jogogloria.io;

import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.iterators.Iterator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Gestor de Mapas (I/O), calsse responsável por interagir com o sistema de ficheiros
 * para listar mapas, gravar novos mapas em JSON
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class MapManager {

    /** Nome da diretoria onde os ficheiros do mapa são guardados*/
    private static final String MAPS_FOLDER = "maps";

    /**
     * Lista todos os ficheiros do mapa disponiveis na pasta do jogo
     * @return Lista não ordenada contendo o nome dos ficheiros
     */
    public static ArrayUnorderedList<String> listMaps() {
        ArrayUnorderedList<String> results = new ArrayUnorderedList<>();
        File folder = new File(MAPS_FOLDER);
        if (!folder.exists())
            folder.mkdir();

        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && f.getName().endsWith(".json")) {
                    results.addToRear(f.getName());
                }
            }
        }
        return results;
    }

    /**
     * Guarda um objeto de dados de mapa num ficheiro JSON
     * @param map O objeto contendo os dados do mapa a gravar
     * @param filename Nome do ficheiro de destino
     */
   public static void saveMap(MapData map, String filename) {
        if (!filename.endsWith(".json"))
            filename += ".json";

        File folder = new File(MAPS_FOLDER);
        if (!folder.exists())
            folder.mkdir();

        File file = new File(folder, filename);
        StringBuilder json = new StringBuilder();

        json.append("{\n");
        json.append("  \"name\": \"").append(map.name).append("\",\n");

        json.append(" \"grid\": [\n");
        for (int i = 0; i < map.grid.length; i++) {
            json.append("    [");
            for (int j = 0; j < map.grid[i].length; j++) {
                json.append(map.grid[i][j]);
                if ( j < map.grid[i].length -1)
                    json.append(", ");
            }
            json.append("]");
            if (i < map.grid.length - 1)
                json.append(",");
            json.append("\n");
        }
        json.append("  ],\n");

        //Portas trancadas
       json.append("  \"locked\": [\n");
       Iterator<MapData.LockData> itLock = map.locked.iterator();
       while(itLock.hasNext()) {
           MapData.LockData l = itLock.next();
           json.append("    { \"roomA\": \"").append(l.roomA).append("\", \"roomB\": \"").append(l.roomB).append("\" }");
           if (itLock.hasNext()) json.append(",");
           json.append("\n");
       }
       json.append("  ],\n");

       //alavancas
       json.append("  \"levers\": [\n");
       Iterator<MapData.LeverData> itLever = map.levers.iterator();
       while(itLever.hasNext()) {
           MapData.LeverData l = itLever.next();
           json.append("    {\n");
           json.append("      \"roomId\": \"").append(l.roomId).append("\",\n");
           json.append("      \"id\": \"").append(l.id).append("\",\n");
           json.append("      \"doorRoomA\": \"").append(l.doorRoomA).append("\",\n");
           json.append("      \"doorRoomB\": \"").append(l.doorRoomB).append("\"\n");
           json.append("    }");
           if (itLever.hasNext()) json.append(",");
           json.append("\n");
       }
       json.append("  ]\n");
       json.append("}");

       try (FileWriter writer = new FileWriter(file)) {
           writer.write(json.toString());
           System.out.println("Mapa gravado: " + file.getAbsolutePath());
       } catch (IOException e) {
           System.err.println("Erro ao gravar: " + e.getMessage());
       }
   }
}

