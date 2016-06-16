package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.application.SOPageBase;
import org.openqa.selenium.WebDriver;

public class IdolPromotionsPage extends PromotionsPage {
    private IdolPromotionsPage(final WebDriver driver) {
        super(driver);
    }

    public static class Factory extends SOPageBase.SOPageFactory<IdolPromotionsPage> {
        public Factory() {
            super(IdolPromotionsPage.class);
        }

        @Override
        public IdolPromotionsPage create(final WebDriver context) {
            return new IdolPromotionsPage(context);
        }
    }
}
