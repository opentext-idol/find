package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorIndexStepTab;
import org.openqa.selenium.WebElement;

public class ConnectorIndexStep implements WizardStep {
    private final static String TITLE = "Index";
    private final Index index;
    private final String name;

    private final ConnectorIndexStepTab newConnectionPage;

    public ConnectorIndexStep(NewConnectionPage newConnectionPage, Index index, String name){
        this.index = index;
        this.newConnectionPage = newConnectionPage.getIndexStep();
        this.name = name;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Object apply() {
        if(index.getName().equals(name)){
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

        throw new IndexNotFoundException(index);
    }

    private class IndexNotFoundException extends RuntimeException {
        public IndexNotFoundException(String index){
            super("Index: '"+index+"' not found");
        }

        public IndexNotFoundException(Index index){
            this(index.getName());
        }
    }
}
