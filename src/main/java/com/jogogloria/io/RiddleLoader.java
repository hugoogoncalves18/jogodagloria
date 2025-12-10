package com.jogogloria.io;

import com.jogogloria.model.Riddle;
import com.example.Biblioteca.lists.ArrayUnorderedList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Responsável por carrear e interpretar os dados dos enigmas
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class RiddleLoader {

    /**
     * Carrega a lista de enigmas
     * @param jsonFilePath Caminho relativo ou absoluto para o ficheiro .json
     * @return Uma lista não ordenada quem contém todos os enigmas
     */
    public static ArrayUnorderedList<Riddle> loadRiddles(String jsonFilePath) {
        ArrayUnorderedList<Riddle> riddles = new ArrayUnorderedList<>();

        String jsonContent = readJsonFile(jsonFilePath);
        if (jsonContent.isEmpty()) return riddles;

        int startArray = jsonContent.indexOf("[");
        int endArray = jsonContent.lastIndexOf("]");

        if (startArray == -1 || endArray == -1) {
            System.err.println("Formato JSON de riddles inválido: Array [] não encontrado.");
            return riddles;
        }

        String arrayContent = jsonContent.substring(startArray + 1, endArray);

        String[] items = arrayContent.split("},");

        for (String item : items) {
            if (!item.trim().endsWith("}")) item = item + "}";

            String id = extractValue(item, "id");
            String question = extractValue(item, "question");
            String correctAnswerIndexStr = extractValue(item, "correctAnswer");
            String bonusStr = extractValue(item, "bonus");
            String penaltyStr = extractValue(item, "penalty");

            ArrayUnorderedList<String> options = extractOptions(item);

            if (question != null && correctAnswerIndexStr != null && !options.isEmpty()) {
                try {
                    int index = Integer.parseInt(correctAnswerIndexStr);
                    String correctAnswerString = options.get(index - 1);

                    int bonus = (bonusStr != null) ? Integer.parseInt(bonusStr) : 1;
                    int penalty = (penaltyStr != null) ? Integer.parseInt(penaltyStr) : 1;
                    Riddle riddle = new Riddle(id, question, correctAnswerString, options, bonus, penalty);
                    riddles.addToRear(riddle);

                } catch (Exception e) {
                    System.err.println("Erro ao processar enigma (" + id + "): " + e.getMessage());
                }
            }
        }

        System.out.println("Enigmas carregados: " + riddles.size());
        return riddles;
    }

    //Parsing

    /**
     * Lê o conteúdo integral de um ficheiro de texto para String
     * @param filePath O caminho do ficheiro
     * @return Conteúdo do ficheiro ou string vazia
     */
    private static String readJsonFile(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) content.append(line.trim());
        } catch (IOException e) {
            System.err.println("Erro ao ler " + filePath + ": " + e.getMessage());
            return "";
        }
        return content.toString();
    }

    /**
     * Extrai o valor de uma chave específica num objeto JSON
     * @param source String que contém o objeto JSON
     * @param key A cave a procurar
     * @return Valor associado á chave
     */
    private static String extractValue(String source, String key) {
        String searchKey = "\"" + key + "\":";
        int start = source.indexOf(searchKey);
        if (start == -1) return null;

        start += searchKey.length();

        while (start < source.length() && Character.isWhitespace(source.charAt(start))) {
            start++;
        }

        if (start >= source.length()) return null;

        char firstChar = source.charAt(start);

        if (firstChar == '\"') {
            int endQuote = source.indexOf("\"", start + 1);
            if (endQuote != -1) {
                return source.substring(start + 1, endQuote);
            }
        } else {
            int end = start;
            while (end < source.length()) {
                char c = source.charAt(end);
                if (c == ',' || c == '}' || c == ']') {
                    break;
                }
                end++;
            }
            return source.substring(start, end).trim();
        }
        return null;
    }

    /**
     * Extrai especificamente o array de opções de um enigma
     * @param source A String de objetos JSON
     * @return Uma lista com as opções de resposta
     */
    private static ArrayUnorderedList<String> extractOptions(String source) {
        ArrayUnorderedList<String> list = new ArrayUnorderedList<>();
        int start = source.indexOf("\"options\":");
        if (start == -1) return list;

        int openBracket = source.indexOf("[", start);
        int closeBracket = source.indexOf("]", openBracket);

        if (openBracket != -1 && closeBracket != -1) {
            String arrayContent = source.substring(openBracket + 1, closeBracket);
            String[] rawOptions = arrayContent.split(",");
            for (String opt : rawOptions) {
                String clean = opt.replace("\"", "").trim();
                if (!clean.isEmpty()) list.addToRear(clean);
            }
        }
        return list;
    }
}