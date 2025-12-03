package com.jogogloria.engine;

import com.example.Biblioteca.exceptions.NoElementFoundException;
import com.jogogloria.model.Riddle;
import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.stacks.LinkedStack;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class RiddleManager {

    private ArrayUnorderedList<Riddle> availableRiddles;
    private LinkedStack<Riddle> usedRiddles; // A tua Stack de histórico

    public RiddleManager(String jsonFilePath) {
        this.availableRiddles = new ArrayUnorderedList<>();
        this.usedRiddles = new LinkedStack<>();
        loadRiddles(jsonFilePath);
    }

    /**
     * Sorteia um enigma, remove-o da lista de disponíveis e coloca na Stack de usados.
     * @return Um objeto Riddle ou null se acabarem.
     */
    public Riddle getRandomRiddle() throws NoElementFoundException {
        if (availableRiddles.isEmpty()) {
            return null; // Acabaram as perguntas!
        }

        // 1. Gerar índice aleatório
        int size = availableRiddles.size();
        int randomIndex = (int) (Math.random() * size);

        // 2. Obter a pergunta (usando o .get(i) da tua lista)
        Riddle r = availableRiddles.get(randomIndex);

        // 3. Mover para a Stack de usados (A tua ideia!)
        usedRiddles.push(r);

        // 4. Remover da lista original para não repetir
        availableRiddles.remove(r);

        return r;
    }

    // --- Parser Manual de JSON para Riddles ---
    private void loadRiddles(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) content.append(line);
        } catch (IOException e) {
            System.err.println("Erro ao ler riddles.json");
            return;
        }

        // Limpeza básica para facilitar o split
        String json = content.toString().replace("[", "").replace("]", "");

        // Divide pelos objetos (assumindo que terminam em "},")
        String[] parts = json.split("},");

        for (String part : parts) {
            String question = extractValue(part, "question");
            String answer = extractValue(part, "answer");

            if (question != null && answer != null) {
                availableRiddles.addToRear(new Riddle(question, answer));
            }
        }
    }

    // Helper simples para extrair valor de chave JSON manual: "key": "value"
    private String extractValue(String source, String key) {
        String searchKey = "\"" + key + "\":";
        int start = source.indexOf(searchKey);
        if (start == -1) return null;

        start += searchKey.length();
        int firstQuote = source.indexOf("\"", start);
        int secondQuote = source.indexOf("\"", firstQuote + 1);

        if (firstQuote != -1 && secondQuote != -1) {
            return source.substring(firstQuote + 1, secondQuote);
        }
        return null;
    }
}