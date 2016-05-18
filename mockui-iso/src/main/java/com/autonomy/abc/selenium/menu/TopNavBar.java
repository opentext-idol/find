package com.autonomy.abc.selenium.menu;

import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Locator;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class TopNavBar extends AppElement implements LoginService.LogoutHandler {
    private WebElement searchbox;

    public TopNavBar(WebDriver driver) {
        super(new WebDriverWait(driver, 30).withMessage("top nav bar to be visible").until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".navbar-static-top.affix-element"))), driver);
    }

    public NotificationsDropDown getNotifications(){
        return new NotificationsDropDown(getDriver());
    }

    public void notificationsDropdown(){
        getDriver().findElement(By.cssSelector("nav.affix-element .count-info")).click();
        Waits.loadOrFadeWait();
    }

    public boolean notificationsDropdownVisible(){
        return findElement(By.className("notification-list")).isDisplayed();
    }

    private void settingsDropdown(){
        ElementUtil.ancestor(findElement(By.className("hp-settings")),1).click();
        Waits.loadOrFadeWait();
    }

    public boolean settingsDropdownVisible(){
        return findElement(By.className("navigation-logout")).isDisplayed();
    }

    public void closeNotifications() {
        if(notificationsDropdownVisible()){
            notificationsDropdown();
        }
    }

    public void openNotifications() {
        if(!notificationsDropdownVisible()){
            notificationsDropdown();
        }
    }

    public void closeSettings() {
        if(settingsDropdownVisible()){
            settingsDropdown();
        }
    }

    public void openSettings() {
        if(!settingsDropdownVisible()){
            settingsDropdown();
        }
    }

    public void search(String searchTerm) {
        WebElement topSearch = searchBox();

        topSearch.clear();
        topSearch.sendKeys(searchTerm);
        topSearch.sendKeys(Keys.ENTER);
    }

    public WebElement searchBox() {
        if (searchbox == null) {
            searchbox = findElement(By.cssSelector("[name='top-search']"));
        }
        return searchbox;
    }

    public String getSearchBarText() {
        return searchBox().getAttribute("value");
    }




    public void clickAnywhereButNotifications(){
        findElement(By.className("page-heading")).click();
    }
}
