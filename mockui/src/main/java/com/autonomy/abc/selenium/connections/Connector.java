package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;

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

    public abstract Wizard makeWizard(NewConnectionPage newConnectionPage);
}
