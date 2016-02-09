package com.autonomy.abc.selenium.connections.wizard;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.connections.NewConnectionPage;

public class ConnectorTypeStep implements WizardStep {
    private final static String TITLE = "Select Connector Type";
    private String url;
    private String name;

    private ConnectorTypeStepTab connectorTypeStepTab;
    public ConnectorTypeStep(NewConnectionPage newConnectionPage, String url, String name) {
        this.url = url;
        this.name = name;
        this.connectorTypeStepTab = newConnectionPage.getConnectorTypeStep();
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Object apply() {
        connectorTypeStepTab.connectorUrl().setValue(url);
        connectorTypeStepTab.connectorName().setValue(name);
        return null;
    }

    @Override
    public String toString() {
        return "wizard step " + getTitle();
    }
}
