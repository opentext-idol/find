/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find.filters;

import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.util.CssUtil;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Locator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ParametricFilterModal extends ModalView implements Iterable<ParametricModalCheckbox> {
    //number of segments visible in sunburst - calculable from ParametricFilterModal
    private static final int VISIBLE_SEGMENTS = 20;
    private static final Logger LOGGER = LoggerFactory.getLogger(ParametricFilterModal.class);

    private final WebDriver driver;

    ParametricFilterModal(final WebElement element, final WebDriver webDriver) {
        super(element, webDriver);
        driver = webDriver;
    }

    public static ParametricFilterModal getParametricModal(final WebDriver driver) {
        final WebElement $el = new WebDriverWait(driver, 40)
                .withMessage("Parametric filter modal did not open within 30 seconds ")
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".fixed-height-modal")));
        return new ParametricFilterModal($el, driver);
    }

    private static void waitUntilModalGone(final WebDriver driver) {
        new WebDriverWait(driver, 30).until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".fixed-height-modal")));
    }

    private static boolean isBigEnough(final int thisCount, final int totalResults) {
        return (double)thisCount / totalResults >= 0.05;
    }

    public boolean loadingIndicatorPresent() {
        return !findElements(By.cssSelector(".loading-spinner")).isEmpty();
    }

    public void waitForLoad() {
        new WebDriverWait(getDriver(), 15).withMessage("loading indicator to disappear").until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(final WebDriver driver) {
                return !loadingIndicatorPresent();
            }
        });
    }

    public void cancel() {
        findElement(new Locator()
                            .withTagName("button")
                            .containingText("Cancel")
        ).click();

        waitUntilModalGone(driver);
    }

    public void apply() {
        findElement(new Locator()
                            .withTagName("button")
                            .containingText("Apply")
        ).click();
        waitUntilModalGone(driver);
    }

    public List<WebElement> tabs() {
        return findElements(By.cssSelector(".category-title"));
    }

    public String activeTabName() {
        return findElement(By.cssSelector("li.category-title.active span")).getText();
    }

    //input 0-indexed like panel
    public void goToTab(final int tabNumber) {
        findElement(By.cssSelector(".category-title:nth-child(" + CssUtil.cssifyIndex(tabNumber) + ") a")).click();
    }

    public WebElement activePane() {
        return findElement(By.cssSelector(".tab-pane.active"));
    }

    public List<WebElement> activePaneFilterList() {
        scrollDownInsideModal();
        return activePane().findElements(By.cssSelector(".checkbox.parametric-field-label"));
    }

    private void scrollDownInsideModal() {
        final WebElement pane = activePane();
        pane.click();
        pane.click();

        final int limit = 20;
        int i = 0;
        int numberOfCategories = 0;
        int previousNumber = -1;
        int twicePreviousNumber = -1;

        while(i < limit && filtersWithNoResults(pane) < 1 && twicePreviousNumber < numberOfCategories) {
            DriverUtil.scrollToBottom(driver);
            twicePreviousNumber = previousNumber;
            previousNumber = numberOfCategories;
            numberOfCategories = pane.findElements(By.cssSelector(".checkbox.parametric-field-label")).size();
            i++;
        }
        if(i >= limit) {
            LOGGER.info("Loop reached limit of " + limit + " , " +
                                "but if there is v large number of categories the limit may need to be higher");
        }
    }

    private int filtersWithNoResults(final WebElement pane) {
        return pane.findElements(
                By.xpath(
                        ".//*[contains(@class, 'checkbox') and " +
                                "contains(@class, 'parametric-field-label') and contains(.,'(0)')]"
                )).size();
    }

    public List<WebElement> allFilters() {
        return findElements(By.cssSelector(".checkbox.parametric-field-label"));
    }

    public String checkCheckBoxInActivePane(final int i) {
        final ParametricModalCheckbox box = new ParametricModalCheckbox(activePaneFilterList().get(i));
        box.check();
        return box.getName();
    }

    public List<String> checkedFiltersAllPanes() {
        final List<String> allCheckedFilters = new ArrayList<>();
        for(final WebElement tab : tabs()) {
            tab.click();
            allCheckedFilters.addAll(ElementUtil.getTexts(activePane().findElements(
                    By.cssSelector(".icheckbox-hp.checked + span")))
            );
        }
        return allCheckedFilters;
    }

    @Override
    public Iterator<ParametricModalCheckbox> iterator() {
        return values().iterator();
    }

    public List<ParametricModalCheckbox> values() {
        return activePaneFilterList().stream().map(ParametricModalCheckbox::new).collect(Collectors.toList());
    }

    private int totalResultsInPane(final Iterable<ParametricModalCheckbox> checkboxes) {
        int totalResults = 0;

        for(final ParametricModalCheckbox checkbox : checkboxes) {
            if(checkbox.getResultsCount() != 0) {
                totalResults += checkbox.getResultsCount();
            } else {
                break;
            }
        }
        return totalResults;
    }

    public int filtersWithResultsForCurrentSearch() {
        final Iterable<ParametricModalCheckbox> checkboxes = values();
        int count = 0;

        for(final ParametricModalCheckbox checkbox : checkboxes) {
            if(checkbox.getResultsCount() != 0) {
                count++;
            } else {
                break;
            }
        }
        cancel();
        return count;
    }

    public List<String> expectedParametricValues() {
        final Iterable<ParametricModalCheckbox> checkboxes = values();
        final List<String> expected = new ArrayList<>();

        final int totalResults = totalResultsInPane(checkboxes);

        for(final ParametricModalCheckbox checkbox : checkboxes) {
            final int thisCount = checkbox.getResultsCount();
            if((expected.size() < VISIBLE_SEGMENTS || isBigEnough(thisCount, totalResults)) && thisCount != 0) {
                expected.add(checkbox.getName());
            } else {
                break;
            }
        }
        return expected;
    }
}
