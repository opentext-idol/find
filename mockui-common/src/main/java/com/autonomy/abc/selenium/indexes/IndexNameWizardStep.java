package com.autonomy.abc.selenium.indexes;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.page.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.page.indexes.wizard.IndexNameWizardStepTab;

public class IndexNameWizardStep implements WizardStep {
    private final static String TITLE = "Choose Index Name";
    private final String name;
    private final String displayName;
    private final IndexNameWizardStepTab tab;

    public IndexNameWizardStep(CreateNewIndexPage newIndexPage, String name, String displayName){
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
