package com.autonomy.abc.selenium.menu;

import com.autonomy.abc.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public enum NavBarTabId {
    ANALYTICS("Analytics"),
    SEARCH("Search"),
    CONNECTIONS("Connections"),
    INDEXES("Indexes"),
    PROMOTIONS("Promotions"),
    KEYWORDS("Keywords"),
    GETTING_STARTED("Getting Started"),
    USER_MGMT("User Management"),
    DEVELOPERS("Haven OnDemand Developers"){
        @Override
        public void click(WebDriver driver) {
            nestedClick(driver, USER_MGMT);
        }
    },
    USERS("Haven Search OnDemand Users"){
        @Override
        public void click(WebDriver driver) {
            nestedClick(driver, USER_MGMT);
        }
    },
    OVERVIEW("Overview");

    private final String tabName;

    NavBarTabId(final String name) {
        tabName = name;
    }

    public String toString() {
        return tabName;
    }

    public void click(WebDriver driver) {
        defaultClick(driver);
    }

    protected void defaultClick(WebDriver driver){
        WebElement element = new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul//li//a[contains(.,'" + toString() + "')]")));
        try {
            element.click();
        } catch (Exception e) {
            ElementUtil.ancestor(element, 1).click();
        }
    }

    protected void nestedClick(WebDriver driver, NavBarTabId parent){
        if(!this.isDisplayed(driver)){
            parent.click(driver);
        }

        this.defaultClick(driver);
    }

    protected boolean isDisplayed(WebDriver driver){
        return driver.findElement(By.xpath("//a[contains(.,'" + this.tabName + "')]")).isDisplayed();
    }
}
