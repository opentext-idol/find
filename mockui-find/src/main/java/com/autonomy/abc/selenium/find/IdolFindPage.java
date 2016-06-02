package com.autonomy.abc.selenium.find;

import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.indexes.IdolDatabaseTree;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class IdolFindPage extends FindPage {

    private IdolFindPage(WebDriver driver) {
        super(driver);
    }

    @Override
    protected FilterPanel filters() {
        return new FilterPanel(new IdolDatabaseTree.Factory(), getDriver());
    }

    public void filterResults(String term) {
        filters().filterResults(term);
    }

    public void clearFilter() {
        filters().clearFilter();
    }

    public boolean parametricFilterExists(String filter) {
        return filters().parametricFilterExists(filter);
    }

    public boolean filterVisible(String filter) {
        return filters().filterVisible(filter);
    }

    public boolean noneMatchingMessageVisible() {
        return filters().noneMatchingMessageVisible();
    }

    public List<WebElement> getCurrentFilters() {
        return filters().getCurrentFilters();
    }

    public List<String> findFilterString(String targetFilter, List<WebElement> allFilters) {
        return filters().findFilterString(targetFilter, allFilters);
    }

    //toggling see more
    public void showFilters() {
        filters().showFilters();
    }

    public void expandFiltersFully() {
        filters().expandFiltersFully();
    }

    public void collapseAll() {
        filters().collapseAll();
    }

    public static class Factory implements ParametrizedFactory<WebDriver, IdolFindPage> {
        public IdolFindPage create(WebDriver context) {
            return new IdolFindPage(context);
        }
    }
}

