package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorConfigStepTab;
import org.openqa.selenium.WebElement;

public class ConnectorConfigStep implements WizardStep {
    private static final String TITLE = "Connector Configuration";

    private final NewConnectionPage newConnectionPage;
    private final int depth;
    private final int maxPages;

    public ConnectorConfigStep(NewConnectionPage newConnectionPage, int depth, int maxPages) {
        this.newConnectionPage = newConnectionPage;
        this.depth = depth;
        this.maxPages = maxPages;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Object apply() {
        ConnectorConfigStepTab connectorConfigStepTab = newConnectionPage.getConnectorConfigStep();

        connectorConfigStepTab.advancedConfigurations().click();
        WebElement maxPagesBox = connectorConfigStepTab.getMaxPagesBox();
        maxPagesBox.clear();
        maxPagesBox.sendKeys(maxPages + "");
        WebElement depthBox = connectorConfigStepTab.getDepthBox();
        depthBox.clear();
        depthBox.sendKeys(depth + "");

        return null;
    }
}
