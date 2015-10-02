package com.autonomy.abc.selenium.actions.wizard;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class OptionWizardStep implements WizardStep {
    private WebElement container;
    private String title;
    private String option;

    public OptionWizardStep(WebElement container, String title, String option) {
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
