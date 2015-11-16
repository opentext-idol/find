package com.autonomy.abc.selenium.page.connections.wizard;

import com.autonomy.abc.selenium.page.SAASPageBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class ConnectorIndexStepTab extends SAASPageBase {

    private ConnectorIndexStepTab(WebDriver driver){
        super(driver);
    }

    public static ConnectorIndexStepTab make(WebDriver driver) {
        return new ConnectorIndexStepTab(driver);
    }

    public WebElement selectIndexButton(){
        return findElement(By.tagName("button"));
    }

    public WebElement getIndexSearchBox(){
        return getDriver().findElement(By.className("chosen-search"));
    }

    public List<WebElement> getExistingIndexes() {
        return getDriver().findElements(By.cssSelector(".chosen-results li"));
    }
}
