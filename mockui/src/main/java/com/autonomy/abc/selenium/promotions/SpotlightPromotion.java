package com.autonomy.abc.selenium.promotions;


import com.autonomy.abc.selenium.actions.wizard.OptionWizardStep;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsBase;
import com.autonomy.abc.selenium.page.promotions.HSOCreateNewPromotionsPage;

// TODO: properly separate on-prem from hosted
public class SpotlightPromotion extends Promotion {
    private final static Type TYPE = Type.SPOTLIGHT;
    private SpotlightType spotlightType;

    public SpotlightPromotion(String trigger) {
        this(SpotlightType.SPONSORED, trigger);
    }

    @Override
    public String getName() {
        return "spotlight promotion";
    }

    public SpotlightPromotion(SpotlightType type, String trigger) {
        super(trigger);
        spotlightType = type;
    }

    public Wizard makeWizard(CreateNewPromotionsBase createNewPromotionsBase) {
        if (createNewPromotionsBase instanceof HSOCreateNewPromotionsPage) {
            return new SpotlightPromotionWizard(createNewPromotionsBase);
        } else {
            return new OPSpotlightPromotionWizard(createNewPromotionsBase);
        }
    }

    private class SpotlightPromotionWizard extends PromotionWizard {
        public SpotlightPromotionWizard(CreateNewPromotionsBase page) {
            super(page);
            add(new OptionWizardStep(page, "Promotion type", TYPE.getOption()));
            add(new SearchTriggerStep(page, getTrigger()));
        }
    }

    private class OPSpotlightPromotionWizard extends PromotionWizard {
        public OPSpotlightPromotionWizard(CreateNewPromotionsBase page) {
            super(page);
            add(new OptionWizardStep(page, "Promotion type", TYPE.getOption()));
            add(new OptionWizardStep(page, "Promotion details", spotlightType.getOption()));
            add(new SearchTriggerStep(page, getTrigger()));
        }
    }
}
