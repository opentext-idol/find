package com.autonomy.abc.selenium.indexes;

import com.autonomy.abc.selenium.actions.wizard.WizardStep;

import java.util.List;

public class IndexConfigStep implements WizardStep {
    private final static String TITLE = "Index Configuration";
    private final CreateNewIndexPage page;
    private final List<String> parametricFields;
    private final List<String> indexFields;

    public IndexConfigStep(CreateNewIndexPage page, List<String> parametricFields, List<String> indexFields) {
        this.page = page;
        this.parametricFields = parametricFields;
        this.indexFields = indexFields;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Object apply() {
        IndexConfigStepTab tab = page.getIndexConfigStepTab();

        tab.setParametricFields(parametricFields);
        tab.setIndexFields(indexFields);
        return null;
    }
}
