package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsBase;

public class SearchTriggerStep implements WizardStep {
    private CreateNewPromotionsBase page;
    private String trigger;

    public SearchTriggerStep(CreateNewPromotionsBase page, String trigger) {
        this.page = page;
        this.trigger = trigger;
    }

    @Override
    public String getTitle() {
        return "Promotion triggers";
    }

    @Override
    public Object apply() {
        page.triggerBox().setAndSubmit(trigger);
        return null;
    }
}
