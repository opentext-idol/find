package com.autonomy.abc.selenium.promotions;

import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
import org.openqa.selenium.WebDriver;

public class OPPromotionsPage extends PromotionsPage {
    private OPPromotionsPage(WebDriver driver) {
        super(driver);
    }

    public static class Factory implements ParametrizedFactory<WebDriver, OPPromotionsPage> {
        public OPPromotionsPage create(WebDriver context) {
            return new OPPromotionsPage(context);
        }
    }
}
