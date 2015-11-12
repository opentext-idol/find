package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorIndexStepTab;
import org.openqa.selenium.WebElement;

public class ConnectorIndexStep implements WizardStep {
    private final static String TITLE = "Index";
    private final Index index;

    private final ConnectorIndexStepTab newConnectionPage;

    public ConnectorIndexStep(NewConnectionPage newConnectionPage, Index index){
        this.index = index;
        this.newConnectionPage = newConnectionPage.getIndexStep();
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Object apply() {
        if(index == null){
            return null;
        }

        newConnectionPage.selectIndexButton().click();
        newConnectionPage.getIndexSearchBox().click();

        for(WebElement existingIndex : newConnectionPage.getExistingIndexes()){
            if(existingIndex.getText().equals(index.getName())){
                existingIndex.click();
                return null;
            }
        }

        throw new IndexNotFoundException();
    }

    private class IndexNotFoundException extends RuntimeException {
    }
}
