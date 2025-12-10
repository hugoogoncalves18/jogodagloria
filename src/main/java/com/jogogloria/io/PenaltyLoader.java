package com.jogogloria.io;

import com.jogogloria.model.Penalty;
import com.jogogloria.model.Penalty.PenaltyType;
import com.example.Biblioteca.lists.ArrayUnorderedList; // <--- Import da Lista

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Responsável por carregar as configs de Penalidades a partir de um JSON
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class PenaltyLoader {

    /**
     * Carrega a lista de penalidades a partir de um caminho
     * @param jsonFilePath Caminho para o ficheiro .json
     * @return Uma {@link ArrayUnorderedList} contendo todas as penalidades carregadas
     */
    public static ArrayUnorderedList<Penalty> loadPenalties(String jsonFilePath) {
        ArrayUnorderedList<Penalty> list = new ArrayUnorderedList<>();

        String jsonContent = readJsonFile(jsonFilePath);
        if (jsonContent.isEmpty()) {
            System.out.println("Aviso: Penalties vazios.");
            return list;
        }

        // Limpeza básica
        String cleanContent = jsonContent.replace("[", "").replace("]", "");
        String[] items = cleanContent.split("},");

        for (String item : items) {
            // Garante fecho do objeto
            if (!item.trim().endsWith("}")) item = item + "}";

            String description = extractValue(item, "description");
            String typeStr = extractValue(item, "type");
            String valueStr = extractValue(item, "value");

            if (description != null && typeStr != null && valueStr != null) {
                try {
                    PenaltyType type = PenaltyType.valueOf(typeStr);
                    int value = Integer.parseInt(valueStr);
                    // Adiciona à lista
                    list.addToRear(new Penalty(description, type, value));
                } catch (Exception e) {
                    System.err.println("Erro penalty: " + e.getMessage());
                }
            }
        }
        System.out.println("Penalidades carregadas: " + list.size());
        return list;
    }


    /**
     * Lê o contúdo de um ficheiro de texto
     * @param filePath Caminho de ficheiro
     * @return Contúdo como String única
     */
    private static String readJsonFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) content.append(line.trim());
        } catch (IOException e) {
            return "";
        }
        return content.toString();
    }

    /**
     * Extrai o valor associado a uma chave JSON numa String
     * @param source Fragmento de JSON contendo o objeto
     * @param key A chave a pesquisae
     * @return O valor extraido
     */
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
            int end = (comma == -1) ? brace : (brace == -1 ? comma : Math.min(comma, brace));
            if (end != -1) return source.substring(start, end).trim();
        }
        return null;
    }
}