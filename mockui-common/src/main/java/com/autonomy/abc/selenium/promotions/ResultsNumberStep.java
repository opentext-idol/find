package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.page.promotions.HSOCreateNewPromotionsPage;

public class ResultsNumberStep implements WizardStep {
    private HSOCreateNewPromotionsPage page;
    private int count;

    public ResultsNumberStep(HSOCreateNewPromotionsPage page, int count) {
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
