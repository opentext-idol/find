package com.autonomy.abc.selenium.iso;

import com.autonomy.abc.selenium.menu.TopNavBar;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

class OPISOTopNavBar extends TopNavBar {
    OPISOTopNavBar(WebDriver driver) {
        super(driver);
    }

    @Override
    public void logOut() {
        clickCog();
        new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOfElementLocated(By.className("navigation-logout"))).click();
    }

    private void clickCog(){
        findElement(By.className("hp-settings")).click();
        Waits.loadOrFadeWait();
    }

    void switchPage(TabId tab) {
        clickCog();
        findElement(tab.locator).click();
    }

    enum TabId {
        ABOUT("About"),
        SETTINGS("Settings"),
        USERS("Users");

        private final By locator;

        TabId(String linkText) {
            locator = By.linkText(linkText);
        }
    }
}
