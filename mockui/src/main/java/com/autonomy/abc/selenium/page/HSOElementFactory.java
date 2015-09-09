package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.page.keywords.*;
import com.autonomy.abc.selenium.page.login.HSOLoginPage;
import com.autonomy.abc.selenium.page.login.LoginPage;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.HSOCreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.HSOPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.HSOSearchPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.openqa.selenium.WebDriver;

public class HSOElementFactory extends ElementFactory {
    public HSOElementFactory(WebDriver driver) {
        super(driver);
    }

    @Override
    public PromotionsPage getPromotionsPage() {
        return new HSOPromotionsPage(getDriver());
    }

    @Override
    public LoginPage getLoginPage() {
        return new HSOLoginPage(getDriver());
    }

    @Override
    public KeywordsPage getKeywordsPage() {
        return new HSOKeywordsPage(getDriver());
    }

    @Override
    public CreateNewKeywordsPage getCreateNewKeywordsPage() {
        return new HSOCreateNewKeywordsPage(getDriver());
    }

    @Override
    public SearchPage getSearchPage() {
        return new HSOSearchPage(getDriver());
    }

    @Override
    public CreateNewPromotionsPage getCreateNewPromotionsPage() {
        return new HSOCreateNewPromotionsPage(getDriver());
    }
}
