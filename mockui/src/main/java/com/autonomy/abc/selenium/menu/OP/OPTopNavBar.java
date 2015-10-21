package com.autonomy.abc.selenium.menu.OP;

import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.menu.TopNavBar;
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
        findElement(By.className("fa-cog")).click();
    }

    private void clickDropdown(String page){
       findElement(By.xpath(".//*[contains(text(),'"+page+"')]")).click();
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
