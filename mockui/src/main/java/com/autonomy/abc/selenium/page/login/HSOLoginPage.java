package com.autonomy.abc.selenium.page.login;

import com.hp.autonomy.frontend.selenium.login.HasLoggedIn;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HSOLoginPage extends LoginPage {
    private final WebDriver webDriver;

    public HSOLoginPage(final WebDriver driver, final HasLoggedIn hasLoggedIn) {
        super(driver, hasLoggedIn);
        this.webDriver = driver;
        waitForLoad();
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(webDriver, 40).until(ExpectedConditions.visibilityOfElementLocated(By.className("twitter")));
    }

}
