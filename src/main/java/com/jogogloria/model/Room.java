package com.jogogloria.model;

/**
 * Representa uma sala no mapa de jogo
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class Room implements Comparable<Room> {

    /**
     * Enumeração que define os diferentes tipos de salas e os seus efeitos
     */
    public enum RoomType {
        START, NORMAL, RIDDLE, LEVER, PENALTY, BOOST, EXIT
    }

    /** Identificador único da sala*/
    private final String id;

    /** Coordenada X  para desenhar na interface gráfica. */
    private final int x; // Coordenada Visual X

    /** Coordenada Y para desenhar na interface gráfica. */
    private final int y; // Coordenada Visual Y

    /** O tipo de comportamento desta sala. */
    private final RoomType type;

    /** Texto ou etiqueta opcional para exibir na sala. */
    private final String label;

    /** Campo genérico para guardar informação adicional se necessário. */
    private String extraData;

    private Lever lever;

    /**
     * Cria uma nova sala
     * @param id ID
     * @param type Tipo de sala
     * @param label Etiqueta de texto
     */
    public Room(String id, RoomType type, String label) {
        this.id = id;
        this.type = type;
        this.label = label;
        this.extraData = null;
        this.lever = null;

        // Tenta extrair coordenadas do ID (formato "x-y") para facilitar a GUI
        int tempX = 0, tempY = 0;
        try {
            String[] parts = id.split("-");
            if (parts.length == 2) {
                tempX = Integer.parseInt(parts[0]);
                tempY = Integer.parseInt(parts[1]);
            }
        } catch (NumberFormatException e) {
            // Se o ID não for "x-y", fica 0,0 (fallback)
        }
        this.x = tempX;
        this.y = tempY;
    }

    public String getId() {
        return id;
    }

    /** Obtém a coordenada X da sala na grelha. */
    public int getX() {
        return x;
    }

    /** Obtém a coordenada Y da sala na grelha. */
    public int getY() {
        return y;
    }

    public RoomType getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public String getExtraData() {
        return extraData;

    }
    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public void setLever(Lever lever) {
        this.lever = lever;
    }

    public Lever getLever() {
        return this.lever;
    }

    public boolean hasLever() {
        return this.lever != null;
    }

    /**
     * Verifica se a sala é especial
     * @return {@code true} de for alavanca, enigma ou boost
     */
    public boolean isSpecial() {
        return type == RoomType.RIDDLE || type == RoomType.BOOST ||
                type == RoomType.PENALTY || type == RoomType.LEVER;
    }

    @Override
    public String toString() {
        return "Room [" + id + "] (" + x + "," + y + ") Type: " + type;
    }

    /**
     * Comparação de salas
     * @param o Outra sala
     * @return 0
     */
    @Override
    public int compareTo(Room o) {
        return 0;
    }
}