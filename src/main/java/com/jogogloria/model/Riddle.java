package com.jogogloria.model;

public class Riddle implements Comparable<Riddle> {
    private String question;
    private String answer;

    public Riddle(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }

    @Override
    public String toString() {
        return question;
    }

    @Override
    public int compareTo(Riddle o) {
        return 0;
    }
}