package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorConfigStepTab;
import org.openqa.selenium.WebElement;

public class ConnectorConfigStep implements WizardStep {
    private static final String TITLE = "Connector Configuration";

    private final NewConnectionPage newConnectionPage;
    private Integer depth;
    private Integer maxPages;
    private Integer duration;
    private Credentials credentials;

    public ConnectorConfigStep(NewConnectionPage newConnectionPage) {
        this.newConnectionPage = newConnectionPage;
    }

    public ConnectorConfigStep withDepth(Integer depth){
        this.depth = depth;
        return this;
    }

    public ConnectorConfigStep maxPages(Integer maxPages){
        this.maxPages = maxPages;
        return this;
    }

    public ConnectorConfigStep withCredentials(Credentials credentials){
        this.credentials = credentials;
        return this;
    }

    public WizardStep withDuration(Integer duration) {
        this.duration = duration;
        return this;;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Object apply() {
        ConnectorConfigStepTab connectorConfigStepTab = newConnectionPage.getConnectorConfigStep();

        if(maxPages != null || depth != null || duration != null) {
            WebElement advancedConfig = connectorConfigStepTab.advancedConfigurations();
            advancedConfig.click();

            try {
                Thread.sleep(1000);
            } catch (Exception e) {/*NOOP*/}

            if (maxPages != null) {
                connectorConfigStepTab.getMaxPagesBox().setValue(maxPages.toString());
            }

            if (depth != null) {
                connectorConfigStepTab.getDepthBox().setValue(depth.toString());
            }

            if (duration != null) {
                connectorConfigStepTab.getDurationBox().setValue(duration.toString());
            }

            advancedConfig.click();
            try {
                Thread.sleep(1000);
            } catch (Exception e) {/*NOOP*/}
        }

        if(credentials != null){
            credentials.apply(connectorConfigStepTab);
        }

        return null;
    }
}
