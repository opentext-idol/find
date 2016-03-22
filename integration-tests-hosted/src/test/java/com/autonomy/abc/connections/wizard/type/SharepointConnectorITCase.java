package com.autonomy.abc.connections.wizard.type;

import com.autonomy.abc.base.HostedTestBase;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.connections.*;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.openqa.selenium.lift.Matchers.displayed;

public class SharepointConnectorITCase extends HostedTestBase {

    private SharepointConnector connector;
    private NewConnectionPage newConnectionPage;

    public SharepointConnectorITCase(TestConfig config) {
        super(config);
        useIndexTestsUser();
    }

    @Before
    public void setUp(){
        ConnectionsPage connectionsPage = getApplication().switchTo(ConnectionsPage.class);
        connectionsPage.newConnectionButton().click();

        newConnectionPage = getElementFactory().getNewConnectionPage();
        connector = new SharepointConnector("http://www.bbc.co.uk", "esposito", "ryan", "castle", "stanakatic@katebeckett.com", false, SharepointCredentialsConfigurations.URLType.WEB_APP);
    }

    @After
    public void tearDown(){
        getApplication().connectionService().deleteAllConnections(true);
        getApplication().indexService().deleteAllIndexes();
    }

    @Test
    public void testRequiredFields(){
        Wizard wizard = connector.makeWizard(newConnectionPage);

        wizard.getCurrentStep().apply();
        wizard.next();

        newConnectionPage.nextButton().click();

        SharepointCredentialsConfigurations cc = newConnectionPage.getConnectorConfigStep().getSharepointCredentialsConfigurations();
        FormInput emailInput = cc.notificationEmailInput();

        for(FormInput input : Arrays.asList(cc.userNameInput(), cc.passwordInput(), emailInput)){
            verifyThat(input.getErrorMessage(), is("Required"));
        }

        emailInput.setValue("abc123");
        verifyThat(emailInput.getErrorMessage(), is("Invalid Value"));

        wizard.getCurrentStep().apply();
        wizard.next();

        verifyThat(newConnectionPage.getIndexStep().selectIndexButton(), displayed());
    }

    @Test
    @Ignore("Can't create sharepoint connectors")
    public void testCreated(){
        connector.makeWizard(newConnectionPage).apply();

        new WebDriverWait(getDriver(), 10)
                .withMessage("starting connection")
                .until(GritterNotice.notificationContaining("started"));

        new WebDriverWait(getDriver(), 600)
                .withMessage("running connection " + connector)
                .until(GritterNotice.notificationContaining(connector.getFinishedNotification()));

        verifyThat(getElementFactory().getConnectionsPage().getConnectionNames(), hasItem(connector.getName()));
    }

    @Test
    @KnownBug("CSA-2096")
    @Ignore("Documentation link disappeared")
    public void testDocumentationLink(){
        SharepointCompleteStepTab completeStep = goToLastStep();

        completeStep.downloadAgentButton().click();
        Waits.loadOrFadeWait();
        completeStep.agentInformationButton().click();
        Waits.loadOrFadeWait();

        getMainSession().switchWindow(1);

        verifyThat(getDriver().getCurrentUrl(), not(containsString(".int.")));
        verifyThat(getDriver().getPageSource(), not(containsString("404")));

        getWindow().close();
        getMainSession().switchWindow(0);

        completeStep.modalCancel().click();
    }

    @Test
    public void testAPIKeyGen(){
        SharepointCompleteStepTab completeStep = goToLastStep();

        completeStep.downloadAgentButton().click();
        Waits.loadOrFadeWait();

        verifyThat(completeStep.apiKey().getAttribute("value"), is(""));

        completeStep.generateAPIKeyButton().click();
        Waits.loadOrFadeWait();

        verifyThat(completeStep.apiKey().getAttribute("value"), not(""));

        completeStep.modalCancel().click();
    }

    @Test
    @KnownBug("CSA-2097")
    public void testNoLinuxOption(){
        SharepointCompleteStepTab completeStep = goToLastStep();

        completeStep.downloadAgentButton().click();

        assertThat(completeStep.osVersion().getOptions().size(), is(1));
        assertThat(completeStep.osVersion().getOptions().get(0).getText(), is("Windows"));

        completeStep.modalCancel().click();
    }

    private SharepointCompleteStepTab goToLastStep(){
        Wizard wizard = connector.makeWizard(newConnectionPage);

        for(int i = 0; i < 3; i++){
            wizard.getCurrentStep().apply();
            wizard.next();
        }

        return new SharepointCompleteStepTab(getDriver());
    }
}
