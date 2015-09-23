package com.autonomy.abc.selenium.page.connections;

import com.autonomy.abc.selenium.page.SAASPageBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class ConnectionsPage extends SAASPageBase {
    private ConnectionsPage(final WebDriver driver) {
        super(driver);
    }

    public static ConnectionsPage make(WebDriver driver) {
        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("base-page-content")));
        return new ConnectionsPage(driver);
    }

    public WebElement newConnectionButton() {
        return findElement(By.cssSelector(".affix-toolbar:not(.affix-clone) #new-repo-btn"));
    }

    public List<WebElement> connectionsList() {
        return findElements(By.cssSelector(".list-group .data-container"));
    }

    public WebElement connectionWithTitleContaining(String name) {
        return findElement(By.xpath(".//*[contains(@class, 'listItemTitle')][contains(text(), '" + name + "')]"));
    }

}
