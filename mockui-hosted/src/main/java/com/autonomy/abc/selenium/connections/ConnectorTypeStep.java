package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;

class ConnectorTypeStep implements WizardStep {
    private final static String TITLE = "Select Connector Type";
    private String url;
    private String name;

    private ConnectorTypeStepTab connectorTypeStepTab;
    ConnectorTypeStep(NewConnectionPage newConnectionPage, String url, String name) {
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
