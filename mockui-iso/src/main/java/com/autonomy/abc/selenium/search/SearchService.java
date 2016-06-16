package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.application.IsoElementFactory;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.query.AggregateQueryFilter;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.query.QueryService;
import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SearchService extends ServiceBase<IsoElementFactory> implements QueryService<SearchPage> {
    SearchPage searchPage;

    public SearchService(final IsoApplication<?> application) {
        super(application);
    }

    @Override
    public SearchPage search(final Query query) {
        getElementFactory().getTopNavBar().search(query.getTerm());
        this.searchPage = getElementFactory().getSearchPage();
        // TODO: get rid of this once IDOL test VM fixed
        new LanguageFilter(Language.ENGLISH).apply(searchPage);
        searchPage.filterBy(new AggregateQueryFilter(query.getFilters()));
        return searchPage;
    }

    @Override
    public SearchPage search(final String term) {
        return search(new Query(term));
    }

    //Is this service for the search page or for searching?
    public void deleteDocument(final String documentTitle){
        ElementUtil.ancestor(searchPage.findElement(By.linkText(documentTitle)), 2).findElement(By.className("delete")).click();
        new WebDriverWait(getDriver(), 10).until(GritterNotice.notificationContaining("was deleted"));
        searchPage.waitForSearchLoadIndicatorToDisappear();
    }
}
