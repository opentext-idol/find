package com.autonomy.abc.selenium.actions.wizard;

import com.autonomy.abc.selenium.actions.Action;

import java.util.ArrayList;
import java.util.List;

public abstract class Wizard implements Action {
    private List<WizardStep> steps;
    private int currentStep = 0;

    public Wizard() {
        steps = new ArrayList<>();
    }

    public Wizard(List<WizardStep> steps) {
        this.steps = steps;
    }

    public List<WizardStep> getSteps() {
        return steps;
    }

    protected void add(WizardStep wizardStep) {
        steps.add(wizardStep);
    }

    public WizardStep getCurrentStep() {
        return steps.get(currentStep);
    }

    public boolean onFinalStep() {
        return currentStep == steps.size() - 1;
    }

    protected void incrementStep() {
        currentStep++;
    }

    public abstract void next();

    public abstract void cancel();

    public void apply() {
        for (WizardStep wizardStep : steps) {
            wizardStep.apply();
            next();
        }
    }

}
