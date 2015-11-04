package com.autonomy.abc.selenium.page.connections;

import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.SAASPageBase;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class NewConnectionPage extends SAASPageBase {
    private NewConnectionPage(WebDriver driver) {
        super(driver);
    }

    public static NewConnectionPage make(WebDriver driver) {
        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("actions")));
        return new NewConnectionPage(driver);
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

    public FormInput connectorName() {
        return new FormInput(findElement(By.name("name")), getDriver());
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

    public void loadOrFadeWait() {
        getPage().loadOrFadeWait();
    }
}
