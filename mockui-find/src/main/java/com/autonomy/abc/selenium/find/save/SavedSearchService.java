package com.autonomy.abc.selenium.find.save;

import com.autonomy.abc.selenium.find.application.IdolFind;
import com.autonomy.abc.selenium.find.application.IdolFindElementFactory;

public class SavedSearchService {
    private final IdolFindElementFactory elementFactory;

    public SavedSearchService(final IdolFind find) {
        elementFactory = find.elementFactory();
    }

    public void saveCurrentAs(final String searchName, final SearchType type) {
        final SearchOptionsBar options = elementFactory.getSearchOptionsBar();
        options.saveAsButton().click();
        options.searchTitleInput().setValue(searchName);
        options.searchTypeButton(type).click();
        options.confirmSave();
    }

    public void openNewTab() {
        elementFactory.getSearchTabBar().newTabButton().click();
        elementFactory.getResultsPage().waitForResultsToLoad();
    }

    public void deleteAll() {
        for (final SearchTab tab : elementFactory.getSearchTabBar()) {
            tab.activate();
            deleteCurrentSearch();
        }
    }

    private void deleteCurrentSearch() {
        final SearchOptionsBar options = elementFactory.getSearchOptionsBar();
        options.openDeleteModal();
        options.confirmDelete();
    }
}
