package com.autonomy.abc.selenium.page.devconsole;

import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.page.analytics.AnalyticsPage;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevConsoleSearchPage extends AppElement implements AppPage {

    private static Logger LOGGER = LoggerFactory.getLogger(DevConsoleSearchPage.class);

    public DevConsoleSearchPage(WebDriver driver) {
        super(new WebDriverWait(driver,30).until(ExpectedConditions.visibilityOfElementLocated(By.id("solution-overall-container"))), driver);
    }

    public AnalyticsPage launchSearchOptimizer(){
        try {
            findElement(By.xpath("//a[text()='Launch app']")).click();
            return new AnalyticsPage.Factory().create(getDriver());
        } catch (NoSuchElementException e) {
            LOGGER.error("Not signed in to Dev Console");
        }

        return null;
    }

    public FindPage launchFind(){
        try {
            findElement(By.className("hsod-find-button")).click();
            return new FindPage(getDriver());
        } catch (NoSuchElementException e) {
            LOGGER.error("Not signed in to Dev Console");
        }

        return null;
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(), 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("haven-splash-header")));
    }

    public void clickLogInButton() {
        try {
            getDriver().findElement(By.id("loginLogout")).click();
        } catch (NoSuchElementException | ElementNotVisibleException e) {
            LOGGER.error("Already logged in");
        }
    }
}
