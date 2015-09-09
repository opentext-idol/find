package com.autonomy.abc.selenium.page.login;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.page.AppPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class LoginPage extends AppElement implements AppPage {
    public LoginPage(WebElement element, WebDriver driver) {
        super(element, driver);
    }

    public abstract void loginWith(AuthProvider authProvider);

    public boolean hasLoggedIn() {
        try {
            new WebDriverWait(getDriver(), 30).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("navbar-static-top")));
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }
}
