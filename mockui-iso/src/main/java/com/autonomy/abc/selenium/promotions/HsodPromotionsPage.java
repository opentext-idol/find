package com.autonomy.abc.selenium.promotions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HsodPromotionsPage extends PromotionsPage {
    private HsodPromotionsPage(final WebDriver driver) {
        super(driver);
    }

    public WebElement staticPromotionButton() {
        return findElement(By.linkText("NEW"));
    }

    public static class Factory extends SOPageFactory<HsodPromotionsPage> {
        public Factory() {
            super(HsodPromotionsPage.class);
        }

        @Override
        public HsodPromotionsPage create(final WebDriver context) {
            return new HsodPromotionsPage(context);
        }
    }
}
