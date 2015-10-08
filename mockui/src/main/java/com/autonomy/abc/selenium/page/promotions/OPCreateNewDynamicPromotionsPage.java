package com.autonomy.abc.selenium.page.promotions;

import org.openqa.selenium.WebDriver;

/**
 * Created by mattwill on 03/09/2015.
 */
public class OPCreateNewDynamicPromotionsPage extends CreateNewDynamicPromotionsPage {


    public OPCreateNewDynamicPromotionsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public void createDynamicPromotion(String type, String trigger) {
            spotlightType(type).click();
            continueButton(WizardStep.PROMOTION_TYPE).click();
            loadOrFadeWait();
            addSearchTrigger(trigger);
            finishButton().click();
            loadOrFadeWait();
    }
}
