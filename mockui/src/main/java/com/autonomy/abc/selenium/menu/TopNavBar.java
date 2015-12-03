package com.autonomy.abc.selenium.menu;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class TopNavBar extends AppElement {
    private WebElement searchbox;

    public TopNavBar(WebDriver driver) {
        super(new WebDriverWait(driver, 30).withMessage("top nav bar to be visible").until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".navbar-static-top.affix-element"))), driver);
    }

    public abstract void logOut();

    // use SideNavBar
    @Deprecated
    public void sideBarToggle() {
        getDriver().findElement(By.className("navbar-minimize")).click();
    }

    public NotificationsDropDown getNotifications(){
        return new NotificationsDropDown(getDriver());
    }

    public void notificationsDropdown(){
        getDriver().findElement(By.cssSelector("nav.affix-element .count-info")).click();
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
}
