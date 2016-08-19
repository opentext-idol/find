package com.autonomy.abc.selenium.hsod;

import com.autonomy.abc.selenium.analytics.FakeIcmPage;
import com.autonomy.abc.selenium.application.AppPageFactory;
import com.autonomy.abc.selenium.application.IsoElementFactory;
import com.autonomy.abc.selenium.keywords.HsodCreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.HsodKeywordsPage;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.promotions.HsodCreateNewPromotionsPage;
import com.autonomy.abc.selenium.promotions.HsodPromotionsPage;
import com.autonomy.abc.selenium.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.search.EditDocumentReferencesPage;
import com.autonomy.abc.selenium.search.HsodSearchPage;
import com.autonomy.abc.selenium.users.HsodDevelopersPage;
import com.autonomy.abc.selenium.users.HsodUsersPage;
import com.autonomy.abc.selenium.users.SOHasLoggedIn;
import com.hp.autonomy.frontend.selenium.application.PageMapper;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

enum IsoHsodPage implements PageMapper.Page, PageMapper.SwitchStrategy<IsoElementFactory> {
    LOGIN(new AppPageFactory<HSOLoginPage>() {
        @Override
        public Class<HSOLoginPage> getPageType() {
            return HSOLoginPage.class;
        }

        @Override
        public HSOLoginPage create(final WebDriver context) {
            return new HSOLoginPage(context, new SOHasLoggedIn(context));
        }
    }),

    ANALYTICS(NavBarTabId.ANALYTICS, new FakeIcmPage.Factory()),

    SEARCH(NavBarTabId.SEARCH, new HsodSearchPage.Factory()),

    PROMOTIONS(NavBarTabId.PROMOTIONS, new HsodPromotionsPage.Factory()),
    PROMOTION_WIZARD(new HsodCreateNewPromotionsPage.Factory()),
    PROMOTION_DETAILS(new PromotionsDetailPage.Factory()),
    EDIT_REFERENCES(new EditDocumentReferencesPage.Factory()),

    KEYWORDS(NavBarTabId.KEYWORDS, new HsodKeywordsPage.Factory()),
    KEYWORD_WIZARD(new HsodCreateNewKeywordsPage.Factory()),

    DEVELOPERS(NavBarTabId.DEVELOPERS, new HsodDevelopersPage.Factory()),
    USERS(NavBarTabId.USERS, new HsodUsersPage.Factory());

    private PageMapper.SwitchStrategy<IsoElementFactory> switchStrategy;
    private final AppPageFactory<?> factory;

    <T extends AppPage> IsoHsodPage(final AppPageFactory<T> factory) {
        this.factory = factory;
    }

    <T extends AppPage> IsoHsodPage(final NavBarTabId tab, final AppPageFactory<T> factory) {
        this(factory);
        this.switchStrategy = new IsoHsodElementFactory.SideNavStrategy(tab);
    }

    @Override
    public Object loadAsObject(final WebDriver driver) {
        return this.factory.create(driver);
    }

    @Override
    public Class<?> getPageType() {
        return factory.getPageType();
    }

    @Override
    public void switchUsing(final IsoElementFactory context) {
        switchStrategy.switchUsing(context);
    }
}
