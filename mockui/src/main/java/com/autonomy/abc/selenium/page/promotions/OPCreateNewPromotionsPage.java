package com.autonomy.abc.selenium.page.promotions;

import org.openqa.selenium.WebDriver;

public class OPCreateNewPromotionsPage extends CreateNewPromotionsPage {
    public OPCreateNewPromotionsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public void addSpotlightPromotion(String promotionType, String searchTrigger) {
        promotionType("SPOTLIGHT").click();
        continueButton().click();
        loadOrFadeWait();
        promotionType(promotionType).click();
        continueButton().click();
        loadOrFadeWait();
        addSearchTrigger(searchTrigger);
        finishButton().click();
        loadOrFadeWait();
    }
}
