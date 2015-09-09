package com.autonomy.abc.selenium.menu;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.page.AppBody;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class TopNavBar extends AppElement {
    public TopNavBar(WebDriver driver) {
        super(new WebDriverWait(driver,30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".navbar-static-top"))), driver);
    }

    public abstract NotificationsDropDown getNotifications();
    public abstract void notificationsDropdown();

    public void sideBarToggle() {
        getDriver().findElement(By.className("navbar-minimize")).click();
    }

    public void search(String searchTerm) {
        WebElement topSearch = new WebDriverWait(getDriver(),10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[name='top-search']")));

        topSearch.clear();
        topSearch.sendKeys(searchTerm);
        topSearch.sendKeys(Keys.RETURN);

        //new AppBody(getDriver()).getSearchPage().waitForSearchLoadIndicatorToDisappear();
    }

    public String getSearchBarText() {
        return findElement(By.cssSelector("[name='top-search']")).getAttribute("value");
    }
}
