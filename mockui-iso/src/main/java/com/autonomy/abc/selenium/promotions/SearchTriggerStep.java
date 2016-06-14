package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;

public class SearchTriggerStep implements WizardStep {
    private final CreateNewPromotionsBase page;
    private final String trigger;
    public static final String TITLE = "Promotion triggers";

    public SearchTriggerStep(CreateNewPromotionsBase page, String trigger) {
        this.page = page;
        this.trigger = trigger;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Object apply() {
        page.getTriggerForm().addTrigger(trigger);
        return null;
    }
}
