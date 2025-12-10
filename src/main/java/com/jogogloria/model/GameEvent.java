package com.jogogloria.model;

/**
 * Representa um novo envento significativo ocorrido durante o jogo
 * Os eventos são gerados pelos jogadores e servem para criar logs
 * que é guardado num ficheiro JSON para análise póstuma
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class GameEvent implements Comparable<GameEvent> {

    /** O número do turno em que o evento ocorreu*/
    private final int turn;

    /** O tipo de evento*/
    private final String type;

    /** Descrição detalhada do evento*/
    private final String description;

    /** A hora exata do sistema em que o objeto foi criado*/
    private final String timestamp;

    /**
     * Cria um novo evento de jogo
     * @param turn O numero do turno atual
     * @param type A categoria do evento
     * @param description A mensagem descritiva do que aconteceu
     */
    public GameEvent(int turn, String type, String description) {
        this.turn = turn;
        this.type = type;
        this.description = description;
        this.timestamp = java.time.LocalTime.now().toString();
    }

    /**
     * Converte o evento numa String formatada em JSON
     * Serve para gravar histórico do jogo em ficheiro
     * Trata caracteres especiais na descrição para evitar erros
     * @return Uma String JSON válida representando este objeto
     */
    public String toJson() {
        String doc = description.replace("\"", "\\\"");
        return String.format("{\"turn\": %d, \"type\": \"%s\", \"description\": \"%s\", \\\"time\\\": \\\"%s\\\"}", turn, type, description, timestamp);
    }

    /**
     * Retorna a descrição do evento
     * @return A descrição textual
     */
    @Override
    public  String toString() {
        return description;
    }

    /**
     * Comaparação de eventos
     * @param o O outro evento a comparar
     * @return 0
     */
    @Override
    public int compareTo(GameEvent o) {
        return 0;
    }
}
