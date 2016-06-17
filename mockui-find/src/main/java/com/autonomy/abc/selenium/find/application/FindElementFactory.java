package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindTopNavBar;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.preview.DetailedPreviewPage;
import com.autonomy.abc.selenium.find.preview.InlinePreview;
import com.autonomy.abc.selenium.find.results.FindResultsPage;
import com.autonomy.abc.selenium.find.results.RelatedConceptsPanel;
import com.autonomy.abc.selenium.find.results.SimilarDocumentsView;
import com.autonomy.abc.selenium.indexes.tree.IndexesTree;
import com.hp.autonomy.frontend.selenium.application.ElementFactoryBase;
import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

public abstract class FindElementFactory extends ElementFactoryBase {
    protected FindElementFactory(final WebDriver driver) {
        super(driver, null);
    }

    @Override
    public LoginService.LogoutHandler getLogoutHandler() {
        return getTopNavBar();
    }

    public FindPage getFindPage() {
        return new FindPage.Factory().create(getDriver());
    }

    public FindTopNavBar getTopNavBar() {
        return new FindTopNavBar(getDriver());
    }

    public FindResultsPage getResultsPage() {
        return new FindResultsPage(getDriver());
    }

    public RelatedConceptsPanel getRelatedConceptsPanel() {
        return new RelatedConceptsPanel(getDriver());
    }

    public FilterPanel getFilterPanel() {
        return new FilterPanel(new IndexesTree.Factory(), getDriver());
    }

    public SimilarDocumentsView getSimilarDocumentsView() {
        return new SimilarDocumentsView.Factory().create(getDriver());
    }

    public InlinePreview getInlinePreview() {
        return InlinePreview.make(getDriver());
    }

    public DetailedPreviewPage getDetailedPreview(){
        return new DetailedPreviewPage.Factory().create(getDriver());
    }

    @Override
    public <T extends AppPage> T loadPage(final Class<T> type) {
        throw new UnsupportedOperationException("loadPage does not make sense for a single page application");
    }
}
