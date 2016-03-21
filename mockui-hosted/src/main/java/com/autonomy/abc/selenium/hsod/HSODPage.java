package com.autonomy.abc.selenium.hsod;

import com.autonomy.abc.selenium.analytics.AnalyticsPage;
import com.hp.autonomy.frontend.selenium.application.PageMapper;
import com.autonomy.abc.selenium.application.SOElementFactory;
import com.autonomy.abc.selenium.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.connections.ConnectionsPage;
import com.autonomy.abc.selenium.connections.NewConnectionPage;
import com.autonomy.abc.selenium.icma.GettingStartedPage;
import com.autonomy.abc.selenium.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.indexes.IndexesPage;
import com.autonomy.abc.selenium.keywords.HSODCreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.HSODKeywordsPage;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.promotions.HSODCreateNewPromotionsPage;
import com.autonomy.abc.selenium.promotions.HSODPromotionsPage;
import com.autonomy.abc.selenium.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.search.EditDocumentReferencesPage;
import com.autonomy.abc.selenium.search.HSODSearchPage;
import com.autonomy.abc.selenium.users.SOHasLoggedIn;
import com.autonomy.abc.selenium.users.HSODDevelopersPage;
import com.autonomy.abc.selenium.users.HSODUsersPage;
import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

enum HSODPage implements PageMapper.Page, PageMapper.SwitchStrategy<SOElementFactory> {
    LOGIN(new ParametrizedFactory<WebDriver, HSOLoginPage>() {
        @Override
        public HSOLoginPage create(WebDriver context) {
            return new HSOLoginPage(context, new SOHasLoggedIn(context));
        }
    }, HSOLoginPage.class),

    ANALYTICS(NavBarTabId.ANALYTICS, new AnalyticsPage.Factory(), AnalyticsPage.class),

    SEARCH(NavBarTabId.SEARCH, new HSODSearchPage.Factory(), HSODSearchPage.class),

    CONNECTIONS(NavBarTabId.CONNECTIONS, new ConnectionsPage.Factory(), ConnectionsPage.class),
    CONNECTION_WIZARD(new NewConnectionPage.Factory(), NewConnectionPage.class),
    CONNECTION_DETAILS(new ConnectionsDetailPage.Factory(), ConnectionsDetailPage.class),

    INDEXES(NavBarTabId.INDEXES, new IndexesPage.Factory(), IndexesPage.class),
    INDEX_WIZARD(new CreateNewIndexPage.Factory(), CreateNewIndexPage.class),
    INDEX_DETAILS(new IndexesDetailPage.Factory(), IndexesDetailPage.class),

    PROMOTIONS(NavBarTabId.PROMOTIONS, new HSODPromotionsPage.Factory(), HSODPromotionsPage.class),
    PROMOTION_WIZARD(new HSODCreateNewPromotionsPage.Factory(), HSODCreateNewPromotionsPage.class),
    PROMOTION_DETAILS(new PromotionsDetailPage.Factory(), PromotionsDetailPage.class),
    EDIT_REFERENCES(new EditDocumentReferencesPage.Factory(), EditDocumentReferencesPage.class),

    KEYWORDS(NavBarTabId.KEYWORDS, new HSODKeywordsPage.Factory(), HSODKeywordsPage.class),
    KEYWORD_WIZARD(new HSODCreateNewKeywordsPage.Factory(), HSODCreateNewKeywordsPage.class),

    GETTING_STARTED(NavBarTabId.GETTING_STARTED, new GettingStartedPage.Factory(), GettingStartedPage.class),

    DEVELOPERS(NavBarTabId.DEVELOPERS, new HSODDevelopersPage.Factory(), HSODDevelopersPage.class),
    USERS(NavBarTabId.USERS, new HSODUsersPage.Factory(), HSODUsersPage.class);

    private final Class<?> pageType;
    private final PageMapper.SwitchStrategy<SOElementFactory> switchStrategy;
    private ParametrizedFactory<WebDriver, ?> factory;

    <T extends AppPage> HSODPage(NavBarTabId tab, ParametrizedFactory<WebDriver, T> factory, Class<? super T> type) {
        switchStrategy = new HSODElementFactory.SideNavStrategy(tab);
        pageType = type;
        this.factory = factory;
    }

    <T extends AppPage> HSODPage(ParametrizedFactory<WebDriver, T> factory, Class<? super T> type) {
        this(null, factory, type);
    }

    @Override
    public Object loadAsObject(WebDriver driver) {
        return this.factory.create(driver);
    }

    @Override
    public Class<?> getPageType() {
        return pageType;
    }

    @Override
    public void switchUsing(SOElementFactory context) {
        switchStrategy.switchUsing(context);
    }
}
