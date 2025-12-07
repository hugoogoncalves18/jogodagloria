package com.jogogloria.model;

public class PlayerScore implements Comparable<PlayerScore> {
    private final Player player;

    public PlayerScore(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return player.getName() + ": " + player.getWins() + " vitórias";
    }

    @Override
    public int compareTo(PlayerScore other) {
        return other.player.getWins() - this.player.getWins();
    }
}
