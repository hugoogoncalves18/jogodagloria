package com.jogogloria.engine;
import com.example.Biblioteca.exceptions.EmptyCollectionException;
import com.example.Biblioteca.queues.CircularArrayQueue;
import com.jogogloria.model.Penalty;
import com.jogogloria.model.Penalty.PenaltyType;

public class PenaltyManager {
    private CircularArrayQueue<Penalty> penaltyList;

    public PenaltyManager() {
        this.penaltyList = new CircularArrayQueue<>();
        loadPenalties();
    }

    private void loadPenalties() {
        //Proprio
        penaltyList.enqueue(new Penalty("Azar! Recua 1 casa", PenaltyType.RETREAT, -1));
        penaltyList.enqueue(new Penalty("Azar! Recua 2 casas", PenaltyType.RETREAT, -2 ));
        penaltyList.enqueue(new Penalty("Azar! Volta ao inicio", PenaltyType.RETREAT, -99));

        //Perde vez
        penaltyList.enqueue(new Penalty("Passa a vez", PenaltyType.SKIP_TURN, 1));

        //Beneficios para os outros
        penaltyList.enqueue(new Penalty("Deste prémios aos outros, todos avançam 1 casa", PenaltyType.PLAYERS_BENEFITS, 1));
        penaltyList.enqueue(new Penalty("Deste prémio aos outros, todos avanças 2 casas", PenaltyType.PLAYERS_BENEFITS, 2));
    }

    public Penalty getNextPenalty() throws EmptyCollectionException {
        if (penaltyList.isEmpty())
            throw new EmptyCollectionException("Não existem penalidades");

        try{
            Penalty p = penaltyList.dequeue();
            penaltyList.enqueue(p);
            return p;
        } catch (Exception e) {
            System.out.println("Erro" + e.getMessage());
            return null;
        }
    }
}
