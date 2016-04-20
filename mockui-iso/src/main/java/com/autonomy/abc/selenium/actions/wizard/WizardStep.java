package com.autonomy.abc.selenium.actions.wizard;

import com.autonomy.abc.selenium.actions.Action;

public interface WizardStep<T> extends Action<T> {
    String getTitle();
}
