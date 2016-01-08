package com.autonomy.abc.selenium.indexes;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.page.indexes.CreateNewIndexPage;

import java.util.List;

public class IndexConfigStep implements WizardStep {
    private final static String TITLE = "Index Configuration";
    private final IndexConfigStepTab tab;
    private final List<String> parametricFields;
    private final List<String> indexFields;

    public IndexConfigStep(CreateNewIndexPage page, List<String> parametricFields, List<String> indexFields) {
        this.tab = page.getIndexConfigStepTab();
        this.parametricFields = parametricFields;
        this.indexFields = indexFields;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Object apply() {
        tab.setParametricFields(parametricFields);
        tab.setIndexFields(indexFields);
        return null;
    }
}
