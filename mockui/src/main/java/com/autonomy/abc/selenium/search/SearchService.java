package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.config.Application;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.page.ElementFactory;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SearchService extends ServiceBase {
    SearchPage searchPage;

    public SearchService(Application application, ElementFactory elementFactory) {
        super(application, elementFactory);
    }

    public SearchPage search(final SearchQuery query) {
        getBody().getTopNavBar().search(query.getSearchTerm());
        setSearchPage(getElementFactory().getSearchPage());
        searchPage.filterBy(new AggregateSearchFilter(query.getFilters()));
        return searchPage;
    }

    public SearchPage search(String term) {
        return search(new SearchQuery(term));
    }

    //Is this service for the search page or for searching?
    public void deleteDocument(String documentTitle){
        ElementUtil.ancestor(searchPage.findElement(By.xpath("//a[text()=\"" + documentTitle + "\"]")), 2).findElement(By.className("delete")).click();
        new WebDriverWait(getDriver(), 10).until(GritterNotice.notificationContaining("was deleted"));
        searchPage.waitForSearchLoadIndicatorToDisappear();
    }

    private void setSearchPage(SearchPage searchPage){
        this.searchPage = searchPage;
    }
}
