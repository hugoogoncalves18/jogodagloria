package com.jogogloria.engine;

import com.example.Biblioteca.exceptions.NoElementFoundException;
import com.example.Biblioteca.exceptions.EmptyCollectionException;
import com.jogogloria.model.Riddle;
import com.jogogloria.io.RiddleLoader;
import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.stacks.LinkedStack;

/**
 * Gestor de enigmas do jogo
 *
 * @author Hugo Gonçalves
 * @version 1.0
 */
public class RiddleManager {

    private ArrayUnorderedList<Riddle> availableRiddles;
    private LinkedStack<Riddle> usedRiddles;

    /**
     * Inicia o gestor de enigmas, carrega os dados a partir de um ficheiro JSON
     * @param jsonFilePath Caminho para o ficheiro JSON
     */
    public RiddleManager(String jsonFilePath) {
        this.usedRiddles = new LinkedStack<>();
        this.availableRiddles = RiddleLoader.loadRiddles(jsonFilePath);
    }

    /**
     * Obtém um enigma aleatório da lista de disponiveis
     * @return Um objeto {@link Riddle} ou {@code null} de não existirem enigmas
     * @throws NoElementFoundException Se ocorrer um erro interno
     */
    public Riddle getRandomRiddle() throws NoElementFoundException {
        if (availableRiddles.isEmpty()) {
            if (usedRiddles.isEmpty()) {
                return null; // Não há enigmas carregados
            }
            System.out.println("A recarregar enigmas usados...");
            reloadRiddles();
        }

        int size = availableRiddles.size();
        int randomIndex = (int) (Math.random() * size);

        Riddle r = availableRiddles.get(randomIndex);

        try {
            availableRiddles.remove(r);
        } catch (Exception e) {}

        usedRiddles.push(r);

        return r;
    }

    /**
     * Recicla os enigmas utilizados, movendo-os de volta para a lista de disponiveis
     */
    private void reloadRiddles() {
        while (!usedRiddles.isEmpty()) {
            try {
                Riddle r = usedRiddles.pop();
                availableRiddles.addToRear(r);
            } catch (EmptyCollectionException e) {
                break;
            }
        }
    }
}