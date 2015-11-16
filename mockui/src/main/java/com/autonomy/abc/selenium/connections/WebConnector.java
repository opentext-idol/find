package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.BlankWizardStep;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;

public class WebConnector extends Connector {
    private String url;

    public WebConnector(String url, String name) {
        super(name);
        this.url = url;
    }

    public WebConnector(String url, String name, Index index){
        super(name, index);
    }

    public String getUrl() {
        return url;
    }

    @Override
    public Wizard makeWizard(NewConnectionPage newConnectionPage) {
        return new WebConnectorWizard(newConnectionPage);
    }

    private class WebConnectorWizard extends Wizard {
        private NewConnectionPage page;

        public WebConnectorWizard(NewConnectionPage newConnectionPage) {
            super();
            page = newConnectionPage;
            add(new ConnectorTypeStep(page, url, name));
            add(new BlankWizardStep("Connector Configuration"));
            add(new ConnectorIndexStep(page,index,name));
            add(new BlankWizardStep("Complete"));
        }


        @Override
        public void next() {
            if (onFinalStep()) {
                page.finishButton().click();
            } else {
                page.nextButton().click();
                incrementStep();
            }
            page.loadOrFadeWait();
        }

        @Override
        public void cancel() {
            page.cancelButton().click();
            page.loadOrFadeWait();
        }
    }

    @Override
    public String toString() {
        return "WebConnector<" + getName() + "|" + getUrl() + ">";
    }
}
