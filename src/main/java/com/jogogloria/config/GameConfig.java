package com.jogogloria.config;
import java.awt.Color;

public class GameConfig {
    //Ficheiros e recursos
    public static final String MAP_FILE = "mapa.json";
    public static final String RIDDLES_FILE = "riddles.json";
    public static final String PENALTIES_FILE = "penalties.json";
    public static final String IMG_FOLDER = "resources/";
    public static final String IMG_FLOOR = "floor.png";
    public static final String IMG_WALL = "wall.png";
    public static final String IMG_EXIT = "exit.png";
    public static final String IMG_PLAYER = "player.png";
    public static final String IMG_RIDDLE = "riddle.png";
    public static final String IMG_LEVER = "lever.png";
    public static final String IMG_START = "start.png";
    public static final String IMG_PENALTY = "penalty.png";
    public static final String IMG_BOOST = "boost.png";

    //caso falhe a imagem
    public static final Color COLOR_START = new Color(144, 238, 144);
    public static final Color COLOR_EXIT = new Color(255, 215, 0);
    public static final Color COLOR_PENALTY = new Color(255, 99, 71);
    public static final Color COLOR_BOOST = new Color(135, 206, 250);
    public static final Color COLOR_RIDDLE = new Color(221, 160, 221);
    public static final Color COLOR_LEVER = new Color(219, 7, 7);
    public static final Color COLOR_NORMAL = new Color(219, 219, 219);

    //Interface
    public static final String GAME_TITLE = "Jogo da glória";
    public static final int CELL_SIZE = 30;

    //Codigos do mapa JSON
    public static final int CODE_EMPTY = 0;
    public static final int CODE_START = 1;
    public static final int CODE_NORMAL = 2;
    public static final int CODE_RIDDLE = 3;
    public static final int CODE_PENALTY = 4;
    public static final int CODE_BOOST = 5;
    public static final int CODE_LEVER = 6;
    public static final int CODE_EXIT = 9;

    //Definições Gameplay
    public static final int BOT_DELAY = 1000;
}
