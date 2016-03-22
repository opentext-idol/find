package com.autonomy.abc.selenium.connections;

import com.autonomy.abc.selenium.actions.wizard.BlankWizardStep;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.indexes.Index;
import com.hp.autonomy.frontend.selenium.util.Waits;

import java.util.Arrays;
import java.util.List;

public class DropboxConnector extends Connector {
    private final String appKey, accessToken, notificationEmail;
    private final boolean dropboxAccess;

    public DropboxConnector(String name, boolean dropboxAcess, String appKey, String accessToken, String notificationEmail) {
        super(name);
        this.appKey = appKey;
        this.accessToken = accessToken;
        this.notificationEmail = notificationEmail;
        this.dropboxAccess = dropboxAcess;
    }

    public DropboxConnector(String name, boolean dropboxAcess, String appKey, String accessToken, String notificationEmail, Index index) {
        super(name, index);
        this.appKey = appKey;
        this.accessToken = accessToken;
        this.notificationEmail = notificationEmail;
        this.dropboxAccess = dropboxAcess;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getNotificationEmail() {
        return notificationEmail;
    }

    public boolean isDropboxAccess() {
        return dropboxAccess;
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
                new ConnectorTypeStep(newConnectionPage, null, name, this),
                new DropboxConnectorConfigStep(newConnectionPage, this),
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
