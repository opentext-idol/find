package com.autonomy.abc.selenium.menu;

import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class TopNavBar extends AppElement {
    private WebElement searchbox;

    public TopNavBar(WebDriver driver) {
        super(new WebDriverWait(driver,30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".navbar-static-top:not(.affix-clone)"))), driver);
    }

    public abstract NotificationsDropDown getNotifications();
    public abstract void notificationsDropdown();

    public void sideBarToggle() {
        getDriver().findElement(By.className("navbar-minimize")).click();
    }


    public void search(String searchTerm) {
//        WebElement topSearch = new WebDriverWait(getDriver(),10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[name='top-search']")));
        WebElement topSearch = searchBox();

        topSearch.clear();
        topSearch.sendKeys(searchTerm);
        topSearch.sendKeys(Keys.RETURN);

        //new AppBody(getDriver()).getSearchPage().waitForSearchLoadIndicatorToDisappear();
    }

    public WebElement searchBox() {
        if (searchbox == null) {
            searchbox = findElement(By.cssSelector("[name='top-search']:not(.affix-clone)"));
        }
        return searchbox;
    }

    public String getSearchBarText() {
        return searchBox().getAttribute("value");
    }
}
