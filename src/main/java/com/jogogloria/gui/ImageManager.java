package com.jogogloria.gui;

import com.jogogloria.config.GameConfig;
import com.jogogloria.utils.SimpleMap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Gestor de interface gráfica do jogo
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class ImageManager {

    /** Cache de imagens: Associa um nome lógico é imagem carregada*/
    private SimpleMap<String, BufferedImage> images;

    /**
     * Construtor do Gestor de imagens
     */
    public ImageManager() {
        this.images = new SimpleMap<>();
        loadImages();
    }

    /**
     * Carrega em lote todas as imagens definidas nas configurações do jogo
     */
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

    /**
     * Método auxiliar que lê uma imagem do disco e guarda na cache
     * @param key A chave para identificar a imagem futuramente
     * @param fileName O nome do ficheiro na pasta de recursos
     */
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

    /**
     * Recupera uma imagem da cache pronta a desenhar
     * @param key A chave da imagem
     * @return O objeto {@link BufferedImage} ou {@code null} se a imagem não tiver sido carregada
     */
    public BufferedImage getImage(String key) {
        return images.get(key);
    }
}