package com.autonomy.abc.selenium.promotions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HSODPromotionsPage extends PromotionsPage {
    private HSODPromotionsPage(WebDriver driver) {
        super(driver);
    }

    public WebElement staticPromotionButton() {
        return findElement(By.linkText("NEW"));
    }

    public static class Factory extends SOPageFactory<HSODPromotionsPage> {
        public Factory() {
            super(HSODPromotionsPage.class);
        }

        @Override
        public HSODPromotionsPage create(WebDriver context) {
            return new HSODPromotionsPage(context);
        }
    }
}
