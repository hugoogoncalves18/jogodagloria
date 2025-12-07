package com.jogogloria.engine;
import com.example.Biblioteca.queues.CircularArrayQueue;
import com.jogogloria.model.Boost;

public class BoostManager {
    public CircularArrayQueue<Boost> boostQueue;

    public BoostManager() {
        this.boostQueue = new CircularArrayQueue<>();
        loadBoosts();
    }

    private void loadBoosts() {
        boostQueue.enqueue(new Boost("Joga outra vez"));
    }

    public Boost getNextBoost() {
        if (boostQueue.isEmpty()) return new Boost("Joga de novo");

        try{
            Boost x = boostQueue.dequeue();
            boostQueue.enqueue(x);
            return x;
        } catch (Exception e) {
            return null;
        }
    }
}
