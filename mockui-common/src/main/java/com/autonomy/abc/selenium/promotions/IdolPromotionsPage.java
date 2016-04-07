package com.autonomy.abc.selenium.promotions;

import org.openqa.selenium.WebDriver;

public class IdolPromotionsPage extends PromotionsPage {
    private IdolPromotionsPage(WebDriver driver) {
        super(driver);
    }

    public static class Factory extends SOPageFactory<IdolPromotionsPage> {
        public Factory() {
            super(IdolPromotionsPage.class);
        }

        public IdolPromotionsPage create(WebDriver context) {
            return new IdolPromotionsPage(context);
        }
    }
}
