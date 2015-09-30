package com.autonomy.abc.selenium.actions.wizard;

public class BlankWizardStep implements WizardStep {
    private String title;

    public BlankWizardStep(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Object apply() {
        return null;
    }

    @Override
    public String toString() {
        return "wizard step " + getTitle();
    }
}
