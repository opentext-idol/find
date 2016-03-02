package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.navigation.SOElementFactory;
import com.autonomy.abc.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SearchService extends ServiceBase<SOElementFactory> {
    SearchPage searchPage;

    public SearchService(SearchOptimizerApplication<?> application) {
        super(application);
    }

    public SearchPage search(final SearchQuery query) {
        getElementFactory().getTopNavBar().search(query.getSearchTerm());
        setSearchPage(getElementFactory().getSearchPage());
        searchPage.filterBy(new AggregateSearchFilter(query.getFilters()));
        return searchPage;
    }

    public SearchPage search(String term) {
        return search(new SearchQuery(term));
    }

    //Is this service for the search page or for searching?
    public void deleteDocument(String documentTitle){
        ElementUtil.ancestor(searchPage.findElement(By.linkText(documentTitle)), 2).findElement(By.className("delete")).click();
        new WebDriverWait(getDriver(), 10).until(GritterNotice.notificationContaining("was deleted"));
        searchPage.waitForSearchLoadIndicatorToDisappear();
    }

    private void setSearchPage(SearchPage searchPage){
        this.searchPage = searchPage;
    }
}
