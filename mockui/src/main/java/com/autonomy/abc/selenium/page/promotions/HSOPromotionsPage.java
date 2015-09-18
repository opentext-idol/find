package com.autonomy.abc.selenium.page.promotions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HSOPromotionsPage extends PromotionsPage {
    public HSOPromotionsPage(WebDriver driver) {
        super(driver);
    }

    public void newStaticPromotion(String title, String content, String trigger){
        findElement(By.linkText("Promote new document")).click();
        loadOrFadeWait();
        findElement(By.cssSelector("[data-attribute='staticTitle']")).sendKeys(title);
        findElement(By.cssSelector("[data-attribute='staticContent']")).sendKeys(content);
        findElement(By.cssSelector(".current-step .next-step")).click();
        loadOrFadeWait();
        findElement(By.cssSelector(".current-step .next-step")).click();
        loadOrFadeWait();
        findElement(By.cssSelector(".current-step input")).sendKeys(trigger);
        findElement(By.cssSelector(".current-step .input-group .btn")).click();
        loadOrFadeWait();
        findElement(By.cssSelector(".current-step .finish-step")).click();
    }
}
