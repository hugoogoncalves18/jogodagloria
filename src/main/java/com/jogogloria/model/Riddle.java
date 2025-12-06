package com.jogogloria.model;

import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.iterators.Iterator;

public class Riddle implements Comparable<Riddle> {
    private String id;
    private String question;
    private String answer;
    private ArrayUnorderedList<String> options;
    private int bonus;
    private int penalty;

    public Riddle(String id, String question, String answer, ArrayUnorderedList<String> options, int bonus, int penalty ) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.options = options;
        this.bonus = bonus;
        this.penalty = penalty;
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