package com.jogogloria.gui;

import com.jogogloria.config.GameConfig;
import com.jogogloria.engine.BotDifficulty; // Importante
import com.jogogloria.io.MapManager;
import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.iterators.Iterator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Janela do menu principal do jogo.
 * <p>
 * ATUALIZADO:
 * - Inclui seletor de Dificuldade para os Bots.
 * </p>
 *
 * @author Hugo Gonçalves
 * @version 2.0
 */
public class MainMenu extends JFrame {

    // Guarda a dificuldade selecionada (Padrão: Médio)
    private BotDifficulty selectedDifficulty = BotDifficulty.MEDIUM;

    /**
     * Construtor do menu principal
     */
    public MainMenu() {
        setTitle("Jogo da Glória - Menu Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 420); // Aumentei a altura para caber o novo botão
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        JPanel panel = new JPanel();
        // Alterado para 5 linhas: Título + Single + Multi + Definições + Editor
        panel.setLayout(new GridLayout(5, 1, 10, 10));

        // 1. Título
        JLabel titleLabel = new JLabel("JOGO DA GLÓRIA", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel);

        // 2. Botão Single Player
        JButton btnSingle = new JButton("Single Player (vs Bots)");
        btnSingle.setFont(new Font("Arial", Font.PLAIN, 16));
        btnSingle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setupSinglePlayer();
            }
        });
        panel.add(btnSingle);

        // 3. Botão Multiplayer
        JButton btnMulti = new JButton("Multiplayer");
        btnMulti.setFont(new Font("Arial", Font.PLAIN, 16));
        btnMulti.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setupMultiPlayer();
            }
        });
        panel.add(btnMulti);

        // 4. Botão de Definições (Dificuldade) [NOVO]
        JButton btnSettings = new JButton("Definições / Dificuldade");
        btnSettings.setFont(new Font("Arial", Font.PLAIN, 16));
        // Ícone de engrenagem simples com texto
        btnSettings.setText("⚙Dificuldade Bots");
        btnSettings.addActionListener(e -> openSettingsDialog());
        panel.add(btnSettings);

        // 5. Botão Editor de Mapas
        JButton btnEditor = new JButton("Editor de Mapas");
        btnEditor.setFont(new Font("Arial", Font.PLAIN, 16));
        btnEditor.addActionListener(e -> {
            this.dispose(); // Fecha o menu

            JOptionPane.showMessageDialog(null,
                    "O Editor foi iniciado na CONSOLA.\nVerifica a janela do terminal.");

            // Arranca o editor numa nova thread
            new Thread(() -> {
                com.jogogloria.engine.MapEditor.start();
            }).start();
        });
        panel.add(btnEditor);

        add(panel);
    }

    /**
     * Abre janela para escolher a dificuldade dos Bots.
     */
    private void openSettingsDialog() {
        BotDifficulty[] possibilities = BotDifficulty.values();

        BotDifficulty choice = (BotDifficulty) JOptionPane.showInputDialog(
                this,
                "Escolha a inteligência artificial dos Bots:",
                "Definições",
                JOptionPane.QUESTION_MESSAGE,
                null,
                possibilities,
                selectedDifficulty);

        if (choice != null) {
            this.selectedDifficulty = choice;
            JOptionPane.showMessageDialog(this, "Dificuldade alterada para: " + choice);
        }
    }

    /**
     * Configura e inicia uma partida SinglePlayer
     */
    private void setupSinglePlayer() {
        String[] options = {"1 Bot", "2 Bots", "3 Bots"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Quantos Bots queres enfrentar?",
                "Configuração Single Player",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice != -1) {
            int numBots = choice + 1;

            // Pergunta qual o mapa
            String mapFile = escolherMapa();

            if (mapFile != null) {
                this.dispose();
                // Passa a dificuldade escolhida
                Main.launchGame(1, numBots, mapFile, selectedDifficulty);
            }
        }
    }

    /**
     * Configura e inicia uma partida Multiplayer
     */
    private void setupMultiPlayer() {
        String[] options = {"2 Jogadores", "3 Jogadores", "4 Jogadores"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Quantos amigos vão jogar?",
                "Configuração Multiplayer",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice != -1) {
            int numHumans = choice + 2;

            // Pergunta qual o mapa
            String mapFile = escolherMapa();

            if (mapFile != null) {
                this.dispose();
                Main.launchGame(numHumans, 0, mapFile, selectedDifficulty);
            }
        }
    }

    /**
     * Exibe uma janela de diálogo para o utilizador escolher um dos mapas disponiveis
     */
    private String escolherMapa() {
        // 1. Obter lista de mapas da pasta usando o MapManager
        ArrayUnorderedList<String> mapas = MapManager.listMaps();

        if (mapas.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nenhum mapa encontrado na pasta 'maps'!\nVou usar o mapa padrão.");
            return GameConfig.MAP_FILE;
        }

        String[] opcoesMapas = new String[mapas.size()];
        Iterator<String> it = mapas.iterator();
        int i = 0;
        while(it.hasNext()) {
            opcoesMapas[i++] = it.next();
        }

        // 3. Mostrar janela de escolha
        String escolha = (String) JOptionPane.showInputDialog(
                this,
                "Escolhe o campo de batalha:",
                "Seleção de Mapa",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcoesMapas,
                opcoesMapas[0]
        );

        // Se cancelar, retorna null
        if (escolha == null) return null;

        // Constrói o caminho completo (assumindo que MapManager lê da pasta 'maps')
        return "maps/" + escolha;
    }
}