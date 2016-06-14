package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.hp.autonomy.frontend.selenium.util.Waits;

public abstract class Promotion {
    private final String trigger;

    public Promotion(String trigger) {
        this.trigger = trigger;
    }

    public String getTrigger() {
        return trigger;
    }

    public abstract String getName();

    public String getCreateNotification() {
        return "Created a new " + getName() + " promotion";
    }

    public String getEditNotification() {
        return "Edited a " + getName() + " promotion";
    }

    public String getDeleteNotification() {
        return "Removed a " + getName() + " promotion";
    }

    public enum Type {
        SPOTLIGHT("SPOTLIGHT"),
        PIN_TO_POSITION("PIN_TO_POSITION");

        private final String option;

        Type(String option) {
            this.option = option;
        }

        public String getOption() {
            return option;
        }
    }

    public enum SpotlightType {
        SPONSORED("Sponsored"),
        HOTWIRE("Hotwire"),
        TOP_PROMOTIONS("Top Promotions");

        private final String option;

        SpotlightType(String option) {
            this.option = option;
        }

        public String getOption() {
            return option;
        }
    }

    public abstract Wizard makeWizard(CreateNewPromotionsBase createNewPromotionsBase);

    protected class PromotionWizard extends Wizard {
        private final CreateNewPromotionsBase page;

        public PromotionWizard(CreateNewPromotionsBase createNewPromotionsBase) {
            page = createNewPromotionsBase;
        }

        @Override
        public void next() {
            if (onFinalStep()) {
                page.finishButton().click();
            } else {
                page.continueButton().click();
                incrementStep();
            }
            Waits.loadOrFadeWait();
        }

        @Override
        public void cancel() {
            page.cancelButton().click();
            Waits.loadOrFadeWait();
        }
    }

}
