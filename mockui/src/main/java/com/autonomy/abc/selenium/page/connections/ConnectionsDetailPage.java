package com.autonomy.abc.selenium.page.connections;

import com.autonomy.abc.selenium.page.SAASPageBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ConnectionsDetailPage extends SAASPageBase {
    private ConnectionsDetailPage(WebDriver driver) {
        super(driver);
    }

    public static ConnectionsDetailPage make(WebDriver driver) {
        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.id("first-cable")));
        return new ConnectionsDetailPage(driver);
    }

    private WebElement menuButton(String text) {
        return findElement(By.cssSelector(".toolbar .affix-element")).findElement(By.xpath(".//a[contains(text(), '" + text + "')]"));
    }

    public WebElement backButton() {
        return menuButton("Back");
    }

    public WebElement deleteButton() {
        return menuButton("Delete");
    }

    public WebElement deleteModal() {
        return new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-dialog")));
    }

    // TODO: this should be a "Checkbox" element
    public WebElement alsoDeleteIndexCheckbox() {
        return deleteModal().findElement(By.cssSelector("input[type=checkbox]"));
    }

    public WebElement deleteConfirmButton() {
        return deleteModal().findElement(By.cssSelector("button[type=submit]"));
    }

}
