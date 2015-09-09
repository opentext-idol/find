package com.autonomy.abc.selenium.page.login;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.page.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HSOLoginPage extends LoginPage implements AppPage {
    private static Logger logger = LoggerFactory.getLogger(HSOLoginPage.class);

    public HSOLoginPage(final WebDriver driver) {
        super(new WebDriverWait(driver, 40).until(ExpectedConditions.visibilityOfElementLocated(By.className("login-body"))), driver);
        waitForLoad();
    }

    public void loginWith(final AuthProvider provider) {
        provider.login(this);
        if (!hasLoggedIn()) {
            logger.warn("Initial login attempt failed, trying again");
            provider.login(this);
        }
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(), 40).until(ExpectedConditions.visibilityOfElementLocated(By.className("apikey")));
    }
}
