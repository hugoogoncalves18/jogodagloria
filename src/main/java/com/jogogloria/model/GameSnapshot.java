package com.jogogloria.model;

import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.iterators.Iterator;

/**
 * Representa um estado do jogo num determinado momento
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class GameSnapshot {
    /** Guarda estado atual de cada jogador*/
    public final ArrayUnorderedList<PlayerMoment> playerState;

    /** Referência direta para o jogadpr de quem era a vez*/
    public final Player currentPlayer;

    /** Lista de Objetos Lever que estavam ativos*/
    public final ArrayUnorderedList<Lever> activatedLevers;

    /**
     * Construtor da classe
     */
    public GameSnapshot(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
        this.playerState = new ArrayUnorderedList<>();
        this.activatedLevers = new ArrayUnorderedList<>();
    }

    /**
     * Classe interna auxiliar para guardar os dados do jogador
     */
    public static class PlayerMoment implements Comparable<PlayerMoment> {
        public final Player playerRef;
        public final Room roomRef;
        public final int movementPoints;
        public final int skipTurns;
        public final int boostCount;

        public PlayerMoment(Player p) {
            this.playerRef = p;
            this.roomRef = p.getCurrentRoom();
            this.movementPoints = p.getMovementPoints();
            this.skipTurns = p.getSkipTurns();
            this.boostCount = p.getBoost();
        }

        @Override
        public int compareTo(PlayerMoment o) {
            return 0;
        }
    }
}
