package com.autonomy.abc.selenium.devconsole;

import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DevConsoleTopNavBar implements LoginService.LogoutHandler {
    // this uses the driver as the nav bar element goes
    // stale after each login/logout
    private final WebDriver driver;

    DevConsoleTopNavBar(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public void logOut() {
        openUserDropdown();
        clickLogoutButton();
        waitForLogout();
    }

    private void openUserDropdown() {
        driver.findElement(By.className("navigation-icon-user")).click();
    }

    private void clickLogoutButton() {
        driver.findElement(By.id("loginLogout")).click();
        Waits.loadOrFadeWait();
    }

    public WebElement loginButton(){
        return driver.findElement(By.id("loginLogout"));
    }

    private WebElement waitForLogout() {
        return new WebDriverWait(driver, 30)
                .withMessage("logging out of Dev Console")
                .until(ExpectedConditions.visibilityOf(loginButton()));
    }
}
