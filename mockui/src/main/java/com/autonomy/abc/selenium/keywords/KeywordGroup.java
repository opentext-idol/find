package com.autonomy.abc.selenium.keywords;

import com.autonomy.abc.selenium.actions.wizard.OptionWizardStep;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;

class KeywordGroup {
    private String keywordString;
    private KeywordType type;

    public KeywordGroup(KeywordType type, String... keywords) {
        this.keywordString = String.join(" ", keywords);
        this.type = type;
    }

    public Wizard makeWizard(CreateNewKeywordsPage newKeywordsPage) {
        return new KeywordWizard(newKeywordsPage);
    }

    private class KeywordWizard extends Wizard {
        private CreateNewKeywordsPage page;

        private KeywordWizard(CreateNewKeywordsPage newKeywordsPage) {
            page = newKeywordsPage;
            buildSteps();
        }

        private void buildSteps() {
            this.add(new OptionWizardStep(page, "Select Type of Keywords", type.getOptionTitle()));
            this.add(new WizardStep() {
                @Override
                public String getTitle() {
                    return type.getInputTitle();
                }

                @Override
                public Object apply() {
                    page.addSynonymsTextBox().sendKeys(keywordString);
                    page.addSynonymsButton().click();
                    return null;
                }
            });
        }

        @Override
        public void next() {
            if (onFinalStep()) {
                page.finishWizardButton().click();
            } else {
                page.continueWizardButton().click();
            }
        }

        @Override
        public void cancel() {
            page.cancelWizardButton().click();
        }
    }

}
