package com.jogogloria.model;

import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.iterators.Iterator;

/**
 * Representa um enigma que o jogador tem de acertar
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class Riddle implements Comparable<Riddle> {

    /** Identificador único do desafio. */
    private final String id;

    /** O texto da pergunta a apresentar ao jogador. */
    private final String question;

    /** A resposta correta. */
    private final String answer;

    /** Lista de opções para escolha múltipla */
    private final ArrayUnorderedList<String> options;

    /** Valor do prémio em caso de sucesso. */
    private final int bonus;

    /** Valor do castigo em caso de falha.*/
    private final int penalty;

    /**
     * Cria um novo enigma
     * @param id ID
     * @param question texto da pergunta
     * @param answer resposta
     * @param options opções possiveis
     * @param bonus Valor positico para recompensar o jogador
     * @param penalty Valor negativo para prejudicar o jogador
     */
    public Riddle(String id, String question, String answer, ArrayUnorderedList<String> options, int bonus, int penalty ) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.options = options;
        this.bonus = bonus;
        this.penalty = penalty;
    }

    /**
     * Verifica se a resposta está correta
     * @param input A resposta do utilizador
     * @return {@code true} se correta, {@code false} se incorreta
     */
    public boolean checkAnswer(String input) {
        if (input == null) return false;
        String cleanInput = input.trim();
        if (cleanInput.equalsIgnoreCase(answer)) return true;
        try {
            int index = Integer.parseInt(cleanInput);
            int listIndex = index - 1;
            if (options != null && listIndex >= 0 && listIndex < options.size()) {
                String selectedOption = options.get(listIndex);
                return selectedOption.equalsIgnoreCase(answer);
            }
        } catch (NumberFormatException e) {}

        return false;
    }

    /**
     * Formata a pergunta e as opções disponiveis numa única String legível
     * @return A pergunta formatada com as opções numeradas
     */
    public String getQuestion() {
        StringBuilder sb = new StringBuilder();
        sb.append(question).append("\n");

        if (options != null && !options.isEmpty()) {
            Iterator<String> it = options.iterator();
            int i = 1;
            while (it.hasNext()) {
                sb.append("[").append(i).append("] ").append(it.next()).append("\n");
                i++;
            }
        }
        return sb.toString();
    }


    /**
     * Obtém a resposta
     * @return A resposta
     */
    public String getAnswer() {
        return answer;
    }

    public String getId() {
        return id;
    }

    /**
     * Obtém o valor do bonus
     * @return Pontos ou casas a avançar
     */
    public int getBonus() {
        return bonus;
    }

    /**
     * Obtém o valor da penalidade
     * @return Pontos a recuar
     */
    public int getPenalty() {
        return penalty;
    }

    @Override
    public String toString() {

        return question;
    }

    /**
     * Comparação de enigmas
     * @param o outro enigma
     * @return 0
     */
    @Override
    public int compareTo(Riddle o) {
        return 0;
    }
}