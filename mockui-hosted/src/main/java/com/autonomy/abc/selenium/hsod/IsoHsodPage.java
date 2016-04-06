package com.autonomy.abc.selenium.hsod;

import com.autonomy.abc.selenium.application.AppPageFactory;
import com.autonomy.abc.selenium.application.SOElementFactory;
import com.autonomy.abc.selenium.keywords.HSODCreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.HSODKeywordsPage;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.promotions.HSODCreateNewPromotionsPage;
import com.autonomy.abc.selenium.promotions.HSODPromotionsPage;
import com.autonomy.abc.selenium.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.search.EditDocumentReferencesPage;
import com.autonomy.abc.selenium.search.HSODSearchPage;
import com.autonomy.abc.selenium.users.HSODDevelopersPage;
import com.autonomy.abc.selenium.users.HSODUsersPage;
import com.autonomy.abc.selenium.users.SOHasLoggedIn;
import com.hp.autonomy.frontend.selenium.application.PageMapper;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

enum IsoHsodPage implements PageMapper.Page, PageMapper.SwitchStrategy<SOElementFactory> {
    LOGIN(new AppPageFactory<HSOLoginPage>() {
        @Override
        public Class<HSOLoginPage> getPageType() {
            return HSOLoginPage.class;
        }

        @Override
        public HSOLoginPage create(WebDriver context) {
            return new HSOLoginPage(context, new SOHasLoggedIn(context));
        }
    }),

    SEARCH(NavBarTabId.SEARCH, new HSODSearchPage.Factory()),

    PROMOTIONS(NavBarTabId.PROMOTIONS, new HSODPromotionsPage.Factory()),
    PROMOTION_WIZARD(new HSODCreateNewPromotionsPage.Factory()),
    PROMOTION_DETAILS(new PromotionsDetailPage.Factory()),
    EDIT_REFERENCES(new EditDocumentReferencesPage.Factory()),

    KEYWORDS(NavBarTabId.KEYWORDS, new HSODKeywordsPage.Factory()),
    KEYWORD_WIZARD(new HSODCreateNewKeywordsPage.Factory()),

    DEVELOPERS(NavBarTabId.DEVELOPERS, new HSODDevelopersPage.Factory()),
    USERS(NavBarTabId.USERS, new HSODUsersPage.Factory());

    private PageMapper.SwitchStrategy<SOElementFactory> switchStrategy;
    private AppPageFactory<?> factory;

    <T extends AppPage> IsoHsodPage(AppPageFactory<T> factory) {
        this.factory = factory;
    }

    <T extends AppPage> IsoHsodPage(NavBarTabId tab, AppPageFactory<T> factory) {
        this(factory);
        this.switchStrategy = new HSODElementFactory.SideNavStrategy(tab);
    }

    @Override
    public Object loadAsObject(WebDriver driver) {
        return this.factory.create(driver);
    }

    @Override
    public Class<?> getPageType() {
        return factory.getPageType();
    }

    @Override
    public void switchUsing(SOElementFactory context) {
        switchStrategy.switchUsing(context);
    }
}
