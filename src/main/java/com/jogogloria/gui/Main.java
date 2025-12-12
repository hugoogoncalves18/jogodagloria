package com.jogogloria.gui;

import com.jogogloria.engine.*;
import com.jogogloria.io.MapLoader;
import com.jogogloria.model.Labyrinth;
import com.jogogloria.model.Player;

import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.iterators.Iterator;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;


/**
 * Ponto de entrada principal da aplicação Jogo da Glória.
 *
 * @author Hugo Gonçalves
 * @version 2.0
 */
public class Main {

    /**
     * Método a ser executado ao iniciar o programa
     *
     * @param args Argumentos da linha de comandos
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainMenu().setVisible(true);
        });
    }

    /**
     * Inicia uma sessão de jogo real.
     *
     * @param numHumans   Número de jogadores humanos selecionados
     * @param numBots     Número de bots selecionados
     * @param mapFilePath Caminho relativo para o ficheiro JSON do mapa
     * @param difficulty  Nível de dificuldade para os bots inteligentes
     */
    public static void launchGame(int numHumans, int numBots, String mapFilePath, BotDifficulty difficulty, boolean fogEnabled) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("A carregar mapa... " + mapFilePath);
                Labyrinth labyrinth = MapLoader.loadLabyrinth(mapFilePath);

                GameEngine engine = new GameEngine(labyrinth, fogEnabled);
                ArrayUnorderedList<Player> allPlayers = new ArrayUnorderedList<>();

                // 1. Criar Humanos
                for (int i = 1; i <= numHumans; i++) {
                    Player p = new Player("p" + i, "Jogador " + i);
                    engine.addPlayer(p);
                    allPlayers.addToRear(p);
                }

                // 2. Criar Bots com Estratégias Variadas
                for (int i = 1; i <= numBots; i++) {
                    String id = "b" + i;
                    String name;
                    BotStrategy strategy;

                    // Lógica de Distribuição de Personalidades
                    if (i == 1) {
                        strategy = new CowardBot();
                        name = "Bot Medroso";
                    }
                    else if (i == 2) {
                        strategy = new ExplorerBot();
                        name = "Bot Explorador";
                    }
                    else {
                        strategy = new ShortestPathBot(difficulty);
                        name = "Bot Inteligente (" + difficulty + ")";
                    }

                    Player bot = new Player(id, name, strategy);
                    engine.addPlayer(bot);
                    allPlayers.addToRear(bot);
                }

                // Inicia a Janela de Jogo
                GameWindow window = new GameWindow(labyrinth, engine, allPlayers, 20, 20);
                window.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Erro ao iniciar jogo: " + e.getMessage());
            }
        });
    }

    /**
     * Inicia o jogo a partir de um Engine carregado (Load Game).
     */
    public static void launchLoadedGame(GameEngine engine) {
        SwingUtilities.invokeLater(() -> {
            try {
                ArrayUnorderedList<Player> allPlayers = new ArrayUnorderedList<>();
                Iterator<Player> it = engine.getAllPlayersIterator();
                while(it.hasNext()) {
                    allPlayers.addToRear(it.next());
                }

                // Acesso ao Labirinto (Adiciona getLabyrinth() no Engine se não tiveres)
                Labyrinth labyrinth = engine.getLabyrinth();

                GameWindow window = new GameWindow(labyrinth, engine, allPlayers, 20, 20);
                window.setVisible(true);

                System.out.println("Jogo carregado com sucesso!");

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Erro ao iniciar jogo carregado: " + e.getMessage());
            }
        });
    }
}