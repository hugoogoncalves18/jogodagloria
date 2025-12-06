package com.jogogloria.engine;

import com.example.Biblioteca.exceptions.NoElementFoundException;
import com.example.Biblioteca.exceptions.EmptyCollectionException;
import com.jogogloria.model.Riddle;
import com.jogogloria.io.RiddleLoader;
import com.example.Biblioteca.lists.ArrayUnorderedList;
import com.example.Biblioteca.stacks.LinkedStack;

public class RiddleManager {

    private ArrayUnorderedList<Riddle> availableRiddles;
    private LinkedStack<Riddle> usedRiddles;

    public RiddleManager(String jsonFilePath) {
        this.usedRiddles = new LinkedStack<>();
        this.availableRiddles = RiddleLoader.loadRiddles(jsonFilePath);
    }

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