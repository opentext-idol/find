package com.autonomy.abc.selenium.page;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public abstract class SAASPageBase {
    private AppElement page;

    public SAASPageBase(WebDriver driver) {
        page = new AppElement(new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[ui-view]"))), driver);
    }

    protected AppElement getPage() {
        return page;
    }

    protected WebDriver getDriver() {
        return page.getDriver();
    }

    protected WebElement findElement(By location) {
        return page.findElement(location);
    }

    protected List<WebElement> findElements(By location) {
        return page.findElements(location);
    }
}
