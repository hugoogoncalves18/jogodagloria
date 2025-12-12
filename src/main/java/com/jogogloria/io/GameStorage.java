package com.jogogloria.io;

import com.jogogloria.engine.GameEngine;
import com.jogogloria.engine.ShortestPathBot;
import com.jogogloria.engine.BotDifficulty;
import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Player;
import com.jogogloria.model.Room;
import com.jogogloria.model.Lever;
import com.example.Biblioteca.iterators.Iterator;
import com.example.Biblioteca.lists.ArrayUnorderedList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Classe que guarda o estado do Jogo em formato JSON
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class GameStorage {

    /**
     * Guarda o estado atual do jogo num ficheiro JSON.
     */
    public static void saveGame(GameEngine engine, String filepath) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");

        // 1. Jogador Atual
        Player current = engine.getCurrentPlayer();
        json.append("  \"currentPlayer\": \"").append(current.getId()).append("\",\n");

        // 2. Alavancas Ativadas (Percorrer salas para encontrar Levers ativas)
        json.append("  \"activatedLevers\": [");
        Iterator<Room> roomIt = engine.getLabyrinth().getRoomsIterator();
        boolean firstLev = true;
        while(roomIt.hasNext()) {
            Room r = roomIt.next();
            if(r.hasLever() && r.getLever().isActivated()) {
                if(!firstLev) json.append(", ");
                json.append("\"").append(r.getLever().getId()).append("\"");
                firstLev = false;
            }
        }
        json.append("],\n");

        // 3. Jogadores (Posições, Pontos, etc)
        json.append("  \"players\": [\n");
        Iterator<Player> pIt = engine.getAllPlayersIterator(); // Adicionar getter no Engine
        boolean firstP = true;

        while(pIt.hasNext()) {
            Player p = pIt.next();
            if(!firstP) json.append(",\n");

            json.append("    { ");
            json.append("\"id\": \"").append(p.getId()).append("\", ");
            json.append("\"name\": \"").append(p.getName()).append("\", ");
            json.append("\"roomId\": \"").append(p.getCurrentRoom().getId()).append("\", ");
            json.append("\"moves\": ").append(p.getMovementPoints()).append(", ");
            json.append("\"skips\": ").append(p.getSkipTurns()).append(", ");
            json.append("\"boosts\": ").append(p.getBoost()).append(", ");
            json.append("\"isBot\": ").append(p.isBot());
            json.append(" }");
            firstP = false;
        }
        json.append("\n  ]\n");
        json.append("}");

        // Escrever ficheiro
        try (FileWriter writer = new FileWriter(filepath)) {
            writer.write(json.toString());
            System.out.println("Jogo guardado em: " + filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Carrega o jogo a partir de um ficheiro JSON.
     *
     * @param filepath Caminho do ficheiro (ex: "savegame.json")
     * @param mapPath  Caminho do mapa original (necessário para recriar o labirinto base)
     * @return Um GameEngine totalmente configurado e pronto a jogar.
     */
    public static GameEngine loadGame(String filepath, String mapPath) throws Exception {
        // 1. Ler o conteúdo do ficheiro
        String json = readJsonFile(filepath);
        if (json.isEmpty()) throw new Exception("Ficheiro de save vazio ou inexistente.");

        // 2. Carregar o Labirinto Base (Estrutura)
        Labyrinth labyrinth = MapLoader.loadLabyrinth(mapPath);
        GameEngine engine = new GameEngine(labyrinth);

        // 3. Extrair Jogadores
        ArrayUnorderedList<Player> loadedPlayers = parsePlayers(json, labyrinth);

        // Adicionar ao Engine
        Iterator<Player> it = loadedPlayers.iterator();
        while (it.hasNext()) {
            engine.addPlayer(it.next());
        }

        // 4. Restaurar Jogador Atual (Rodar a Queue)
        String currentPlayerId = extractValue(json, "currentPlayer");
        if (currentPlayerId != null) {
            restoreTurnQueue(engine, currentPlayerId);
        }

        // 5. Restaurar Alavancas (Ativar e Destrancar Grafo)
        String leversBlock = extractArrayBlock(json, "activatedLevers");
        if (leversBlock != null && !leversBlock.isEmpty()) {
            String[] ids = leversBlock.replace("\"", "").split(",");
            for (String id : ids) {
                activateLeverById(engine, labyrinth, id.trim());
            }
        }

        return engine;
    }

    private static ArrayUnorderedList<Player> parsePlayers(String json, Labyrinth lab) {
        ArrayUnorderedList<Player> list = new ArrayUnorderedList<>();

        // Encontrar o bloco "players": [ ... ]
        String playersBlock = extractArrayBlock(json, "players");
        if (playersBlock == null) return list;

        // Separar objetos por "}, {"
        String[] playerObjs = playersBlock.split("\\},\\s*\\{");

        for (String obj : playerObjs) {
            // Limpar chavetas extra
            obj = obj.replace("{", "").replace("}", "");

            String id = extractValue(obj, "id");
            String name = extractValue(obj, "name");
            String roomId = extractValue(obj, "roomId");
            int moves = Integer.parseInt(extractValue(obj, "moves"));
            int skips = Integer.parseInt(extractValue(obj, "skips"));
            int boosts = Integer.parseInt(extractValue(obj, "boosts"));
            boolean isBot = Boolean.parseBoolean(extractValue(obj, "isBot"));

            // Recriar Jogador
            Player p = new Player(id, name);
            if (isBot) {
                // Definimos uma estratégia padrão ao carregar
                p.setBotStrategy(new ShortestPathBot(BotDifficulty.MEDIUM));
            }

            // Restaurar Estado
            Room r = lab.getRoom(roomId);
            if (r != null) {
                p.move(r);
                p.setInitialPosition(r); // Assume-se que onde ele está é o "spawn" do load
            }
            p.setMovementPoints(moves);
            p.setSkipTurns(skips);
            // p.setBoost(boosts); // Se tiveres setter

            list.addToRear(p);
        }
        return list;
    }

    private static void restoreTurnQueue(GameEngine engine, String currentId) {
        // Roda a fila até o jogador atual ser o primeiro
        Player first = engine.getCurrentPlayer();
        if (first == null) return;

        // Proteção contra loops infinitos (máx players * 2)
        int maxTries = 20;
        while (!first.getId().equals(currentId) && maxTries > 0) {
            engine.nextTurn(); // Passa a vez (mas sem resetar pontos, idealmente)
            first = engine.getCurrentPlayer();
            maxTries--;
        }
    }

    private static void activateLeverById(GameEngine engine, Labyrinth lab, String leverId) {
        Iterator<Room> it = lab.getRoomsIterator();
        while (it.hasNext()) {
            Room r = it.next();
            if (r.hasLever()) {
                Lever l = r.getLever();
                if (l.getId().equals(leverId)) {
                    // Ativar lógica
                    l.setActivated(true);
                    lab.setConnectionLocked(l.getRoomA().getId(), l.getRoomB().getId(), false);
                    return;
                }
            }
        }
    }

    // Leitura básica de ficheiro
    private static String readJsonFile(String filepath) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        } catch (IOException e) { return ""; }
        return sb.toString();
    }

    // Extrai valor de chave simples "key": "value"
    private static String extractValue(String source, String key) {
        String search = "\"" + key + "\":";
        int start = source.indexOf(search);
        if (start == -1) return null;

        start += search.length();

        // Verifica se é String (aspas) ou número/boolean
        boolean isString = source.charAt(source.indexOf("\"", start - 1) + 1) == '\"'; // Simplificação
        // Procura próxima aspas ou vírgula
        // (Nota: Parser simplificado para o trabalho, pode falhar com JSONs complexos aninhados)
        int end;
        if (source.trim().startsWith("\"", start)) {
            start = source.indexOf("\"", start) + 1;
            end = source.indexOf("\"", start);
        } else {
            int comma = source.indexOf(",", start);
            int brace = source.indexOf("}", start);
            if (comma == -1) end = brace;
            else if (brace == -1) end = comma;
            else end = Math.min(comma, brace);
        }

        if (start != -1 && end != -1) {
            return source.substring(start, end).trim().replace("\"", "");
        }
        return "0"; // Default
    }

    // Extrai conteúdo dentro de [ ... ]
    private static String extractArrayBlock(String source, String key) {
        String search = "\"" + key + "\":";
        int start = source.indexOf(search);
        if (start == -1) return null;

        int openBracket = source.indexOf("[", start);
        int closeBracket = source.lastIndexOf("]"); // Simplificado
        // Idealmente procuraria o fecho correspondente
        if (openBracket != -1) {
            // Procura o ] mais próximo que fecha este array
            // Para simplificar, assumimos que o JSON é bem formado e não aninhado profundamente
            int i = openBracket;
            int counter = 1;
            while (counter > 0 && ++i < source.length()) {
                if (source.charAt(i) == '[') counter++;
                if (source.charAt(i) == ']') counter--;
            }
            return source.substring(openBracket + 1, i);
        }
        return null;
    }
}