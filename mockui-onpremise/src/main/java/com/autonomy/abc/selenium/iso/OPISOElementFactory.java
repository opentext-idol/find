package com.autonomy.abc.selenium.iso;

import com.hp.autonomy.frontend.selenium.application.PageMapper;
import com.autonomy.abc.selenium.application.SOElementFactory;
import com.autonomy.abc.selenium.keywords.IdolCreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.IdolKeywordsPage;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.promotions.IdolCreateNewPromotionsPage;
import com.autonomy.abc.selenium.promotions.IdolPromotionsDetailPage;
import com.autonomy.abc.selenium.promotions.IdolPromotionsPage;
import com.autonomy.abc.selenium.promotions.SchedulePage;
import com.autonomy.abc.selenium.search.IdolIsoSearchPage;
import com.autonomy.abc.selenium.users.IdolIsoLoginPage;
import com.autonomy.abc.selenium.users.IdolUsersPage;
import com.autonomy.abc.selenium.users.UsersPage;
import org.openqa.selenium.WebDriver;

public class OPISOElementFactory extends SOElementFactory {
    public OPISOElementFactory(final WebDriver driver) {
        super(driver, new PageMapper<>(OPISOPage.class));
    }

    @Override
    public TopNavBar getTopNavBar() {
        return new IdolIsoTopNavBar(getDriver());
    }

    @Override
    public IdolPromotionsPage getPromotionsPage() {
        return loadPage(IdolPromotionsPage.class);
    }

    @Override
    public IdolIsoLoginPage getLoginPage() {
        return loadPage(IdolIsoLoginPage.class);
    }

    @Override
    public IdolPromotionsDetailPage getPromotionsDetailPage() {
        return loadPage(IdolPromotionsDetailPage.class);
    }

    @Override
    public IdolCreateNewPromotionsPage getCreateNewPromotionsPage() {
        return loadPage(IdolCreateNewPromotionsPage.class);
    }

    @Override
    public IdolKeywordsPage getKeywordsPage() {
        return loadPage(IdolKeywordsPage.class);
    }

    @Override
    public IdolCreateNewKeywordsPage getCreateNewKeywordsPage() {
        return loadPage(IdolCreateNewKeywordsPage.class);
    }

    @Override
    public IdolIsoSearchPage getSearchPage() {
        return loadPage(IdolIsoSearchPage.class);
    }

    public SchedulePage getSchedulePage() {
        return loadPage(SchedulePage.class);
    }

    @Override
    public UsersPage getUsersPage() {
        return loadPage(IdolUsersPage.class);
    }

    protected static class SideNavStrategy extends SOElementFactory.SideNavStrategy {
        protected SideNavStrategy(NavBarTabId tabId) {
            super(tabId);
        }
    }

    static class TopNavStrategy implements PageMapper.SwitchStrategy<SOElementFactory> {
        private final IdolIsoTopNavBar.TabId tab;

        TopNavStrategy(IdolIsoTopNavBar.TabId tabId) {
            tab = tabId;
        }

        @Override
        public void switchUsing(SOElementFactory context) {
            ((IdolIsoTopNavBar) context.getTopNavBar()).switchPage(tab);
        }
    }
}
