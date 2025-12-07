package com.jogogloria.model;

public class Boost implements Comparable<Boost> {
    private final String description;

    public Boost(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public int compareTo(Boost o) {
        return 0;
    }
}
