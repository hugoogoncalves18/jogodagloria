package com.jogogloria.model;
import com.example.Biblioteca.lists.LinkedList;

/**
 * Representa um Corredor que liga duas salas no labirinto
 * Um corredor é uma conexão entre duas salas
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class Corridor implements Comparable<Corridor> {

    /** Identificador único do corredor*/
    private final String id;

    /** ID da primeira sala conectada por este corredor*/
    private final String roomAId;

    /** ID da segunda sala conectada por este corredor*/
    private final String roomBId;

    /** Indica se o corredor está bloqueado*/
    private boolean locked;

    /** Lista de eventos associados a este corredor (alavancas, boost, enigmas)*/
    private LinkedList<String> events;

    /** Peso do corredor para algoritmos de pathfinding*/
    private int weight;

    /**
     * Cria um novo corredor entre duas salas
     * @param id O identificador único do corredor
     * @param roomAId O ID da sala de origem A
     * @param roomBId O ID da dala de destino B
     */
    public Corridor(String id, String roomAId, String roomBId) {
        this.id = id;
        this.roomAId = roomAId;
        this.roomBId = roomBId;
        this.locked = false; //por defeito iniciamos o corredor como aberto
        this.weight = 1;
        this.events = new LinkedList<>();
    }

    /**
     * Obtém o ID do corredor
     * @return Identificador Único
     */
    public String getId() {
        return id;
    }

    /**
     * Obtém o ID da segunda sala conectada
     * @return ID da sala A
     */
    public String getRoomAId() {
        return roomAId;
    }

    /**
     * Obtém o ID da da segunda sala conectada
     * @return ID da sala B
     */
    public String getRoomBId() {
        return roomBId;
    }

    /**
     * Verifica se o corredor está trancado
     * @return {@code true} se estiver bloqueado, {@code false} se estiver livre
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Obtém o peso do corredor
     * @return peso
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Obtém a lista de eventos especiais neste corredor
     * @return Lista ligada de strings de eventos
     */
    public LinkedList<String> getEvents() {
        return events;
    }

    /**
     * Define o estado de bloqueio do corredor
     * Usado por alavancas para abrir passagens
     * @param locked {@code true} para trancar {@code false} para destrancar
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * Define o peso do corredor
     * Pode ser utilizado para penalizar certos caminhos
     * @param weight o peso
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }

    /**
     * Utilitário para descobrir qual é a outra sala ligada ao corredor
     * Dado o ID da sala, retorna o ID da sala oposta
     * @param currentRoomId O ID da sala onde o jogador está
     * @return O ID da sala vizinha, ou {@code null} se o ID fornecido não fizer parte do corredor
     */
    public String getOtherRoomId(String currentRoomId) {
        if (currentRoomId.equals(roomAId)) return roomBId;
        if (currentRoomId.equals(roomBId)) return roomAId;
        return null;
    }

    /**
     * Comparação de Corredores
     * @param o O outro corredor a comparar
     * @return 0
     */
    @Override
    public int compareTo(Corridor o) {
        return 0;
    }
}