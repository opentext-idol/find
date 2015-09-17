package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.HSOCreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.HSOKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.login.OPLoginPage;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.HSOPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.OPCreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.HSOSearchPage;
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
        return new HSOPromotionsPage(getDriver());
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

}
