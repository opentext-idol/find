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

package com.autonomy.abc.selenium.find.save;

import com.autonomy.abc.selenium.find.application.BIIdolFind;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.comparison.ComparisonModal;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class SavedSearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SavedSearchService.class);
    private final BIIdolFindElementFactory elementFactory;
    private final Set<String> createdSearches = new HashSet<>();

    public SavedSearchService(final BIIdolFind find) {
        elementFactory = find.elementFactory();
    }

    public void saveCurrentAs(final String searchName, final SearchType type) {
        Waits.loadOrFadeWait();
        nameSavedSearch(searchName, type).confirmSave();
        createdSearches.add(searchName);
    }

    public void renameCurrentAs(final String newSearchName) {
        final SearchOptionsBar optionsBar = elementFactory.getSearchOptionsBar();
        Waits.loadOrFadeWait();
        optionsBar.renameButton().click();
        final FormInput formInput = optionsBar.searchTitleInput();
        createdSearches.remove(formInput.getValue());
        formInput.setValue(newSearchName);
        optionsBar.confirmSave();
        createdSearches.add(newSearchName);
    }

    public SearchOptionsBar nameSavedSearch(final String searchName, final SearchType type) {
        final SearchOptionsBar options = elementFactory.getSearchOptionsBar();
        options.saveAsButton(type).click();
        options.searchTitleInput().setValue(searchName);
        return options;
    }

    public void openNewTab() {
        elementFactory.getSearchTabBar().newTabButton().click();
        elementFactory.getTopicMap().waitForMapLoaded();
        elementFactory.getSearchTabBar().hoverOnTab(0);
    }

    public void waitForSomeTabsAndDelete() {
        try {
            elementFactory.getSearchTabBar().waitUntilSavedSearchAppears();
            deleteAll();
        } catch(final TimeoutException ignored) {
            LOGGER.info("Timed out waiting for a Saved Search to appear");
            deleteAll();
        }
    }

    //TODO: Still not deleting the tabs but really a problem with the app's slow deletion
    public void deleteAll() {
        final SearchTabBar tabBar = elementFactory.getSearchTabBar();
        tabBar.savedTabTitles().stream().filter(createdSearches::contains).forEach(title -> {
            elementFactory.getSearchTabBar().tab(title).activate();
            elementFactory.getFindPage().waitForLoad();
            deleteCurrentSearch();
            tabBar.waitUntilTabGone(title);
            createdSearches.remove(title);
        });
    }

    public void deleteCurrentSearch() {
        final SearchOptionsBar options = elementFactory.getSearchOptionsBar();
        options.delete();
    }

    public void compareCurrentWith(final String savedSearchName) {
        final ComparisonModal modal = elementFactory.getFindPage().openCompareModal();
        modal.select(savedSearchName);
        modal.compareButton().click();
        modal.waitForComparisonToLoad();
    }

    /**
     * Click the reset button for the current query, then click to confirm in the modal.
     */
    public void resetCurrentQuery() {
        final SearchOptionsBar options = elementFactory.getSearchOptionsBar();
        options.openResetModal();
        options.confirmModalOperation();
        Waits.loadOrFadeWait();
    }
}
