package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsBase;

public class SpotlightPromotion extends Promotion {
    private final static Type TYPE = Type.SPOTLIGHT;
    private SpotlightType spotlightType;

    public SpotlightPromotion(String trigger) {
        this(SpotlightType.SPONSORED, trigger);
    }

    @Override
    public String getName() {
        return "spotlight";
    }

    public SpotlightPromotion(SpotlightType type, String trigger) {
        super(trigger);
        spotlightType = type;
    }

    public String getTypeOption() {
        return TYPE.getOption();
    }

    public String getSpotlightTypeOption() {
        return spotlightType.getOption();
    }

    public Wizard makeWizard(CreateNewPromotionsBase createNewPromotionsBase) {
        return new SpotlightPromotionWizard(createNewPromotionsBase);
    }

    private class SpotlightPromotionWizard extends PromotionWizard {
        public SpotlightPromotionWizard(CreateNewPromotionsBase page) {
            super(page);
            setSteps(page.getWizardSteps(SpotlightPromotion.this));
        }
    }
}
