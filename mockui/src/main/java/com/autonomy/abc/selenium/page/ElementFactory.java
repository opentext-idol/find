package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import org.openqa.selenium.WebDriver;

public abstract class ElementFactory {

    private final WebDriver driver;

    protected ElementFactory(WebDriver driver){
        this.driver = driver;
    }

    public static ElementFactory from(ApplicationType applicationType, WebDriver driver){
        return (applicationType == applicationType.HOSTED) ? new HSOElementFactory(driver) : new OPElementFactory(driver);
    }

    public abstract PromotionsPage getPromotionsPage();

    public abstract CreateNewPromotionsPage getCreateNewPromotionsPage();

    public abstract KeywordsPage getKeywordsPage();

    public abstract CreateNewKeywordsPage getCreateNewKeywordsPage();

    public abstract SearchPage getSearchPage();

    public abstract LoginPage getLoginPage();

    public WebDriver getDriver() {
        return driver;
    }
}
