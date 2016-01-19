package com.autonomy.abc.selenium.indexes;

import com.autonomy.abc.selenium.actions.wizard.BlankWizardStep;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.page.indexes.CreateNewIndexPage;
import com.autonomy.abc.selenium.util.Waits;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Index {
    private final String name;
    private final String displayName;
    private final List<String> parametricFields = new ArrayList<>();
    private final List<String> indexFields = new ArrayList<>();

    public final static Index DEFAULT = new Index("default_index", "Default Index");

    public Index(String name) {
        this.name = name;
        this.displayName = null;
    }

    public Index(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public Index withParametricFields(Collection<String> fields) {
        parametricFields.addAll(fields);
        return this;
    }

    public Index withIndexFields(Collection<String> fields) {
        getIndexFields().addAll(fields);
        return this;
    }

    public String getName() {
        return name;
    }

    public List<String> getParametricFields() {
        return Collections.unmodifiableList(parametricFields);
    }

    public List<String> getIndexFields() {
        return Collections.unmodifiableList(indexFields);
    }

    public String getCreateNotification() {
        return "Created a new index: " + name;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Index){
            if (((Index) obj).getName().equals(getName())){
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "Index<" + getName() + ">";
    }

    public String getDisplayName() {
        if(displayName == null){
            return name;
        }

        return displayName;
    }

    public Wizard makeWizard(CreateNewIndexPage newIndexPage){
        return new IndexWizard(newIndexPage);
    }

    private class IndexWizard extends Wizard {
        private CreateNewIndexPage page;

        public IndexWizard(CreateNewIndexPage newIndexPage){
            super();
            this.page = newIndexPage;
            add(new IndexNameWizardStep(page, name, displayName));
            add(new IndexConfigStep(page, parametricFields, indexFields));
            add(new BlankWizardStep("Summary"));
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
}
