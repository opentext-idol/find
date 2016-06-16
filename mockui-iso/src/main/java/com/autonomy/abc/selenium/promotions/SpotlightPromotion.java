package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.Wizard;

public class SpotlightPromotion extends Promotion {
    private static final Type TYPE = Type.SPOTLIGHT;
    private final SpotlightType spotlightType;

    public SpotlightPromotion(final String trigger) {
        this(SpotlightType.SPONSORED, trigger);
    }

    @Override
    public String getName() {
        return "spotlight";
    }

    public SpotlightPromotion(final SpotlightType type, final String trigger) {
        super(trigger);
        spotlightType = type;
    }

    public String getTypeOption() {
        return TYPE.getOption();
    }

    public String getSpotlightTypeOption() {
        return spotlightType.getOption();
    }

    @Override
    public Wizard makeWizard(final CreateNewPromotionsBase createNewPromotionsBase) {
        return new SpotlightPromotionWizard(createNewPromotionsBase);
    }

    private class SpotlightPromotionWizard extends PromotionWizard {
        public SpotlightPromotionWizard(final CreateNewPromotionsBase page) {
            super(page);
            setSteps(page.getWizardSteps(SpotlightPromotion.this));
        }
    }
}
