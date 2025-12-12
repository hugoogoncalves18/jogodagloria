package com.jogogloria.model;

/**
 * Representa uma alavanca.
 * <p>
 * Como removemos a classe Corridor (Pure Graph), a alavanca agora guarda
 * referências diretas para as duas Salas (Room) cuja conexão (aresta) ela controla.
 * </p>
 *
 * @author Hugo Gonçalves
 * @version 3.0
 */
public class Lever implements Comparable<Lever> {

    private final String id;

    // Referências para as salas que formam a "porta"
    private final Room roomA;
    private final Room roomB;

    private boolean activated;

    /**
     * Construtor atualizado para receber 3 argumentos.
     * * @param id    Identificador da alavanca.
     * @param roomA Uma das salas da conexão.
     * @param roomB A outra sala da conexão.
     */
    public Lever(String id, Room roomA, Room roomB) {
        this.id = id;
        this.roomA = roomA;
        this.roomB = roomB;
        this.activated = false;
    }

    public String getId() { return id; }

    public Room getRoomA() { return roomA; }
    public Room getRoomB() { return roomB; }

    public boolean isActivated() { return activated; }
    public void setActivated(boolean activated) { this.activated = activated; }

    @Override
    public String toString() {
        return "Alavanca " + id;
    }

    @Override
    public int compareTo(Lever o) {
        return 0;
    }
}