package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.HSOPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import org.openqa.selenium.WebDriver;

public class HSOElementFactory extends ElementFactory {
    public HSOElementFactory(WebDriver driver) {
        super(driver);
    }

    @Override
    public PromotionsPage getPromotionsPage() {
        return new HSOPromotionsPage(getDriver());
    }
}
