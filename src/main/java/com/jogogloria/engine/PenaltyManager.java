package com.jogogloria.engine;

import com.example.Biblioteca.exceptions.EmptyCollectionException;
import com.example.Biblioteca.queues.CircularArrayQueue;
import com.jogogloria.config.GameConfig;
import com.jogogloria.io.PenaltyLoader;
import com.jogogloria.model.Penalty;

public class PenaltyManager {

    private CircularArrayQueue<Penalty> penaltyList;

    public PenaltyManager() {
        this.penaltyList = PenaltyLoader.loadPenalties(GameConfig.PENALTIES_FILE);


        if (this.penaltyList.isEmpty()) {
            System.out.println("Aviso: A usar penalidade de fallback.");
            this.penaltyList.enqueue(new Penalty("Perde a vez (Fallback)", Penalty.PenaltyType.SKIP_TURN, 1));
        }
    }

    public Penalty getNextPenalty() throws EmptyCollectionException {
        if (penaltyList.isEmpty())
            throw new EmptyCollectionException("Não existem penalidades");

        try {
            Penalty p = penaltyList.dequeue();
            penaltyList.enqueue(p);
            return p;
        } catch (Exception e) {
            System.out.println("Erro ao rodar penalidade: " + e.getMessage());
            return null;
        }
    }
}