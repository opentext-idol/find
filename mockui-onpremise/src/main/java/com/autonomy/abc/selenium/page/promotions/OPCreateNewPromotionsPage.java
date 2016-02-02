package com.autonomy.abc.selenium.page.promotions;

import com.autonomy.abc.selenium.actions.wizard.OptionWizardStep;
import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.promotions.SearchTriggerStep;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.util.ParametrizedFactory;
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

    public static class Factory implements ParametrizedFactory<WebDriver, OPCreateNewPromotionsPage> {
        public OPCreateNewPromotionsPage create(WebDriver context) {
            return new OPCreateNewPromotionsPage(context);
        }
    }

}
