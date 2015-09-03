package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import org.openqa.selenium.WebDriver;

public class OPElementFactory extends ElementFactory {
    public OPElementFactory(WebDriver driver) {
        super(driver);
    }

    @Override
    public PromotionsPage getPromotionsPage() {
        return null;
    }

}
