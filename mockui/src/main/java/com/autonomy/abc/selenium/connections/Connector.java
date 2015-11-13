package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;

public abstract class Connector {
    protected String name;
    protected Index index;

    public Connector(String name) {
        this.name = name;
        this.index = new Index(name);
    }

    public Connector(String name, Index index){
        this.name = name;
        this.index = index;
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

    public Index getIndex(){
        return index;
    }
}
