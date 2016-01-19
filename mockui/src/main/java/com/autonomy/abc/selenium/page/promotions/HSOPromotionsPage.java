package com.autonomy.abc.selenium.page.promotions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HSOPromotionsPage extends PromotionsPage {
    public HSOPromotionsPage(WebDriver driver) {
        super(driver);
    }

    public WebElement staticPromotionButton() {
        return findElement(By.linkText("NEW"));
    }
}
