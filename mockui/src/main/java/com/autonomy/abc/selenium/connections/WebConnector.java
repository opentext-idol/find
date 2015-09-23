package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.BlankWizardStep;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;

import java.util.ArrayList;
import java.util.List;

public class WebConnector extends Connector {
    private String url;

    public WebConnector(String url, String name) {
        super(name);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public Wizard makeWizard(NewConnectionPage newConnectionPage) {
        return new WebConnectorWizard(newConnectionPage);
    }

    private class WebConnectorWizard implements Wizard {
        private NewConnectionPage page;
        private List<WizardStep> wizardSteps = new ArrayList<>();
        private int currentStep = 0;

        public WebConnectorWizard(NewConnectionPage newConnectionPage) {
            page = newConnectionPage;
            wizardSteps.add(new ConnectorTypeStep(page, url, name));
            wizardSteps.add(new BlankWizardStep("Connector Configuration"));
            wizardSteps.add(new BlankWizardStep("Index"));
            wizardSteps.add(new BlankWizardStep("Complete"));
        }

        @Override
        public List<WizardStep> getSteps() {
            return wizardSteps;
        }

        @Override
        public WizardStep getCurrentStep() {
            return wizardSteps.get(currentStep);
        }

        @Override
        public void next() {
            currentStep++;
            if (currentStep < 4) {
                page.nextButton().click();
            } else {
                page.finishButton().click();
            }
            page.loadOrFadeWait();
        }

        @Override
        public void cancel() {
            page.cancelButton().click();
            page.loadOrFadeWait();
        }

        @Override
        public void apply() {
            for (WizardStep wizardStep : wizardSteps) {
                wizardStep.apply();
                next();
            }
        }
    }
}
