package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.OptionWizardStep;
import com.autonomy.abc.selenium.actions.wizard.Wizard;

public class PinToPositionPromotion extends Promotion {
    private static final Type TYPE = Type.PIN_TO_POSITION;
    private final int position;

    public PinToPositionPromotion(final int position, final String trigger) {
        super(trigger);
        this.position = position;
    }

    @Override
    public String getName() {
        return "pin to position";
    }

    @Override
    public Wizard makeWizard(final CreateNewPromotionsBase createNewPromotionsBase) {
        return new PinToPositionWizard((CreateNewPromotionsPage) createNewPromotionsBase);
    }

    private class PinToPositionWizard extends Promotion.PromotionWizard {
        public PinToPositionWizard(final CreateNewPromotionsPage page) {
            super(page);
            add(new OptionWizardStep(page, "Promotion type", TYPE.getOption()));
            add(new PinPositionStep(page, position));
            add(new SearchTriggerStep(page, getTrigger()));
        }
    }
}
