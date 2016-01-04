package com.autonomy.abc.selenium.page.devconsole;

import com.autonomy.abc.selenium.find.Find;
import com.autonomy.abc.selenium.page.analytics.AnalyticsPage;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevConsole extends AppElement implements AppPage {

    private static Logger LOGGER = LoggerFactory.getLogger(DevConsole.class);

    public DevConsole(WebDriver driver) {
        super(new WebDriverWait(driver,30).until(ExpectedConditions.visibilityOfElementLocated(By.id("solution-overall-container"))), driver);
    }

    public AnalyticsPage launchSearchOptimizer(){
        try {
            findElement(By.xpath("//a[text()='Launch app']")).click();
            return new AnalyticsPage(getDriver());
        } catch (NoSuchElementException e) {
            LOGGER.error("Not signed in to Dev Console");
        }

        return null;
    }

    public Find launchFind(){
        try {
            findElement(By.className("hsod-find-button")).click();
            return new Find(getDriver());
        } catch (NoSuchElementException e) {
            LOGGER.error("Not signed in to Dev Console");
        }

        return null;
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(), 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("haven-splash-header")));
    }
}
