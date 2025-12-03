package com.jogogloria.gui;

import com.jogogloria.engine.GameEngine;
import com.jogogloria.io.MapLoader;
import com.jogogloria.engine.ShortestPathBot;
import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Player;
import com.jogogloria.gui.GameWindow;
import com.jogogloria.gui.MainMenu;
import com.example.Biblioteca.lists.ArrayUnorderedList;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        // A aplicação começa abrindo o Menu
        SwingUtilities.invokeLater(() -> {
            new MainMenu().setVisible(true);
        });
    }

    /**
     * Método chamado pelo Menu para arrancar o jogo real.
     * @param numHumans Quantidade de jogadores humanos
     * @param numBots Quantidade de bots
     */
    public static void launchGame(int numHumans, int numBots) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Carregar Mapa
                System.out.println("A carregar mapa...");
                Labyrinth labyrinth = MapLoader.loadLabyrinth("mapa.json");

                // 2. Inicializar Motor
                GameEngine engine = new GameEngine(labyrinth);
                ArrayUnorderedList<Player> allPlayers = new ArrayUnorderedList<>();

                // 3. Criar Humanos
                for (int i = 1; i <= numHumans; i++) {
                    String name = "Jogador " + i;
                    Player p = new Player("p" + i, name);

                    engine.addPlayer(p);
                    allPlayers.addToRear(p);
                }

                // 4. Criar Bots
                for (int i = 1; i <= numBots; i++) {
                    String name = "Bot " + i;
                    // ID dos bots começa depois dos humanos (ex: se 1 humano, bot é p2)
                    String id = "b" + i;

                    Player bot = new Player(id, name, new ShortestPathBot());

                    engine.addPlayer(bot);
                    allPlayers.addToRear(bot);
                }

                // 5. Configurar Janela de Jogo
                // Nota: Podes ajustar rows/cols se souberes o tamanho do mapa,
                // ou deixar fixo se o JSON for sempre o mesmo tamanho.
                GameWindow window = new GameWindow(labyrinth, engine, allPlayers, 15, 15);
                window.setVisible(true);

                System.out.println("Jogo iniciado: " + numHumans + " Humanos, " + numBots + " Bots.");

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Erro crítico ao iniciar o jogo: " + e.getMessage());
            }
        });
    }
}