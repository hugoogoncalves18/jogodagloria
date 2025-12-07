package com.jogogloria.gui;

import com.jogogloria.gui.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenu extends JFrame {

    public MainMenu() {
        setTitle("Jogo da Glória - Menu Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout()); // Centraliza tudo

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1, 10, 10)); // 3 Linhas, espaçamento de 10px

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

        add(panel);
    }

    private void setupSinglePlayer() {
        // Abre janela para escolher número de bots (1 a 3)
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
            int numBots = choice + 1; // choice 0 = 1 bot, etc.
            this.dispose(); // Fecha o Menu
            // Inicia o jogo: 1 Humano, N Bots
            Main.launchGame(1, numBots);
        }
    }

    private void setupMultiPlayer() {
        // Abre janela para escolher número de jogadores (2 a 4)
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
            int numHumans = choice + 2; // choice 0 = 2 jogadores, etc.
            this.dispose(); // Fecha o Menu
            // Inicia o jogo: N Humanos, 0 Bots
            Main.launchGame(numHumans, 0);
        }
    }
}