package com.autonomy.abc.selenium.actions.wizard;

import com.autonomy.abc.selenium.actions.Action;

import java.util.List;

public interface Wizard extends Action {
    List<WizardStep> getSteps();

    WizardStep getCurrentStep();

    void next();

    void cancel();

}
