package com.jogogloria.model;

/**
 * Representa uma alavanca
 * São mecanismos que quando são ativados por um jogador, destrancam passagens
 * Cada alavanca sabe exatamente quais as duas salas que compõem a porta que ela abre
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class Lever implements Comparable<Lever> {

    /** Identificador único da alavanca*/
    private final String id;

    /** Identificador da primeira sala associada á alavanca */
    private final String doorA;

    /** Identificador da segunda sala associada á alavanca*/
    private final String doorB;

    /** Estado da alavanca*/
    private boolean activated;

    /**
     * Cria uma nova alavanca
     * @param id O identificador único
     * @param doorA Id de uma das salas do corredor trancado
     * @param doorB Id da outra sala do corredor trancado
     */
    public Lever(String id, String doorA, String doorB) {
        this.id = id;
        this.doorA = doorA;
        this.doorB = doorB;
        this.activated = false;
    }

    /**
     * Obtém o ID da primeira sala da porta
     * @return ID da sala A
     */
    public  String getDoorA() {
        return doorA;
    }

    /**
     * Obtém o ID da primeira sala da porta
     * @return ID da sala B
     */
    public String getDoorB() {
        return doorB;
    }

    /**
     * Verifica se a alavanca já foi ativada
     * @return true se já foi usada, false se ainda não foi usada
     */
    public boolean isActivated() {
        return activated;
    }

    /**
     * Define o estado da alavanca
     * @param activated O novo estado da alavanca
     */
    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    /**
     * Retorna uma representação textual da alavanca e da porta qu controla
     * @return String descritiva
     */
    @Override
    public String toString() {
        return "Alavanca" + id + "Abre: " + doorA + "<->" + doorB;
    }

    /**
     * Comparação de alavancas
     * @param o A outra alavanca a comparar
     * @return 0
     */
    @Override
    public int compareTo(Lever o) {
        return 0;
    }
}
