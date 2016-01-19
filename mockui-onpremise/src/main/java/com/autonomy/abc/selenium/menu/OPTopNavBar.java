package com.autonomy.abc.selenium.menu;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OPTopNavBar extends TopNavBar {
    public OPTopNavBar(WebDriver driver) {
        super(driver);
    }

    @Override
    public void logOut() {
        clickCog();
        new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOfElementLocated(By.className("navigation-logout"))).click();
    }

    private void clickCog(){
        findElement(By.className("hp-settings")).click();
        loadOrFadeWait();
    }

    private void clickDropdown(String page){
       findElement(By.xpath(".//a[contains(.,'"+page+"')]")).click();
    }

    public void goToAboutPage(){
        clickCog();
        clickDropdown("About");
    }

    public void goToSettingsPage(){
        clickCog();
        clickDropdown("Settings");
    }

    public void goToUsersPage(){
        clickCog();
        clickDropdown("Users");
    }
}
