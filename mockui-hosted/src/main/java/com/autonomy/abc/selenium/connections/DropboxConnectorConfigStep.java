package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;

public class DropboxConnectorConfigStep implements WizardStep {
    private static final String TITLE = "Connector Configuration";

    private final NewConnectionPage newConnectionPage;
    private final DropboxConnector connector;

    DropboxConnectorConfigStep(NewConnectionPage newConnectionPage, DropboxConnector connector) {
        this.newConnectionPage = newConnectionPage;
        this.connector = connector;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Object apply() {
        DropboxCredentialsConfigurations credentialsConfig = newConnectionPage.getConnectorConfigStep().getDropboxCredentialsConfigurations();

        if(!connector.isDropboxAccess()){
            credentialsConfig.fullDropboxAccess().click();
        }

        credentialsConfig.applicationKeyInput().setValue(connector.getAppKey());
        credentialsConfig.accessTokenInput().setValue(connector.getAccessToken());
        credentialsConfig.notificationEmailInput().setValue(connector.getNotificationEmail());

        return null;
    }
}
