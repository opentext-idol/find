package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;

public class ConnectorTypeStep implements WizardStep {
    private final static String TITLE = "Select Connector Type";
    private String url;
    private String name;

    private NewConnectionPage page;
    public ConnectorTypeStep(NewConnectionPage newConnectionPage, String url, String name) {
        this.url = url;
        this.name = name;
        this.page = newConnectionPage;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Object apply() {
        page.connectorUrl().setValue(url);
        page.connectorName().setValue(name);
        return null;
    }

    @Override
    public String toString() {
        return "wizard step " + getTitle();
    }
}
