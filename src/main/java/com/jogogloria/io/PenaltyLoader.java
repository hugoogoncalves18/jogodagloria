package com.jogogloria.io;

import com.jogogloria.model.Penalty;
import com.jogogloria.model.Penalty.PenaltyType;
import com.example.Biblioteca.queues.CircularArrayQueue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PenaltyLoader {

    public static CircularArrayQueue<Penalty> loadPenalties(String jsonFilePath) {
        CircularArrayQueue<Penalty> queue = new CircularArrayQueue<>();

        // Ler ficheiro
        String jsonContent = readJsonFile(jsonFilePath);
        if (jsonContent.isEmpty()) return queue;


        // Remove parênteses retos do array
        String cleanContent = jsonContent.replace("[", "").replace("]", "");

        // Separa por "}," para obter cada objeto individualmente
        String[] items = cleanContent.split("},");

        for (String item : items) {
            String description = extractValue(item, "description");
            String typeStr = extractValue(item, "type");
            String valueStr = extractValue(item, "value");

            if (description != null && typeStr != null && valueStr != null) {
                try {
                    // Converter String para Enum e Int
                    PenaltyType type = PenaltyType.valueOf(typeStr);
                    int value = Integer.parseInt(valueStr);

                    queue.enqueue(new Penalty(description, type, value));
                } catch (Exception e) {
                    System.err.println("Erro ao processar penalidade: " + item + " -> " + e.getMessage());
                }
            }
        }

        System.out.println("Penalidades carregadas: " + queue.size());
        return queue;
    }

    private static String readJsonFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) content.append(line.trim());
        } catch (IOException e) {
            System.err.println("Erro ao ler penalties.json: " + e.getMessage());
            return "";
        }
        return content.toString();
    }

    private static String extractValue(String source, String key) {
        String searchKey = "\"" + key + "\":";
        int start = source.indexOf(searchKey);
        if (start == -1) return null;

        start += searchKey.length();

        // Verifica se o valor é string (tem aspas) ou número (não tem aspas)
        // Se começar com aspas:
        int firstQuote = source.indexOf("\"", start);
        if (firstQuote != -1 && firstQuote < start + 2) { // É String
            int secondQuote = source.indexOf("\"", firstQuote + 1);
            if (secondQuote != -1) {
                return source.substring(firstQuote + 1, secondQuote);
            }
        } else {
            // É Número (lê até à vírgula ou fim da chaveta)
            int endComma = source.indexOf(",", start);
            int endBrace = source.indexOf("}", start);

            // Pega o que aparecer primeiro (vírgula ou fecho)
            int end = -1;
            if (endComma == -1) end = endBrace;
            else if (endBrace == -1) end = endComma;
            else end = Math.min(endComma, endBrace);

            if (end != -1) {
                return source.substring(start, end).trim();
            }
        }
        return null;
    }
}