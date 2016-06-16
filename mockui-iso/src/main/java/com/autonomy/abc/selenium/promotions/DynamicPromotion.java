package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.Wizard;

// TODO: refactor using factories
public class DynamicPromotion extends Promotion {
    private final int numberOfResults;
    private final SpotlightType spotlightType;

    // for HSO
    public DynamicPromotion(final int numberOfResults, final String trigger) {
        this(SpotlightType.SPONSORED, numberOfResults, trigger);
    }

    // for OP
    public DynamicPromotion(final SpotlightType spotlightType, final String trigger) {
        this(spotlightType, 10, trigger);
    }

    public DynamicPromotion(final SpotlightType spotlightType, final int numberOfResults, final String trigger) {
        super(trigger);
        this.numberOfResults = numberOfResults;
        this.spotlightType = spotlightType;
    }

    @Override
    public String getName() {
        return "dynamic spotlight";
    }

    public int getNumberOfResults() {
        return numberOfResults;
    }

    public SpotlightType getSpotlightType() {
        return spotlightType;
    }

    @Override
    public Wizard makeWizard(final CreateNewPromotionsBase createNewPromotionsBase) {
        return new DynamicPromotionsWizard(createNewPromotionsBase);
    }

    private class DynamicPromotionsWizard extends PromotionWizard {
        public DynamicPromotionsWizard(final CreateNewPromotionsBase page) {
            super(page);
            // steps are app-specific, so are set using the dynamic type of the page
            // but also need to get (app-specific) info related to the promotion
            setSteps(page.getWizardSteps(DynamicPromotion.this));
        }
    }
}
