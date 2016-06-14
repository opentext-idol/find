package com.autonomy.abc.selenium.actions.wizard;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class OptionWizardStep implements WizardStep {
    private final WebElement container;
    private final String title;
    private final String option;

    public OptionWizardStep(final WebElement container, final String title, final String option) {
        this.container = container;
        this.title = title;
        this.option = option;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Object apply() {
        container.findElement(By.cssSelector("[data-option='" + option + "']")).click();
        return null;
    }
}
