package com.autonomy.abc.selenium.connections;

public abstract class Connector {
    protected String name;

    public Connector(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getFinishedNotification() {
        return "Connection " + getName() + " has finished running";
    }

    public String getDeleteNotification() {
        return "Deleting connection " + getName();
    }
}
