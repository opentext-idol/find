package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.page.promotions.HSOCreateNewDynamicPromotionsPage;

public class ResultsNumberStep implements WizardStep {
    private HSOCreateNewDynamicPromotionsPage page;
    private int count;

    public ResultsNumberStep(HSOCreateNewDynamicPromotionsPage page, int count) {
        this.page = page;
        this.count = count;
    }

    @Override
    public String getTitle() {
        return "Results number";
    }

    @Override
    public void apply() {
        page.dialInput().setValue(Integer.toString(count));
    }
}
