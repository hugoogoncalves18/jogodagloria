package com.jogogloria.gui;

import com.jogogloria.config.GameConfig;
import com.jogogloria.utils.SimpleMap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageManager {

    private SimpleMap<String, BufferedImage> images;

    public ImageManager() {
        this.images = new SimpleMap<>();
        loadImages();
    }

    private void loadImages() {
        // Carregar as texturas para o mapa (Cache)
        loadImage("NORMAL", GameConfig.IMG_FLOOR);
        loadImage("EXIT", GameConfig.IMG_EXIT);
        loadImage("RIDDLE", GameConfig.IMG_RIDDLE);
        loadImage("LEVER", GameConfig.IMG_LEVER);
        loadImage("START", GameConfig.IMG_START);
        loadImage("PENALTY", GameConfig.IMG_PENALTY);
        loadImage("BOOST", GameConfig.IMG_BOOST);
        loadImage("WALL", GameConfig.IMG_WALL);

        // Carregar Jogador
        loadImage("PLAYER", GameConfig.IMG_PLAYER);
    }

    private void loadImage(String key, String fileName) {
        try {
            // Tenta ler da pasta resources na raiz
            File file = new File(GameConfig.IMG_FOLDER + fileName);
            if (file.exists()) {
                BufferedImage img = ImageIO.read(file);
                images.put(key, img);
                System.out.println("Imagem carregada: " + fileName);
            } else {
                System.err.println("Imagem não encontrada: " + fileName);
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler imagem " + fileName + ": " + e.getMessage());
        }
    }

    public BufferedImage getImage(String key) {
        return images.get(key);
    }
}