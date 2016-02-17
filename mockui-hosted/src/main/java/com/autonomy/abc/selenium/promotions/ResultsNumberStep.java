package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;

class ResultsNumberStep implements WizardStep {
    private HSODCreateNewPromotionsPage page;
    private int count;

    ResultsNumberStep(HSODCreateNewPromotionsPage page, int count) {
        this.page = page;
        this.count = count;
    }

    @Override
    public String getTitle() {
        return "Results number";
    }

    @Override
    public Object apply() {
        page.setDialValue(count);
        return null;
    }
}
