package com.autonomy.abc.selenium.find.save;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class SearchTab {
    private final WebElement tab;

    SearchTab(WebElement tab) {
        this.tab = tab;
    }

    public String getTitle() {
        return tab.findElement(By.className("search-tab-title")).getText();
    }

    public boolean isNew() {
        return tab.findElement(By.cssSelector(".search-tab-title .hp-new")).isDisplayed();
    }

    public void activate() {
        tab.click();
    }

    public SearchType getType() {
        return SearchType.valueOf(tab.findElement(By.className("search-type")).getText());
    }
}
