package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.icma.ICMAPageBase;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class ConnectionsDetailPage extends ICMAPageBase {
    private ConnectionsDetailPage(WebDriver driver) {
        super(driver);
    }

    private WebElement menuButton(String text) {
        return findElement(By.cssSelector(".toolbar .affix-element")).findElement(By.xpath(".//a[contains(text(), '" + text + "')]"));
    }

    public WebElement backButton() {
        return menuButton("Back");
    }

    WebElement deleteButton() {
        return menuButton("Delete");
    }

    WebElement deleteModal() {
        return new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-dialog")));
    }

    // TODO: this should be a "Checkbox" element
    WebElement alsoDeleteIndexCheckbox() {
        return deleteModal().findElement(By.className("iCheck-helper"));
    }

    WebElement deleteConfirmButton() {
        return deleteModal().findElement(By.cssSelector("button[type=submit]"));
    }

    List<Integer> lastRun() {
        List<WebElement> lastRunElements = findElements(By.className("highlightDocCountNumber"));
        List<Integer> lastRun = new ArrayList<>();

        for(WebElement number : lastRunElements){
            lastRun.add(Integer.parseInt(number.getText()));
        }

        return lastRun;
    }

    public WebElement editButton() {
        return menuButton("Edit");
    }

    //TODO does it belong here?
    public WebElement cancelButton() {
        return findElement(By.xpath("//a[contains(text(),'Cancel')]"));
    }

    public WebElement webConnectorURL() {
        return findElement(By.cssSelector(".breakWord a"));
    }

    public String getIndexName() {
        return findElement(By.cssSelector("#indexPane h3")).getText();
    }

    public String getScheduleString() {
        return ElementUtil.ancestor(findElement(By.className("hp-schedule")),1).findElement(By.cssSelector("div:nth-child(1)")).getText();
    }

    public static class Factory implements ParametrizedFactory<WebDriver, ConnectionsDetailPage> {
        @Override
        public ConnectionsDetailPage create(WebDriver context) {
            new WebDriverWait(context, 30).until(ExpectedConditions.visibilityOfElementLocated(By.id("first-cable")));
            return new ConnectionsDetailPage(context);
        }
    }
}
