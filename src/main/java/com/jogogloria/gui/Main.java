package com.jogogloria.gui;

import com.jogogloria.config.GameConfig;
import com.jogogloria.engine.GameEngine;
import com.jogogloria.io.MapLoader;
import com.jogogloria.engine.ShortestPathBot;
import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Player;
import com.jogogloria.gui.GameWindow;
import com.jogogloria.gui.MainMenu;
import com.example.Biblioteca.lists.ArrayUnorderedList;

import javax.swing.SwingUtilities;

/**
 * Ponto de entrada principal da aplicação Jogo da Glória
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class Main {

    /**
     * Método a ser executado ao iniciar o programa
     * @param args Argumentos da linha de comandos
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainMenu().setVisible(true);
        });
    }

    /**
     * Inicia uma sessão de jogo real
     *
     * @param numHumans Número de jogadores humanos selecionados
     * @param numBots Número de bots selecionados
     * @param mapFilePath Caminho relativo para o ficheiro JSON do mapa a carregar
     */
    public static void launchGame(int numHumans, int numBots, String mapFilePath) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("A carregar mapa..." + mapFilePath);
                Labyrinth labyrinth = MapLoader.loadLabyrinth(mapFilePath);

                GameEngine engine = new GameEngine(labyrinth);
                ArrayUnorderedList<Player> allPlayers = new ArrayUnorderedList<>();

                for (int i = 1; i <= numHumans; i++) {
                    String name = "Jogador " + i;
                    Player p = new Player("p" + i, name);

                    engine.addPlayer(p);
                    allPlayers.addToRear(p);
                }

                for (int i = 1; i <= numBots; i++) {
                    String name = "Bot " + i;
                    String id = "b" + i;

                    Player bot = new Player(id, name, new ShortestPathBot());

                    engine.addPlayer(bot);
                    allPlayers.addToRear(bot);
                }

                GameWindow window = new GameWindow(labyrinth, engine, allPlayers, 20, 20);
                window.setVisible(true);

                System.out.println("Jogo iniciado: " + numHumans + " Humanos, " + numBots + " Bots." + "\nMapa selecionado: " + mapFilePath);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Erro crítico ao iniciar o jogo: " + e.getMessage());
                javax.swing.JOptionPane.showMessageDialog(null, "Erro ao carregar o mapa:\n" + e.getMessage());
            }
        });
    }
}