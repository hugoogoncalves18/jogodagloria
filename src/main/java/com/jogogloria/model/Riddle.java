package com.jogogloria.model;

import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.iterators.Iterator;

public class Riddle implements Comparable<Riddle> {
    private final String id;
    private final String question;
    private final String answer;
    private final ArrayUnorderedList<String> options;
    private final int bonus;
    private final int penalty;

    public Riddle(String id, String question, String answer, ArrayUnorderedList<String> options, int bonus, int penalty ) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.options = options;
        this.bonus = bonus;
        this.penalty = penalty;
    }

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


    public String getAnswer() {
        return answer;
    }

    public String getId() {
        return id;
    }

    public int getBonus() {
        return bonus;
    }

    public int getPenalty() {
        return penalty;
    }

    @Override
    public String toString() {

        return question;
    }

    @Override
    public int compareTo(Riddle o) {
        return 0;
    }
}