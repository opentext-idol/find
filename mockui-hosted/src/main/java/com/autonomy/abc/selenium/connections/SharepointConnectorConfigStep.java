package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;

public class SharepointConnectorConfigStep implements WizardStep {
    private static final String TITLE = "Connector Configuration";

    private final NewConnectionPage newConnectionPage;
    private final SharepointConnector connector;

    SharepointConnectorConfigStep(NewConnectionPage newConnectionPage, SharepointConnector connector) {
        this.newConnectionPage = newConnectionPage;
        this.connector = connector;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Object apply() {
        ConnectorConfigStepTab connectorConfigStepTab = newConnectionPage.getConnectorConfigStep();

        SharepointCredentialsConfigurations credentialsConfigurations = connectorConfigStepTab.getSharepointCredentialsConfigurations();

        credentialsConfigurations.userNameInput().setValue(connector.getUsername());
        credentialsConfigurations.passwordInput().setValue(connector.getPassword());
        credentialsConfigurations.notificationEmailInput().setValue(connector.getNotificationEmail());

        if (connector.isOnline()) {
            credentialsConfigurations.onlineCheckbox().click();
        }

        credentialsConfigurations.selectURLType(connector.getUrlType());

        return null;
    }
}
