package com.jogogloria.gui;

import com.jogogloria.config.GameConfig;
import com.jogogloria.engine.BotDifficulty;
import com.jogogloria.engine.GameEngine;
import com.jogogloria.engine.MapEditor;
import com.jogogloria.io.MapManager;
import com.jogogloria.io.GameStorage;
import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.iterators.Iterator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Janela do menu principal do jogo.
 *
 * @author Hugo Gonçalves
 * @version 3.0
 */
public class MainMenu extends JFrame {

    private BotDifficulty selectedDifficulty = BotDifficulty.MEDIUM;
    private JCheckBox chkFog;
    private Image backgroundImage;
    private Image iconImage;

    /**
     * Construtor do menu principal
     */
    public MainMenu() {
        setTitle("Jogo da Glória - Menu Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 650);
        setLocationRelativeTo(null);
        setResizable(false);

        // Carrega imagens do disco
        loadImages();

        // Define o Favicon
        if (iconImage != null) {
            setIconImage(iconImage);
        }

        // Define o painel de fundo personalizado
        BackgroundPanel mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        // Título (No Topo)
        JLabel titleLabel = new JLabel("JOGO DA GLÓRIA", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 36));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setBorder(new EmptyBorder(40, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Painel Central para os Botões
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new GridLayout(6, 1, 10, 15));
        panel.setBorder(new EmptyBorder(0, 80, 40, 80));

        // Botão Single Player
        JButton btnSingle = createStyledButton("Single Player (vs Bots)");
        btnSingle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setupSinglePlayer();
            }
        });
        panel.add(btnSingle);

        // Botão Multiplayer
        JButton btnMulti = createStyledButton("Multiplayer");
        btnMulti.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setupMultiPlayer();
            }
        });
        panel.add(btnMulti);

        // Botão Carregar Jogo
        JButton btnLoad = createStyledButton("Carregar Jogo");
        btnLoad.setBackground(new Color(240, 210, 80));
        btnLoad.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(180, 140, 20), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        btnLoad.addActionListener(e -> {
            loadSavedGame();
        });
        panel.add(btnLoad);

        // Botão de Definições (Dificuldade)
        JButton btnSettings = createStyledButton("⚙ Dificuldade Bots");
        btnSettings.addActionListener(e -> openSettingsDialog());
        panel.add(btnSettings);


        JPanel fogPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        fogPanel.setOpaque(false);

        chkFog = new JCheckBox("Ativar névoa");
        chkFog.setFont(new Font("SansSerif", Font.BOLD, 14));
        chkFog.setForeground(Color.WHITE);
        chkFog.setOpaque(false);
        chkFog.setFocusPainted(false);

        fogPanel.add(chkFog);
        panel.add(fogPanel);

        // Botão Editor de Mapas
        JButton btnEditor = createStyledButton("Editor de Mapas");
        btnEditor.addActionListener(e -> {
            this.dispose();

            JOptionPane.showMessageDialog(null,
                    "O Editor foi iniciado na CONSOLA.\nVerifica a janela do terminal.");

            new Thread(MapEditor::start).start();
        });
        panel.add(btnEditor);

        add(panel, BorderLayout.CENTER);
    }

    /**
     * Carrega as imagens necessárias (Fundo e Ícone).
     */
    private void loadImages() {
        try {
            backgroundImage = ImageIO.read(new File("resources/menu_background.png"));
            iconImage = ImageIO.read(new File("resources/game_icon.png"));
        } catch (IOException e) {
            System.err.println("Imagens de menu não encontradas (usando padrão).");
        }
    }

    /**
     * Cria um botão com estilo visual personalizado (RPG/Pedra).
     */
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Serif", Font.BOLD, 18));
        btn.setBackground(new Color(220, 220, 220));
        btn.setForeground(new Color(50, 50, 50));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(100, 100, 100), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return btn;
    }

    /**
     * Painel interno para desenhar a imagem de fundo.
     */
    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(new Color(50, 50, 60));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
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
                Main.launchGame(1, numBots, mapFile, selectedDifficulty, chkFog.isSelected());
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
                Main.launchGame(numHumans, 0, mapFile, selectedDifficulty, chkFog.isSelected());
            }
        }
    }

    /**
     * Exibe uma janela de diálogo para o utilizador escolher um dos mapas disponiveis
     */
    private String escolherMapa() {
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

        String escolha = (String) JOptionPane.showInputDialog(
                this,
                "Escolhe o campo de batalha:",
                "Seleção de Mapa",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcoesMapas,
                opcoesMapas[0]
        );

        if (escolha == null) return null;

        return "maps/" + escolha;
    }

    private void loadSavedGame() {
        try{
            String mapFile = escolherMapa();
            if (mapFile == null) return;

            this.dispose();
            GameEngine loadedEngine = GameStorage.loadGame("savegame.json", mapFile, chkFog.isSelected());

            Main.launchLoadedGame(loadedEngine);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar save: " + e.getMessage());
            new MainMenu().setVisible(true);
        }
    }
}