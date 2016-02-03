package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.PageMapper;
import com.autonomy.abc.selenium.menu.SideNavBar;
import com.autonomy.abc.selenium.menu.TopNavBar;
import com.autonomy.abc.selenium.page.admin.UsersPage;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsDetailPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.EditDocumentReferencesPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import org.openqa.selenium.WebDriver;

public abstract class SOElementFactory extends ElementFactoryBase {
    protected SOElementFactory(WebDriver driver, PageMapper<?> mapper) {
        super(driver, mapper);
    }

    public abstract TopNavBar getTopNavBar();

    public SideNavBar getSideNavBar() {
        return new SideNavBar(getDriver());
    }

    public abstract PromotionsPage getPromotionsPage();

    public abstract CreateNewPromotionsPage getCreateNewPromotionsPage();

    public PromotionsDetailPage getPromotionsDetailPage() {
        return loadPage(PromotionsDetailPage.class);
    }

    public EditDocumentReferencesPage getEditDocumentReferencesPage() {
        return loadPage(EditDocumentReferencesPage.class);
    }

    public abstract KeywordsPage getKeywordsPage();

    public abstract CreateNewKeywordsPage getCreateNewKeywordsPage();

    public abstract SearchPage getSearchPage();

    public abstract LoginPage getLoginPage();

    public abstract UsersPage getUsersPage();

    @Override
    public WebDriver getDriver() {
        return super.getDriver();
    }

    @Override
    protected void handleSwitch(NavBarTabId tab) {
        getSideNavBar().switchPage(tab);
    }
}
