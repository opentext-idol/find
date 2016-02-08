package com.autonomy.abc.selenium.navigation;

import com.autonomy.abc.selenium.page.login.SSOFailureException;
import com.hp.autonomy.frontend.selenium.login.HasLoggedIn;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FindHasLoggedIn implements HasLoggedIn {
    private WebDriver driver;

    public FindHasLoggedIn(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public boolean hasLoggedIn() {
        try {
            new WebDriverWait(driver,30)
                    .withMessage("logging in to Find")
                    .until(ExpectedConditions.visibilityOfElementLocated(By.className("container-fluid")));
            return true;
        } catch (Exception e) {
            if (signedInTextVisible()) {
                throw new SSOFailureException();
            }
        }
        return false;
    }

    private boolean signedInTextVisible() {
        return driver.findElements(By.xpath("//*[text()='Signed in']")).size() > 0;
    }
}
