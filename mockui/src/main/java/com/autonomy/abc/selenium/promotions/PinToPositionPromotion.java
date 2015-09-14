package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.element.Wizard;

public class PinToPositionPromotion extends Promotion {
    private final static Type TYPE = Type.PIN_TO_POSITION;
    private int position;

    public PinToPositionPromotion(int position, String trigger) {
        super(trigger);
        this.position = position;
    }

    public void doWizard(Wizard wizard) {
        doType(wizard);
        if (wizard.getTitle().equals("Promotion details")) {
            doPosition(wizard);
        }
        doTriggers(wizard);
    }

    @Override
    public Type getType() {
        return TYPE;
    }

    public void doPosition(Wizard wizard) {
        for (int i=1; i<position; i++) {
            wizard.button("plus").click();
        }
        wizard.continueButton().click();
        wizard.loadOrFadeWait();
    }
}
