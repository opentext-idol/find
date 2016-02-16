package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.indexes.Index;

class ConnectorIndexStep implements WizardStep {
    private final static String TITLE = "Index";
    private final Index index;
    private final String name;

    private final NewConnectionPage newConnectionPage;

    ConnectorIndexStep(NewConnectionPage newConnectionPage, Index index, String name){
        this.index = index;
        this.name = name;
        this.newConnectionPage = newConnectionPage;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Object apply() {
        ConnectorIndexStepTab connectorIndexStepTab = newConnectionPage.getIndexStep();

        if(index.getName().equals(name)){
            return null;
        }

        connectorIndexStepTab.selectIndexButton().click();
        connectorIndexStepTab.selectIndex(index);

        return null;
    }
}
