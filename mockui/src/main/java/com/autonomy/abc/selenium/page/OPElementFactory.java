package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.page.keywords.*;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.search.OPSearchPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.openqa.selenium.WebDriver;

public class OPElementFactory extends ElementFactory {
    public OPElementFactory(WebDriver driver) {
        super(driver);
    }

    @Override
    public PromotionsPage getPromotionsPage() {
        return null;
    }

    @Override
    public KeywordsPage getKeywordsPage() {
        return null;
    }

    @Override
    public CreateNewKeywordsPage getCreateNewKeywordsPage() {
        return null;
    }

    @Override
    public SearchPage getSearchPage() {
        return null;
    }

}
