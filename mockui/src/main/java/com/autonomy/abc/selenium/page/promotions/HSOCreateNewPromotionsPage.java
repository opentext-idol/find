package com.autonomy.abc.selenium.page.promotions;

import org.openqa.selenium.WebDriver;

public class HSOCreateNewPromotionsPage extends CreateNewPromotionsPage {
    public HSOCreateNewPromotionsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public void addSpotlightPromotion(final String promotionType, final String searchTrigger) {
        // TODO: move this logic into a "SpotlightPromotion" object
        promotionType("SPOTLIGHT").click();
        continueButton(WizardStep.TYPE).click();
        addSearchTrigger(searchTrigger);
        finishButton().click();
    }
}
