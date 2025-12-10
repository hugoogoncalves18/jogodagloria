package com.jogogloria.model;

/**
 * Representa um bónus que o jogador pode receber no jogo
 * Os boosts são eventos que dão beneficios ao jogador
 * ("pode jogar a 2 vez), que ajuda o jogador a chegar mais rapidamente
 * ao tesouro
 * Esta classe implementa {@link Comparable} para permitir que os objetos sejam
 * armazenados em estruturas de dados da biblioteca
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class Boost implements Comparable<Boost> {
    /** Descrição textual do efeito de boost */
    private final String description;

    /**
     * Cria um novo objeto de Boost
     * @param description A descrição do efeito benéfico
     */
    public Boost(String description) {
        this.description = description;
    }

    /**
     * Obtém a descrição do boost
     * @return A descrição textual
     */
    public String getDescription() {
        return description;
    }

    /**
     * Retorna a representação em string do boost
     * Util para logs e mensagen na consola
     * @return A descrição do boost
     */
    @Override
    public String toString() {
        return description;
    }

    /**
     * Comparação de objetos boost
     *
     * @param o O outro boost a comparar
     * @return 0
     */
    @Override
    public int compareTo(Boost o) {
        return 0;
    }
}
