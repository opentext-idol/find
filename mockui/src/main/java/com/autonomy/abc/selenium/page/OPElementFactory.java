package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.page.keywords.*;
import com.autonomy.abc.selenium.page.login.OPLoginPage;
import com.autonomy.abc.selenium.page.promotions.*;
import com.autonomy.abc.selenium.page.search.HSOSearchPage;
import com.autonomy.abc.selenium.page.search.OPSearchPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import org.openqa.selenium.WebDriver;

public class OPElementFactory extends ElementFactory {
    // TODO: replace HSOs with OPs
    public OPElementFactory(final WebDriver driver) {
        super(driver);
    }

    @Override
    public PromotionsPage getPromotionsPage() {
        return new OPPromotionsPage(getDriver());
    }

    @Override
    public LoginPage getLoginPage() {
        return new OPLoginPage(getDriver());
    }

    @Override
    public CreateNewPromotionsPage getCreateNewPromotionsPage() {
        return new OPCreateNewPromotionsPage(getDriver());
    }

    @Override
    public KeywordsPage getKeywordsPage() {
        return new OPKeywordsPage(getDriver());
    }

    @Override
    public CreateNewKeywordsPage getCreateNewKeywordsPage() {
        return new OPCreateNewKeywordsPage(getDriver());
    }

    @Override
    public SearchPage getSearchPage() {
        return new OPSearchPage(getDriver());
    }

}
