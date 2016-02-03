package com.autonomy.abc.selenium.page;

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
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.WebDriver;

public abstract class SOElementFactory {
    private final WebDriver driver;
    private final PageMapper<?> mapper;

    protected SOElementFactory(WebDriver driver, PageMapper<?> mapper){
        this.driver = driver;
        this.mapper = mapper;
    }

    public abstract TopNavBar getTopNavBar();

    public SideNavBar getSideNavBar() {
        return new SideNavBar(driver);
    }

    public abstract PromotionsPage getPromotionsPage();

    public abstract CreateNewPromotionsPage getCreateNewPromotionsPage();

    public PromotionsDetailPage getPromotionsDetailPage() {
        return loadPage(PromotionsDetailPage.class);
    };

    public EditDocumentReferencesPage getEditDocumentReferencesPage() {
        return loadPage(EditDocumentReferencesPage.class);
    }

    public abstract KeywordsPage getKeywordsPage();

    public abstract CreateNewKeywordsPage getCreateNewKeywordsPage();

    public abstract SearchPage getSearchPage();

    public abstract LoginPage getLoginPage();

    public WebDriver getDriver() {
        return driver;
    }

    public abstract UsersPage getUsersPage();

    public <T extends AppPage> T switchTo(Class<T> type) {
        getSideNavBar().switchPage(mapper.getId(type));
        return loadPage(type);
    }

    public <T extends AppPage> T loadPage(Class<T> type) {
        return mapper.load(type, getDriver());
    }
}
