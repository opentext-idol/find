package com.autonomy.abc.selenium.page.promotions;

import com.autonomy.abc.selenium.element.FormInput;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HSOCreateNewDynamicPromotionsPage extends  CreateNewDynamicPromotionsPage{
    public HSOCreateNewDynamicPromotionsPage(WebDriver driver) {
        super(driver);
    }

    public FormInput dialInput() {
        return new FormInput(findElement(By.cssSelector("input.dial")), getDriver());
    }

    @Override
    public void createDynamicPromotion(String type, String trigger) {
            spotlightType(type).click();
            continueButton(WizardStep.PROMOTION_TYPE).click();
            loadOrFadeWait();
            continueButton(WizardStep.RESULTS).click();
            loadOrFadeWait();
            addSearchTrigger(trigger);
            finishButton().click();
            loadOrFadeWait();
    }
}
