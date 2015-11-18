package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorConfigStepTab;
import org.openqa.selenium.WebElement;

public class ConnectorConfigStep implements WizardStep {
    private static final String TITLE = "Connector Configuration";

    private final NewConnectionPage newConnectionPage;
    private final WebConnector connector;

    public ConnectorConfigStep(NewConnectionPage newConnectionPage, WebConnector connector) {
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

        if(connector.getMaxPages() != null || connector.getDepth() != null || connector.getDuration() != null) {
            WebElement advancedConfig = connectorConfigStepTab.advancedConfigurations();
            advancedConfig.click();

            try {
                Thread.sleep(1000);
            } catch (Exception e) {/*NOOP*/}

            if (connector.getMaxPages() != null) {
                connectorConfigStepTab.getMaxPagesBox().setValue(connector.getMaxPages().toString());
            }

            if (connector.getDepth() != null) {
                connectorConfigStepTab.getDepthBox().setValue(connector.getDepth().toString());
            }

            if (connector.getDuration() != null) {
                connectorConfigStepTab.getDurationBox().setValue(connector.getDuration().toString());
            }

            advancedConfig.click();
            try {
                Thread.sleep(1000);
            } catch (Exception e) {/*NOOP*/}
        }

        if(connector.getCredentials() != null){
            connector.getCredentials().apply(connectorConfigStepTab);
        }

        return null;
    }
}
