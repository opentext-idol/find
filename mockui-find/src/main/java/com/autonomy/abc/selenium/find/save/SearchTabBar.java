package com.autonomy.abc.selenium.find.save;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SearchTabBar {
    private final WebElement bar;

    public SearchTabBar(WebDriver driver) {
        bar = driver.findElement(By.className("search-tabs-list"));
    }

    public SearchTab currentTab() {
        return new SearchTab(bar.findElement(By.cssSelector(".search-tab.active")));
    }
}
