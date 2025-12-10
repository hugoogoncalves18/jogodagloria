package com.jogogloria.io;

import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.jogogloria.model.Player;
import com.jogogloria.model.GameEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Responsável pelo geração de relatórios do jogo
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class History {

    /**
     * Gera um ficheiro JSON como resumo completo da partida
     * @param players A lista dos jogadores que participaram na partida
     * @param winner O nome do jogador que venceu
     */
    public static void generateDoc(ArrayUnorderedList<Player> players, String winner) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String filename = "History_" + timestamp + ".json";

        File directory = new File("gameHistory");
        if(!directory.exists()) {
            directory.mkdir();
        }
        File report = new File(directory, filename);

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"timestamp\": \"").append(LocalDateTime.now().toString()).append("\",\n");
        json.append("  \"vencedor\": \"").append(winner).append("\",\n");
        json.append("  \"jogadores\": [\n");

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);

            json.append("    {\n");
            json.append("      \"nome\": \"").append(p.getName()).append("\",\n");
            json.append("      \"tipo\": \"").append(p.isBot() ? "BOT" : "HUMAN").append("\",\n");
            json.append("      \"vitorias\": ").append(p.getWins()).append(",\n");
            json.append("      \"historico\": [\n");

            ArrayUnorderedList<GameEvent> logs = p.getLogs();
            for (int j = 0; j < logs.size(); j++) {
                GameEvent event = logs.get(j);
                json.append("        ").append(event.toJson());

                if (j < logs.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }

            json.append("      ]\n");
            json.append("    }");

            if (i < players.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]\n");
        json.append("}");

        try (FileWriter writer = new FileWriter(report)) {
            writer.write(json.toString());
            System.out.println("Histórico gerado: " + report.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Erro ao gravar histórico: " + e.getMessage());
        }
    }
}