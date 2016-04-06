package com.autonomy.abc.selenium.hsod;

import com.autonomy.abc.selenium.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.application.SOElementFactory;
import com.autonomy.abc.selenium.application.SOPageBase;
import com.autonomy.abc.selenium.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.connections.ConnectionsPage;
import com.autonomy.abc.selenium.connections.NewConnectionPage;
import com.autonomy.abc.selenium.icma.GettingStartedPage;
import com.autonomy.abc.selenium.icma.ICMAPageBase;
import com.autonomy.abc.selenium.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.indexes.IndexesPage;
import com.autonomy.abc.selenium.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.HSODCreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.HSODKeywordsPage;
import com.autonomy.abc.selenium.keywords.KeywordsPage;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.promotions.HSODCreateNewPromotionsPage;
import com.autonomy.abc.selenium.promotions.HSODPromotionsPage;
import com.autonomy.abc.selenium.search.HSODSearchPage;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.users.HSODDevelopersPage;
import com.autonomy.abc.selenium.users.HSODUsersPage;
import com.hp.autonomy.frontend.selenium.application.PageMapper;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import org.openqa.selenium.WebDriver;

public class HSODElementFactory extends SOElementFactory {
    private final IsoHsodElementFactory delegate;

    HSODElementFactory(final WebDriver driver) {
        super(driver, new PageMapper<>(HSODPage.class));
        delegate = new IsoHsodElementFactory(driver);
    }

    @Override
    public TopNavBar getTopNavBar() {
        return delegate.getTopNavBar();
    }

    @Override
    public HSODPromotionsPage getPromotionsPage() {
        return loadIsoPage(HSODPromotionsPage.class);
    }

    @Override
    public LoginPage getLoginPage() {
        return delegate.loadPage(HSOLoginPage.class);
    }

    @Override
    public KeywordsPage getKeywordsPage() {
        return loadIsoPage(HSODKeywordsPage.class);
    }

    @Override
    public CreateNewKeywordsPage getCreateNewKeywordsPage() {
        return loadIsoPage(HSODCreateNewKeywordsPage.class);
    }

    @Override
    public SearchPage getSearchPage() {
        return loadIsoPage(HSODSearchPage.class);
    }

    @Override
    public HSODCreateNewPromotionsPage getCreateNewPromotionsPage() {
        return loadIsoPage(HSODCreateNewPromotionsPage.class);
    }

    @Override
    public HSODUsersPage getUsersPage(){
        return loadIsoPage(HSODUsersPage.class);
    }

    public ConnectionsPage getConnectionsPage() {
        return loadIcmaPage(ConnectionsPage.class);
    }

    public NewConnectionPage getNewConnectionPage() {
        return loadIcmaPage(NewConnectionPage.class);
    }

    public ConnectionsDetailPage getConnectionsDetailPage() {
        return loadIcmaPage(ConnectionsDetailPage.class);
    }

    public AnalyticsPage getAnalyticsPage() {
        return loadPage(AnalyticsPage.class);
    }

    public IndexesPage getIndexesPage() {
        return loadIcmaPage(IndexesPage.class);
    }

    public CreateNewIndexPage getCreateNewIndexPage() {
        return loadIcmaPage(CreateNewIndexPage.class);
    }

    public IndexesDetailPage getIndexesDetailPage() {
        return loadIcmaPage(IndexesDetailPage.class);
    }

    public GettingStartedPage getGettingStartedPage() {
        return loadIcmaPage(GettingStartedPage.class);
    }

    public HSODDevelopersPage getDevsPage() {
        return loadIsoPage(HSODDevelopersPage.class);
    }

    private <T extends ICMAPageBase> T loadIcmaPage(Class<T> pageType) {
        return loadPage(pageType);
    }

    private <T extends SOPageBase> T loadIsoPage(Class<T> pageType) {
        return delegate.loadPage(pageType);
    }

    protected static class SideNavStrategy extends SOElementFactory.SideNavStrategy {
        protected SideNavStrategy(NavBarTabId tabId) {
            super(tabId);
        }
    }
}
