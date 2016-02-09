package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.page.SAASPageBase;
import com.autonomy.abc.selenium.connections.wizard.ConnectorConfigStepTab;
import com.autonomy.abc.selenium.connections.wizard.ConnectorIndexStepTab;
import com.autonomy.abc.selenium.connections.wizard.ConnectorTypeStepTab;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class NewConnectionPage extends SAASPageBase {
    private NewConnectionPage(WebDriver driver) {
        super(driver);
    }

    public ConnectorTypeStepTab getConnectorTypeStep(){
        return ConnectorTypeStepTab.make(getDriver());
    }

    public ConnectorIndexStepTab getIndexStep(){
        return ConnectorIndexStepTab.make(getDriver());
    }

    public ConnectorConfigStepTab getConnectorConfigStep(){
        return ConnectorConfigStepTab.make(getDriver());
    }

    private WebElement menuButton(String text) {
        return findElement(By.className("actions")).findElement(By.xpath(".//a[contains(text(), '" + text + "')]"));
    }

    public WebElement nextButton() {
        return menuButton("Next");
    }

    public WebElement finishButton() {
        return menuButton("Finish");
    }

    public WebElement cancelButton() {
        return menuButton("Cancel");
    }

    public AppElement connectorTypeStepTab() {
        return new AppElement(findElement(By.id("stepAnchor1")), getDriver());
    }

    public AppElement connectorSchedualeStepTab() {
        return new AppElement(findElement(By.id("stepAnchor2")), getDriver());
    }

    public AppElement connectorIndexStepTab() {
        return new AppElement(findElement(By.id("stepAnchor3")), getDriver());
    }

    public AppElement connectorSummaryStepTab() {
        return new AppElement(findElement(By.id("stepAnchor4")), getDriver());
    }

    public static class Factory implements ParametrizedFactory<WebDriver, NewConnectionPage> {
        @Override
        public NewConnectionPage create(WebDriver context) {
            new WebDriverWait(context, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("wizard")));
            return new NewConnectionPage(context);
        }
    }
}
