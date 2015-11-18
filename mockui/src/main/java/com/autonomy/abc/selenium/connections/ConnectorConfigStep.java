package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorConfigStepTab;
import org.openqa.selenium.WebElement;

public class ConnectorConfigStep implements WizardStep {
    private static final String TITLE = "Connector Configuration";

    private final NewConnectionPage newConnectionPage;
    private final WebConnector webConnector;
    private Integer depth;
    private Integer maxPages;

    public ConnectorConfigStep(NewConnectionPage newConnectionPage, WebConnector connector) {
        this.newConnectionPage = newConnectionPage;
        this.webConnector = connector;
    }

    public ConnectorConfigStep withDepth(Integer depth){
        this.depth = depth;
        return this;
    }

    public ConnectorConfigStep maxPages(Integer maxPages){
        this.maxPages = maxPages;
        return this;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Object apply() {
        if(maxPages != null || depth != null) {
            ConnectorConfigStepTab connectorConfigStepTab = newConnectionPage.getConnectorConfigStep();

            WebElement advancedConfig = connectorConfigStepTab.advancedConfigurations();
            advancedConfig.click();

            try {
                Thread.sleep(1000);
            } catch (Exception e) {/*NOOP*/}

            if (maxPages != null) {
                connectorConfigStepTab.getMaxPagesBox().setValue(maxPages + "");
            }

            if (depth != null) {
                connectorConfigStepTab.getDepthBox().setValue(depth + "");
            }

            advancedConfig.click();
            try {
                Thread.sleep(1000);
            } catch (Exception e) {/*NOOP*/}

            if(webConnector instanceof SecureWebConnector){
                ((SecureWebConnector) webConnector).getCredentials().apply();
            }
        }

        return null;
    }
}
