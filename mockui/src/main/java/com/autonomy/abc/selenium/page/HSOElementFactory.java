package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.HSOCreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.HSOKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.login.AbcHasLoggedIn;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.HSOCreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.HSOPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.HSOSearchPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.hp.autonomy.frontend.selenium.login.LoginPage;
import com.hp.autonomy.frontend.selenium.sso.HSOLoginPage;
import org.openqa.selenium.WebDriver;
import com.autonomy.abc.selenium.find.FindPage;

public class HSOElementFactory extends ElementFactory {
    public HSOElementFactory(final WebDriver driver) {
        super(driver);
    }

    @Override
    public PromotionsPage getPromotionsPage() {
        return new HSOPromotionsPage(getDriver());
    }

    @Override
    public LoginPage getLoginPage() {
        return new HSOLoginPage(getDriver(), new AbcHasLoggedIn(getDriver()));
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

    public FindPage getFindPage() {
        return new FindPage(getDriver());
    }
}
