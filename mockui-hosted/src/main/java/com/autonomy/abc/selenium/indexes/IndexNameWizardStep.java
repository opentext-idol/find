package com.autonomy.abc.selenium.indexes;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;

class IndexNameWizardStep implements WizardStep {
    private final static String TITLE = "Choose Index Name";
    private final String name;
    private final String displayName;
    private final IndexNameWizardStepTab tab;

    IndexNameWizardStep(CreateNewIndexPage newIndexPage, String name, String displayName){
        this.tab = newIndexPage.getIndexNameWizardStepTab();
        this.name = name;
        this.displayName = displayName;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Object apply() {
        tab.indexNameInput().setValue(name);
        if(displayName != null && !name.equals(displayName)){
            tab.displayNameInput().setValue(displayName);
        }
        return null;
    }
}
