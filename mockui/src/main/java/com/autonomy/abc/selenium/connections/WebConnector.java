package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.BlankWizardStep;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.page.connections.NewConnectionPage;
import com.autonomy.abc.selenium.page.connections.wizard.ConnectorConfigStepTab;

public class WebConnector extends Connector {
    private String url;

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

    private Integer depth;
    private Integer maxPages;
    private Integer duration;
    private Credentials credentials;

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

    public WebConnector withDepth(int depth){
        this.depth = depth;
        return this;
    }

    public WebConnector maxPages(int maxPages){
        this.maxPages = maxPages;
        return this;
    }

    public WebConnector withDuration(int duration) {
        this.duration = duration;
        return this;
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
            add(new ConnectorConfigStep(page, WebConnector.this));
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
