/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.find.bi.MapView;
import com.autonomy.abc.selenium.find.bi.SunburstView;
import com.autonomy.abc.selenium.find.bi.TableView;
import com.autonomy.abc.selenium.find.bi.TopicMapView;
import com.autonomy.abc.selenium.find.bi.TrendingView;
import com.autonomy.abc.selenium.find.comparison.ComparisonModal;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.filters.IdolFilterPanel;
import com.autonomy.abc.selenium.find.numericWidgets.MainNumericWidget;
import com.autonomy.abc.selenium.find.results.ListView;
import com.autonomy.abc.selenium.indexes.IdolDatabaseTree;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class IdolFindPage extends FindPage {

    private IdolFindPage(final WebDriver driver) {
        super(driver);
    }

    //FILTERS
    @Override
    protected FilterPanel filters() {
        return new IdolFilterPanel(new IdolDatabaseTree.Factory(), getDriver());
    }

    //COMPARISON - TODO: should this be part of navbar?
    public ComparisonModal openCompareModal() {
        compareButton().click();
        return ComparisonModal.make(getDriver());
    }

    public void goBackToSearch() {
        final List<WebElement> backButtons = findElements(By.cssSelector(".comparison-view-back-button"));
        if(backButtons.size() > 0) { backButtons.get(0).click(); } else {
            LOGGER.info("Could not locate back button; maybe already on main view.");
        }
        waitForLoad();
    }

    public WebElement compareButton() {
        return currentTab().findElement(By.className("compare-modal-button"));
    }

    public boolean resultsComparisonVisible() {
        return findElement(By.cssSelector(".comparison-view")).isDisplayed();
    }

    //WAITS
    public void waitUntilSearchTabsLoaded() {
        new WebDriverWait(getDriver(), 10)
                .until(ExpectedConditions.elementToBeClickable(compareButton()));
    }

    public void waitUntilSaveButtonsActive() {
        new WebDriverWait(getDriver(), 30L).withMessage("Buttons should become active")
                .until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".service-view-container:not(.hide) .save-button:not(.disabled)")));
    }

    public void waitUntilSavePossible() {
        new WebDriverWait(getDriver(), 30L).withMessage("Buttons should become active")
                .until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(".service-view-container:not(.hide) .save-button:not(.disabled)")));
    }

    //NUMERIC GRAPH
    public MainNumericWidget mainGraph() {return new MainNumericWidget(getDriver());}

    public boolean mainGraphDisplayed() {
        return !findElements(By.className("middle-container-time-bar")).isEmpty();
    }

    //TODO:
    //Should this even by in FindPage?
    //Probably need waits after the goToTab but have to make sure it waits for anything that is acceptable including error message

    //VIEW TABS
    private void goToTab(final ViewTab tab) {
        Container.currentTabContents(getDriver()).findElement(
                By.cssSelector("li a[data-tab-id='" + tab.css() + "']")).click();
    }

    @Override
    public ListView goToListView() {
        goToTab(ViewTab.LIST);
        return new ListView(getDriver());
    }

    public TopicMapView goToTopicMap() {
        goToTab(ViewTab.TOPIC_MAP);
        return new TopicMapView(getDriver());
    }

    public MapView goToMap() {
        goToTab(ViewTab.MAP);
        return new MapView(getDriver());
    }

    public TrendingView goToTrending() {
        goToTab(ViewTab.TRENDING);
        return new TrendingView(getDriver());
    }

    public SunburstView goToSunburst() {
        goToTab(ViewTab.SUNBURST);
        return new SunburstView(getDriver());
    }

    public TableView goToTable() {
        goToTab(ViewTab.TABLE);

        new WebDriverWait(getDriver(), 15)
                .withMessage("Table or message never appeared")
                .until(new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(final WebDriver driver) {
                        return !findElements(By.cssSelector("table.dataTable")).isEmpty() ||
                                currentView().findElement(
                                        By.cssSelector(".parametric-view-message .well div")).isDisplayed();
                    }
                });

        return new TableView(getDriver());
    }

    private WebElement currentView() {
        return findElement(By.cssSelector(".tab-pane.active"));
    }

    public static class Factory implements ParametrizedFactory<WebDriver, IdolFindPage> {
        @Override
        public IdolFindPage create(final WebDriver context) {
            return new IdolFindPage(context);
        }
    }
}
