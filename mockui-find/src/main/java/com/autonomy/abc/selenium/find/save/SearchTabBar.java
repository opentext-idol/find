package com.autonomy.abc.selenium.find.save;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SearchTabBar implements Iterable<SearchTab> {
    private final WebElement bar;

    public SearchTabBar(final WebDriver driver) {
        bar = driver.findElement(By.className("search-tabs-list"));
    }

    @Override
    public Iterator<SearchTab> iterator() {
        return tabs().iterator();
    }

    public List<SearchTab> tabs() {
        final List<SearchTab> tabs = new ArrayList<>();
        for (final WebElement tab : bar.findElements(By.className("search-tab"))) {
            tabs.add(new SearchTab(tab));
        }
        return tabs;
    }

    public SearchTab currentTab() {
        return new SearchTab(bar.findElement(By.cssSelector(".search-tab.active")));
    }

    public void switchTo(final String title) {
        tab(title).activate();
    }

    public SearchTab tab(final String title) {
        for (final SearchTab tab : tabs()) {
            if (tab.getTitle().equals(title)) {
                return tab;
            }
        }
        throw new NoSuchElementException("could not find tab with title " + title);
    }

    WebElement newTabButton() {
        return bar.findElement(By.className("start-new-search"));
    }
}
