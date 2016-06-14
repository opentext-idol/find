package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.OptionWizardStep;
import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import org.openqa.selenium.WebDriver;

import java.util.Arrays;
import java.util.List;

public class IdolCreateNewPromotionsPage extends CreateNewPromotionsPage {
    private IdolCreateNewPromotionsPage(final WebDriver driver) {
        super(driver);
    }

    @Override
    public List<WizardStep> getWizardSteps(final SpotlightPromotion promotion) {
        return Arrays.asList(
            new OptionWizardStep(this, "Promotion type", promotion.getTypeOption()),
            new OptionWizardStep(this, "Promotion details", promotion.getSpotlightTypeOption()),
            new SearchTriggerStep(this, promotion.getTrigger())
        );
    }

    public static class Factory extends SOPageFactory<IdolCreateNewPromotionsPage> {
        public Factory() {
            super(IdolCreateNewPromotionsPage.class);
        }

        @Override
        public IdolCreateNewPromotionsPage create(final WebDriver context) {
            return new IdolCreateNewPromotionsPage(context);
        }
    }

}
