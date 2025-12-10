package com.jogogloria.engine;
import com.example.Biblioteca.queues.CircularArrayQueue;
import com.jogogloria.model.Boost;

/**
 * Gestor de bónus
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class BoostManager {
    public CircularArrayQueue<Boost> boostQueue;

    /**
     * Inicia o gestor de boosts
     */
    public BoostManager() {
        this.boostQueue = new CircularArrayQueue<>();
        loadBoosts();
    }

    /**
     * Carrega os tipos de boosts iniciais
     */
    private void loadBoosts() {
        boostQueue.enqueue(new Boost("Joga outra vez"));
    }

    /**
     * Obtém o próximo boost disponivel na fila
     * @return Objeto {@link Boost} sorteado
     */
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
