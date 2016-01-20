package com.autonomy.abc.selenium.page.promotions;

import com.autonomy.abc.selenium.actions.wizard.OptionWizardStep;
import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.promotions.SearchTriggerStep;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.util.Waits;
import org.openqa.selenium.WebDriver;

import java.util.Arrays;
import java.util.List;

public class OPCreateNewPromotionsPage extends CreateNewPromotionsPage {
    public OPCreateNewPromotionsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public List<WizardStep> getWizardSteps(SpotlightPromotion promotion) {
        return Arrays.asList(
            new OptionWizardStep(this, "Promotion type", promotion.getTypeOption()),
            new OptionWizardStep(this, "Promotion details", promotion.getSpotlightTypeOption()),
            new SearchTriggerStep(this, promotion.getTrigger())
        );
    }

    @Override
    public void addSpotlightPromotion(String promotionType, String searchTrigger) {
        promotionType("SPOTLIGHT").click();
        continueButton().click();
        Waits.loadOrFadeWait();
        promotionType(promotionType).click();
        continueButton().click();
        Waits.loadOrFadeWait();
        getTriggerForm().addTrigger(searchTrigger);
        finishButton().click();
        Waits.loadOrFadeWait();
    }
}
