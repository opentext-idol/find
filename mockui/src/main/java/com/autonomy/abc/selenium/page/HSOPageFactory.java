package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.page.promotions.HSOPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import org.openqa.selenium.WebDriver;

public class HSOPageFactory extends PageFactory {
    public HSOPageFactory(WebDriver driver, ApplicationType at) {
        super(driver, at);
    }

    @Override
    public PromotionsPage getPromotionsPage() {
        return new HSOPromotionsPage(getDriver());
    }
}
