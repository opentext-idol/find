package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.OptionWizardStep;
import com.autonomy.abc.selenium.page.promotions.*;

// TODO: refactor using factories
public class DynamicPromotion extends Promotion {
    private int numberOfResults;
    private SpotlightType spotlightType;

    // for HSO
    public DynamicPromotion(int numberOfResults, String trigger) {
        this(SpotlightType.SPONSORED, numberOfResults, trigger);
    }

    // for OP
    public DynamicPromotion(SpotlightType spotlightType, String trigger) {
        this(spotlightType, 10, trigger);
    }

    public DynamicPromotion(SpotlightType spotlightType, int numberOfResults, String trigger) {
        super(trigger);
        this.numberOfResults = numberOfResults;
        this.spotlightType = spotlightType;
    }

    @Override
    public com.autonomy.abc.selenium.actions.wizard.Wizard makeWizard(CreateNewPromotionsBase createNewPromotionsBase) {
        if (createNewPromotionsBase instanceof HSOCreateNewPromotionsPage) {
            return new DynamicPromotionsWizard((HSOCreateNewDynamicPromotionsPage) createNewPromotionsBase);
        }
        return new DynamicPromotionsWizard(createNewPromotionsBase);
    }

    private class DynamicPromotionsWizard extends PromotionWizard {
        public DynamicPromotionsWizard(HSOCreateNewDynamicPromotionsPage page) {
            super(page);
            add(new ResultsNumberStep(page, numberOfResults));
            add(new SearchTriggerStep(page, getTrigger()));
        }

        public DynamicPromotionsWizard(CreateNewPromotionsBase page) {
            super(page);
            add(new OptionWizardStep(page, "Spotlight type", spotlightType.getOption()));
            add(new SearchTriggerStep(page, getTrigger()));
        }
    }
}
