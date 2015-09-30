package com.autonomy.abc.selenium.page.promotions;

import com.autonomy.abc.selenium.element.FormInput;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HSOCreateNewPromotionsPage extends CreateNewPromotionsPage {
    public HSOCreateNewPromotionsPage(WebDriver driver) {
        super(driver);
    }

    private FormInput dataInput(String attribute) {
        return new FormInput(findElement(By.cssSelector("[data-attribute='" + attribute + "']")), getDriver());
    }

    public FormInput documentTitle() {
        return dataInput("staticTitle");
    }

    public FormInput documentContent() {
        return dataInput("staticContent");
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
