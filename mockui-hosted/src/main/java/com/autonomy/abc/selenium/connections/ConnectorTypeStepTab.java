package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.icma.ICMAPageBase;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ConnectorTypeStepTab extends ICMAPageBase {
    private ConnectorTypeStepTab(WebDriver driver) {
        super(driver);
    }

    public static ConnectorTypeStepTab make(WebDriver driver) {
        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.name("connectorTypeStepForm")));
        return new ConnectorTypeStepTab(driver);
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

    public AppElement typeBtn(ConnectorType type) {
        return new AppElement(findElement(type.getLocator()), getDriver());
    }

    public WebElement fileSystemConnector() {
        return findElement(By.id("filesystem_onsite"));
    }

    public WebElement sharepointConnector() {
        return findElement(By.id("sharepoint_onsite"));
    }

    public WebElement dropboxConnector() {
        return findElement(By.id("dropbox_cloud"));
    }

    public FormInput connectorSource() {
        return new FormInput(findElement(By.name("connectorPath")), getDriver());
    }
}
