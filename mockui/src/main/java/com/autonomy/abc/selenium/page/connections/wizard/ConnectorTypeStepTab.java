package com.autonomy.abc.selenium.page.connections.wizard;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.SAASPageBase;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Created by avidan on 10-11-15.
 */
public class ConnectorTypeStepTab extends SAASPageBase{
    public ConnectorTypeStepTab(WebDriver driver) {
        super(driver);
    }

    public static ConnectorTypeStepTab make(WebDriver driver) {
        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.name("connectorTypeStepForm")));
        return new ConnectorTypeStepTab(driver);
    }

    public AppElement webConnectorType() {
        return new AppElement(findElement(By.id("web_cloud")), getDriver());
    }

    public AppElement filesystemConnectorType() {
        return new AppElement(findElement(By.id("filesystem_onsite")), getDriver());
    }

    public AppElement sharepointConnectorType() {
        return new AppElement(findElement(By.id("sharepoint_onsite")), getDriver());
    }

    public AppElement dropboxConnectorType() {
        return new AppElement(findElement(By.id("dropbox_cloud")), getDriver());
    }

    public FormInput connectorUrl() {
        return new FormInput(findElement(By.name("connectorUrl")), getDriver());
    }

    public FormInput connectorPath(){
        return new FormInput(findElement(By.name("connectorPath")),getDriver());
    }

    public FormInput connectorName() {
        return new FormInput(findElement(By.name("name")), getDriver());
    }
}
