package com.autonomy.abc.selenium.keywords;

import com.autonomy.abc.selenium.actions.wizard.OptionWizardStep;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.util.Waits;
import org.apache.commons.lang3.StringUtils;

class KeywordGroup {
    private String keywordString;
    private KeywordWizardType type;
    private Language language;

    public KeywordGroup(KeywordWizardType type, Language language, Iterable<String> keywords) {
        this.keywordString = StringUtils.join(keywords, " ");
        this.type = type;
        this.language = language;
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
            this.add(new TypeStep(page));
            this.add(new InputStep(page));
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

    private class TypeStep extends OptionWizardStep {
        private CreateNewKeywordsPage page;

        public TypeStep(CreateNewKeywordsPage container) {
            super(container, "Select Type of Keywords", type.getOption());
            this.page = container;
        }

        @Override
        public Object apply() {
            super.apply();
            page.selectLanguage(language);
            return null;
        }
    }

    private class InputStep implements WizardStep {
        private CreateNewKeywordsPage page;

        public InputStep(CreateNewKeywordsPage container) {
            this.page = container;
        }

        @Override
        public String getTitle() {
            return type.getInputTitle();
        }

        @Override
        public Object apply() {
            page.getTriggerForm().addTrigger(keywordString);
            return null;
        }
    }

}
