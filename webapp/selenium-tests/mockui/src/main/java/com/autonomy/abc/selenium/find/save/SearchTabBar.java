/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find.save;

import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SearchTabBar implements Iterable<SearchTab> {
    private final WebElement bar;
    private final WebDriver driver;

    public SearchTabBar(final WebDriver driver) {
        bar = driver.findElement(By.className("search-tabs-list"));
        this.driver = driver;
    }

    @Override
    public Iterator<SearchTab> iterator() {
        return tabs().iterator();
    }

    public List<SearchTab> tabs() {
        final List<SearchTab> tabs = new ArrayList<>();
        for(final WebElement tab : bar.findElements(By.className("search-tab"))) {
            tabs.add(new SearchTab(tab));
        }
        return tabs;
    }

    public List<String> savedTabTitles() {
        final List<String> tabTitles = new ArrayList<>();

        for(final SearchTab tab : tabs()) {
            if(!tab.getTitle().equals("New Search")) {
                tabTitles.add(tab.getTitle());
            }
        }
        return tabTitles;
    }

    public String getCurrentTabTitle() {
        return currentTab().getTitle();
    }

    public SearchTab currentTab() {
        return new SearchTab(bar.findElement(By.cssSelector(".search-tab.active")));
    }

    public void switchTo(final String title) {
        tab(title).activate();
    }

    public void switchTo(final int index) {
        tabFromIndex(index).activate();
    }

    public SearchTab tab(final String title) {
        for(final SearchTab tab : tabs()) {
            if(tab.getTitle().equals(title)) {
                return tab;
            }
        }
        throw new NoSuchElementException("could not find tab with title " + title);
    }

    public SearchTab tabFromIndex(final int index) {
        return tabs().get(index);
    }

    WebElement newTabButton() {
        return bar.findElement(By.className("start-new-search"));
    }

    public void newTab() {
        newTabButton().click();
    }

    public void hoverOnTab(final int i) {
        DriverUtil.hover(driver, tabFromIndex(i).getTab());
    }

    public void waitUntilTabGone(final String title) {
        new WebDriverWait(driver, 300).withMessage("deleted tab to disappear").until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(final WebDriver driver) {
                return bar.findElements(By.xpath(".//*[contains(normalize-space(),'" + title + "')]")).isEmpty();
            }
        });
    }

    public void waitUntilSavedSearchAppears() {
        new WebDriverWait(driver, 400).until(ExpectedConditions.presenceOfElementLocated
                (By.cssSelector(".search-tab:nth-child(3) .search-tab-title")));
    }
}
