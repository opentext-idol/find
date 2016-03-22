package com.autonomy.abc.selenium.indexes;

import com.autonomy.abc.selenium.actions.wizard.BlankWizardStep;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.hp.autonomy.frontend.selenium.util.Waits;

public class IndexWizard extends Wizard {
    private final CreateNewIndexPage page;
    public IndexWizard(Index index, CreateNewIndexPage page) {
        super();
        this.page = page;
        add(new IndexNameWizardStep(page, index.getName(), index.getDisplayName()));
        add(new IndexConfigStep(page, index.getParametricFields(),index.getParametricFields()));
        add(new BlankWizardStep("Summary"));
    }

    @Override
    public void next() {
        if (onFinalStep()) {
            page.finishWizardButton().click();
        } else {
            page.continueWizardButton().click();
            incrementStep();
        }
        Waits.loadOrFadeWait();
    }

    @Override
    public void cancel() {
        page.cancelWizardButton().click();
    }
}
