package com.autonomy.abc.selenium.navigation;

import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.admin.HSODevelopersPage;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.page.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.page.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.gettingStarted.GettingStartedPage;
import com.autonomy.abc.selenium.page.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.page.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.page.indexes.IndexesPage;
import com.autonomy.abc.selenium.page.keywords.HSOCreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.HSOKeywordsPage;
import com.autonomy.abc.selenium.page.login.AbcHasLoggedIn;
import com.autonomy.abc.selenium.page.promotions.HSOCreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.HSOPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.search.EditDocumentReferencesPage;
import com.autonomy.abc.selenium.page.search.HSOSearchPage;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

enum HSODPage implements PageMapper.Page {
    LOGIN(new ParametrizedFactory<WebDriver, HSOLoginPage>() {
        @Override
        public HSOLoginPage create(WebDriver context) {
            return new HSOLoginPage(context, new AbcHasLoggedIn(context));
        }
    }, HSOLoginPage.class),

    ANALYTICS(NavBarTabId.ANALYTICS, new AnalyticsPage.Factory(), AnalyticsPage.class),

    SEARCH(NavBarTabId.SEARCH, new HSOSearchPage.Factory(), HSOSearchPage.class),

    CONNECTIONS(NavBarTabId.CONNECTIONS, new ConnectionsPage.Factory(), ConnectionsPage.class),
    CONNECTION_WIZARD(new NewConnectionPage.Factory(), NewConnectionPage.class),
    CONNECTION_DETAILS(new ConnectionsDetailPage.Factory(), ConnectionsDetailPage.class),

    INDEXES(NavBarTabId.INDEXES, new IndexesPage.Factory(), IndexesPage.class),
    INDEX_WIZARD(new CreateNewIndexPage.Factory(), CreateNewIndexPage.class),
    INDEX_DETAILS(new IndexesDetailPage.Factory(), IndexesDetailPage.class),

    PROMOTIONS(NavBarTabId.PROMOTIONS, new HSOPromotionsPage.Factory(), HSOPromotionsPage.class),
    PROMOTION_WIZARD(new HSOCreateNewPromotionsPage.Factory(), HSOCreateNewPromotionsPage.class),
    PROMOTION_DETAILS(new PromotionsDetailPage.Factory(), PromotionsDetailPage.class),
    EDIT_REFERENCES(new EditDocumentReferencesPage.Factory(), EditDocumentReferencesPage.class),

    KEYWORDS(NavBarTabId.KEYWORDS, new HSOKeywordsPage.Factory(), HSOKeywordsPage.class),
    KEYWORD_WIZARD(new HSOCreateNewKeywordsPage.Factory(), HSOCreateNewKeywordsPage.class),

    GETTING_STARTED(NavBarTabId.GETTING_STARTED, new GettingStartedPage.Factory(), GettingStartedPage.class),

    DEVELOPERS(NavBarTabId.DEVELOPERS, new HSODevelopersPage.Factory(), HSODevelopersPage.class),
    USERS(NavBarTabId.USERS, new HSOUsersPage.Factory(), HSOUsersPage.class);

    private final Class<?> pageType;
    private final NavBarTabId tabId;
    private final PageMapper.SwitchStrategy<SOElementFactory> switchStrategy;
    private ParametrizedFactory<WebDriver, ?> factory;

    <T extends AppPage> HSODPage(NavBarTabId tab, ParametrizedFactory<WebDriver, T> factory, Class<? super T> type) {
        tabId = tab;
        switchStrategy = new HSODElementFactory.SideNavStrategy(tab);
        pageType = type;
        this.factory = factory;
    }

    <T extends AppPage> HSODPage(ParametrizedFactory<WebDriver, T> factory, Class<? super T> type) {
        this(null, factory, type);
    }

    public Object loadAsObject(WebDriver driver) {
        return this.factory.create(driver);
    }

    @Override
    public Class<?> getPageType() {
        return pageType;
    }

    @Override
    public NavBarTabId getId() {
        return tabId;
    }
}
