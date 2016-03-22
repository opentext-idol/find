package com.autonomy.abc.selenium.promotions;

import com.hp.autonomy.frontend.selenium.util.ParametrizedFactory;
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

    public static class Factory implements ParametrizedFactory<WebDriver, HSODPromotionsPage> {
        @Override
        public HSODPromotionsPage create(WebDriver context) {
            return new HSODPromotionsPage(context);
        }
    }
}
