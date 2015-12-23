package com.autonomy.abc.selenium.page.promotions;

import com.autonomy.abc.selenium.util.Waits;
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

    // use PromotionActionFactory instead
    @Deprecated
    public void newStaticPromotion(String title, String content, String trigger){
        findElement(By.linkText("Promote new document")).click();
        Waits.loadOrFadeWait();
        findElement(By.cssSelector("[data-attribute='staticTitle']")).sendKeys(title);
        findElement(By.cssSelector("[data-attribute='staticContent']")).sendKeys(content);
        WebElement continueButton = findElement(By.cssSelector(".wizard-controls .next-step"));
        continueButton.click();
        Waits.loadOrFadeWait();
        continueButton.click();
        Waits.loadOrFadeWait();
        findElement(By.cssSelector(".current-step input")).sendKeys(trigger);
        findElement(By.cssSelector(".current-step .input-group .btn")).click();
        Waits.loadOrFadeWait();
        findElement(By.cssSelector(".wizard-controls .finish-step")).click();
    }
}
