package com.autonomy.abc.selenium.find.save;

import com.autonomy.abc.selenium.find.application.IdolFind;
import com.autonomy.abc.selenium.find.application.IdolFindElementFactory;

public class SavedSearchService {
    private final IdolFindElementFactory elementFactory;

    public SavedSearchService(IdolFind find) {
        elementFactory = find.elementFactory();
    }

    public void saveCurrentAs(String searchName, SearchType type) {
        SearchOptionsBar options = elementFactory.getSearchOptionsBar();
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
        for (SearchTab tab : elementFactory.getSearchTabBar()) {
            tab.activate();
            deleteCurrentSearch();
        }
    }

    private void deleteCurrentSearch() {
        SearchOptionsBar options = elementFactory.getSearchOptionsBar();
        options.openDeleteModal();
        options.confirmDelete();
    }
}
