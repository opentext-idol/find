/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find.save;

import com.autonomy.abc.selenium.find.Container;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SavedSearchPanel {
    private final WebElement panel;

    public SavedSearchPanel(final WebDriver driver) {
        //might need to use the whole current tab thing?
        panel = Container.LEFT.findUsing(driver);
    }

    public String queryText() {
        return panel.findElement(By.xpath(".//p[contains(text(),'Query Text')]/following-sibling::p ")).getText();
    }

    public int resultCount() {
        return Integer.parseInt(panel.findElement(
                By.xpath(".//p[contains(text(),'Result Count')]/following-sibling::p ")).getText());
    }

    public String getFirstSelectedFilterOfType(final String filterType) {
        return savedFilterParent(filterType).findElement(By.cssSelector("p:nth-child(2)")).getText();
    }

    private WebElement savedFilterParent(final String filterType) {
        return ElementUtil.getParent(panel.findElement(By.xpath(".//p[contains(text(),'" + filterType + "')]")));
    }
}
