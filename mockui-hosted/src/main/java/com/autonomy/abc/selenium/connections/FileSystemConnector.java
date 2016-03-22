package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.BlankWizardStep;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.indexes.Index;
import com.hp.autonomy.frontend.selenium.util.Waits;

import java.util.Arrays;
import java.util.List;

public class FileSystemConnector extends Connector {
    private final String url;

    public FileSystemConnector(String url, String name) {
        super(name);
        this.url = url;
    }

    public FileSystemConnector(String url, String name, Index index) {
        super(name, index);
        this.url = url;
    }

    @Override
    public Wizard makeWizard(NewConnectionPage newConnectionPage) {
        return new WebConnectorWizard(newConnectionPage, getCreationWizardSteps(newConnectionPage));
    }

    public Wizard makeEditWizard(NewConnectionPage newConnectionPage) {
        return new WebConnectorWizard(newConnectionPage, getEditWizardSteps(newConnectionPage));
    }

    private List<WizardStep> getCreationWizardSteps(NewConnectionPage newConnectionPage) {
        return Arrays.asList(
                new ConnectorTypeStep(newConnectionPage, url, name, this),
                new BlankWizardStep("Connector Configuration"),
                new ConnectorIndexStep(newConnectionPage, index, name),
                new BlankWizardStep("Complete")
        );
    }

    private List<WizardStep> getEditWizardSteps(NewConnectionPage newConnectionPage) {
        return getCreationWizardSteps(newConnectionPage).subList(0, 3);
    }

    private class WebConnectorWizard extends Wizard {
        private NewConnectionPage page;

        public WebConnectorWizard(NewConnectionPage newConnectionPage, List<WizardStep> steps) {
            super(steps);
            page = newConnectionPage;
        }

        @Override
        public void next() {
            if (onFinalStep()) {
                page.finishButton().click();
            } else {
                page.nextButton().click();
                incrementStep();
            }
            Waits.loadOrFadeWait();
        }

        @Override
        public void cancel() {
            page.cancelButton().click();
            Waits.loadOrFadeWait();
        }
    }
}
