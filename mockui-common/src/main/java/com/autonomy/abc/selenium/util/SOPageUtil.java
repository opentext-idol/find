package com.autonomy.abc.selenium.util;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SOPageUtil {

    public static String getPageTitle(WebDriver driver) {
        return driver.findElement(By.cssSelector(".page-heading .heading")).getText();
    }

    public static AppElement getWrapperContent(WebDriver driver) {
        waitForLoadingIndicatorToDisappear(driver);
        return new AppElement(driver.findElement(By.className("wrapper-content")), driver);
    }

    public static void waitForLoadingIndicatorToDisappear(WebDriver driver) {
        new WebDriverWait(driver,30).until(ExpectedConditions.invisibilityOfElementLocated(By.className("loadingIcon")));
    }
}
