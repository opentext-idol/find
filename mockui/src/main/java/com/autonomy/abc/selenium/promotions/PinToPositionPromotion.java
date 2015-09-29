package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.OptionWizardStep;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsBase;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;

public class PinToPositionPromotion extends Promotion {
    private final static Type TYPE = Type.PIN_TO_POSITION;
    private int position;

    public PinToPositionPromotion(int position, String trigger) {
        super(trigger);
        this.position = position;
    }

    @Override
    public Wizard makeWizard(CreateNewPromotionsBase createNewPromotionsBase) {
        return new PinToPositionWizard((CreateNewPromotionsPage) createNewPromotionsBase);
    }

    private class PinToPositionWizard extends PromotionWizard {
        public PinToPositionWizard(CreateNewPromotionsPage page) {
            super(page);
            add(new OptionWizardStep(page, "Promotion type", TYPE.getOption()));
            add(new PinPositionStep(page, position));
            add(new SearchTriggerStep(page, getTrigger()));
        }
    }
}
