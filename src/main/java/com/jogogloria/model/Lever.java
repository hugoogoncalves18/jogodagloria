package com.jogogloria.model;

public class Lever {
    private final String id;
    private final String doorA;
    private final String doorB;
    private boolean activated;

    public Lever(String id, String doorA, String doorB) {
        this.id = id;
        this.doorA = doorA;
        this.doorB = doorB;
        this.activated = false;
    }

    public  String getDoorA() {
        return doorA;
    }

    public String getDoorB() {
        return doorB;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    @Override
    public String toString() {
        return "Alavanca" + id + "Abre: " + doorA + "<->" + doorB;
    }
}
