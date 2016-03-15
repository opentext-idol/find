package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.indexes.Index;

public class FileSystemConnector extends Connector {
    private final String url;

    public FileSystemConnector(String url, String name) {
        super(name);
        this.url = url;
    }

    public FileSystemConnector(String url, String name, Index index) {
        super(name, index);
        this.url = url;
    }

    @Override
    public Wizard makeWizard(NewConnectionPage newConnectionPage) {
        return null;
    }
}
