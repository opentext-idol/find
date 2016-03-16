package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;

class ConnectorTypeStep implements WizardStep {
    private final static String TITLE = "Select Connector Type";
    private String url;
    private String name;
    private Connector connector;

    private ConnectorTypeStepTab connectorTypeStepTab;
    ConnectorTypeStep(NewConnectionPage newConnectionPage, String url, String name, Connector connector) {
        this.url = url;
        this.name = name;
        this.connectorTypeStepTab = newConnectionPage.getConnectorTypeStep();
        this.connector = connector;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Object apply() {
        if(connector instanceof DropboxConnector) {
            connectorTypeStepTab.dropboxConnector().click();
        } else if (connector instanceof FileSystemConnector) {
            connectorTypeStepTab.fileSystemConnector().click();
            connectorTypeStepTab.connectorSource().setValue(url);
        } else {
            if (connector instanceof SharepointConnector) {
                connectorTypeStepTab.sharepointConnector().click();
            }

            connectorTypeStepTab.connectorUrl().setValue(url);
        }

        connectorTypeStepTab.connectorName().setValue(name);
        return null;
    }

    @Override
    public String toString() {
        return "wizard step " + getTitle();
    }
}
