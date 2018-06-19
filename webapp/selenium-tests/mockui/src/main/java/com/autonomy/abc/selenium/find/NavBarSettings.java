package com.autonomy.abc.selenium.find;

import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class NavBarSettings implements LoginService.LogoutHandler {
    private final WebElement header;

    public NavBarSettings(final WebDriver driver) {
        header = driver.findElement(By.className("header"));
    }

    @Override
    public void logOut() {
        openSettings();
        header.findElement(By.className("navigation-logout")).click();
        Waits.loadOrFadeWait();
    }

    protected void openSettings() {
        header.findElement(By.className("hp-settings")).click();
    }

    protected void openSideBar() {
        header.findElement(By.className("hp-menu-skinny")).click();
    }

    protected WebElement header() {
        return header;
    }

    public String getCurrentUsername() {
        return header.findElement(By.className("navbar-username")).getText();
    }
}
