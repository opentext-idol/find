package com.autonomy.abc.selenium.icma;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public abstract class ICMAPageBase implements AppPage {
    private AppElement page;

    public ICMAPageBase(WebDriver driver) {
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

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(), 30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[ui-view]")));
    }
}
