package com.autonomy.abc.selenium.page;

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

public abstract class ElementFactory {

    private final WebDriver driver;

    protected ElementFactory(WebDriver driver){
        this.driver = driver;
    }

    public abstract PromotionsPage getPromotionsPage();

    public abstract CreateNewPromotionsPage getCreateNewPromotionsPage();

    public PromotionsDetailPage getPromotionsDetailPage() {
        return new PromotionsDetailPage(driver);
    };

    public EditDocumentReferencesPage getEditDocumentReferencesPage() {
        return EditDocumentReferencesPage.make(driver);
    }

    public abstract KeywordsPage getKeywordsPage();

    public abstract CreateNewKeywordsPage getCreateNewKeywordsPage();

    public abstract SearchPage getSearchPage();

    public abstract LoginPage getLoginPage();

    public WebDriver getDriver() {
        return driver;
    }

    public abstract UsersPage getUsersPage();
}
