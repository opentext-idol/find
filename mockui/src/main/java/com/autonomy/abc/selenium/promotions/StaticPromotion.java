package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.element.Wizard;

public class StaticPromotion extends Promotion {
    private String title;
    private String content;

    public StaticPromotion(String title, String content, String trigger) {
        super(trigger);
        this.title = title;
        this.content = content;
    }

    @Override
    public void doWizard(Wizard wizard) {
        doDetails(wizard);
        doLanguage(wizard);
        doTriggers(wizard);
    }

    private void doDetails(Wizard wizard) {
        wizard.formInput().setValue(title);
        wizard.textarea().setValue(content);
        wizard.continueButton().click();
    }

    private void doLanguage(Wizard wizard) {
        wizard.continueButton().click();
    }
}
