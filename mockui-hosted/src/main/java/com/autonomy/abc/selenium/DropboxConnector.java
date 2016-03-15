package com.autonomy.abc.selenium;

import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.connections.Connector;
import com.autonomy.abc.selenium.connections.NewConnectionPage;
import com.autonomy.abc.selenium.indexes.Index;

public class DropboxConnector extends Connector {
    public DropboxConnector(String name) {
        super(name);
    }

    public DropboxConnector(String name, Index index) {
        super(name, index);
    }

    @Override
    public Wizard makeWizard(NewConnectionPage newConnectionPage) {
        return null;
    }
}
