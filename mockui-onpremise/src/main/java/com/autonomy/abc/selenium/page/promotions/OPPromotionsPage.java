package com.autonomy.abc.selenium.page.promotions;

import com.autonomy.abc.selenium.util.ParametrizedFactory;
import org.openqa.selenium.WebDriver;

public class OPPromotionsPage extends PromotionsPage {
    public OPPromotionsPage(WebDriver driver) {
        super(driver);
    }

    public static class Factory implements ParametrizedFactory<WebDriver, OPPromotionsPage> {
        public OPPromotionsPage create(WebDriver context) {
            return new OPPromotionsPage(context);
        }
    }
}
