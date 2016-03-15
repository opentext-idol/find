package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.BlankWizardStep;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.util.Waits;

import java.util.Arrays;
import java.util.List;

public class SharepointConnector extends Connector {
    private final String url;
    private final String username;
    private final String password;
    private final String notificationEmail;
    private final boolean online;
    private final SharepointCredentialsConfigurations.URLType urlType;

    public SharepointConnector(String url, String name, String username, String password, String notificationEmail, boolean online, SharepointCredentialsConfigurations.URLType urlType) {
        super(name);
        this.url = url;
        this.username = username;
        this.password = password;
        this.notificationEmail = notificationEmail;
        this.online = online;
        this.urlType = urlType;
    }

    public SharepointConnector(String url, String name, String username, String password, String notificationEmail, boolean online, SharepointCredentialsConfigurations.URLType urlType, Index index) {
        super(name, index);
        this.url = url;
        this.username = username;
        this.password = password;
        this.notificationEmail = notificationEmail;
        this.online = online;
        this.urlType = urlType;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNotificationEmail() {
        return notificationEmail;
    }

    public boolean isOnline() {
        return online;
    }

    public SharepointCredentialsConfigurations.URLType getUrlType() {
        return urlType;
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
                new SharepointConnectorConfigStep(newConnectionPage, this),
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
