package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.find.Find;
import com.autonomy.abc.selenium.menu.HSO.HSOTopNavBar;
import com.autonomy.abc.selenium.menu.HSODPage;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.PageMapper;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.admin.HSODevelopersPage;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.page.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.page.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.devconsole.DevConsoleHomePage;
import com.autonomy.abc.selenium.page.devconsole.DevConsoleSearchPage;
import com.autonomy.abc.selenium.page.gettingStarted.GettingStartedPage;
import com.autonomy.abc.selenium.page.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.page.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.HSOCreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.HSOKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.login.FindHasLoggedIn;
import com.autonomy.abc.selenium.page.promotions.HSOCreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.HSOPromotionsPage;
import com.autonomy.abc.selenium.page.search.HSOSearchPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import org.openqa.selenium.WebDriver;

public class HSOElementFactory extends ElementFactory {
    public HSOElementFactory(final WebDriver driver) {
        super(driver, new PageMapper<>(HSODPage.class));
    }

    @Override
    public TopNavBar getTopNavBar() {
        return new HSOTopNavBar(getDriver());
    }

    @Override
    public HSOPromotionsPage getPromotionsPage() {
        return loadPage(HSOPromotionsPage.class);
    }

    @Override
    public LoginPage getLoginPage() {
        return loadPage(HSOLoginPage.class);
    }

    public LoginPage getFindLoginPage() {
        return new HSOLoginPage(getDriver(), new FindHasLoggedIn(this));
    }

    public LoginPage getDevConsoleLoginPage(){
        return new HSOLoginPage(getDriver(), new DevConsoleHasLoggedIn(getDriver()));
    }

    @Override
    public KeywordsPage getKeywordsPage() {
        return loadPage(HSOKeywordsPage.class);
    }

    @Override
    public CreateNewKeywordsPage getCreateNewKeywordsPage() {
        return loadPage(HSOCreateNewKeywordsPage.class);
    }

    @Override
    public SearchPage getSearchPage() {
        return loadPage(HSOSearchPage.class);
    }

    @Override
    public HSOCreateNewPromotionsPage getCreateNewPromotionsPage() {
        return loadPage(HSOCreateNewPromotionsPage.class);
    }

    public HSOUsersPage getUsersPage(){
        return loadPage(HSOUsersPage.class);
    }

    public ConnectionsPage getConnectionsPage() {
        return loadPage(ConnectionsPage.class);
    }

    public NewConnectionPage getNewConnectionPage() {
        return loadPage(NewConnectionPage.class);
    }

    public ConnectionsDetailPage getConnectionsDetailPage() {
        return loadPage(ConnectionsDetailPage.class);
    }

    public Find getFindPage() {
        return new Find(getDriver());
    }


    public AnalyticsPage getAnalyticsPage() {
        return loadPage(AnalyticsPage.class);
    }

    public IndexesPage getIndexesPage() {
        return loadPage(IndexesPage.class);
    }

    public CreateNewIndexPage getCreateNewIndexPage() {
        return loadPage(CreateNewIndexPage.class);
    }

    public IndexesDetailPage getIndexesDetailPage() {
        return loadPage(IndexesDetailPage.class);
    }

    public GettingStartedPage getGettingStartedPage() {
        return loadPage(GettingStartedPage.class);
    }

    public HSODevelopersPage getDevsPage() {
        return loadPage(HSODevelopersPage.class);
    }

    public DevConsoleSearchPage getDevConsoleSearchPage() {
        return new DevConsoleSearchPage(getDriver());
    }

    public DevConsoleHomePage getDevConsoleHomePage() {
        return new DevConsoleHomePage(getDriver());
    }
}
