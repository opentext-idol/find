package com.autonomy.abc.selenium.page.login;

import com.hp.autonomy.frontend.selenium.login.LoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OPLoginPage extends LoginPage {

    private final WebDriver driver;

    public OPLoginPage(final WebDriver driver) {
        super(driver, new AbcHasLoggedIn(driver));

        this.driver = driver;

        waitForLoad();
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.tagName("button")));
    }
}
