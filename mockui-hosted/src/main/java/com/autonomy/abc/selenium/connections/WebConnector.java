package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.BlankWizardStep;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.util.Waits;

import java.util.Arrays;
import java.util.List;

public class WebConnector extends Connector {
    private String url;
    private Integer depth;
    private Integer maxPages;
    private Integer duration = 60;
    private Credentials credentials;

    public Integer getDepth() {
        return depth;
    }

    public Integer getMaxPages() {
        return maxPages;
    }

    public Integer getDuration() {
        return duration;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public WebConnector(String url, String name) {
        super(name);
        this.url = url;
    }

    public WebConnector(String url, String name, Index index){
        super(name, index);
        this.url = url;
    }

    public WebConnector(String url, String name, Credentials credentials){
        this(url, name);
        this.credentials = credentials;
    }

    public WebConnector(String url, String name, Index index, Credentials credentials){
        this(url, name, index);
        this.credentials = credentials;
    }

    public WebConnector withDepth(Integer depth){
        this.depth = depth;
        return this;
    }

    public WebConnector maxPages(Integer maxPages){
        this.maxPages = maxPages;
        return this;
    }

    public WebConnector withDuration(Integer duration) {
        this.duration = duration;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
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
                new WebConnectorConfigStep(newConnectionPage, this),
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

    @Override
    public String toString() {
        return "WebConnector<" + getName() + "|" + getUrl() + ">";
    }
}
