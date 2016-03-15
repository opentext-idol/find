package com.autonomy.abc.connections.wizard.type;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.connections.*;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.element.GritterNotice;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.hasClass;
import static org.hamcrest.Matchers.hasItem;
import static org.openqa.selenium.lift.Matchers.displayed;

public class SharepointConnectorITCase extends HostedTestBase {

    private SharepointConnector connector;
    private NewConnectionPage newConnectionPage;

    public SharepointConnectorITCase(TestConfig config) {
        super(config);
        setInitialUser(config.getUser("index_tests"));
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
            verifyThat(input.getElement(), hasClass("ng-invalid-required"));
        }

        emailInput.setValue("abc123");
        verifyThat(emailInput.getElement(), hasClass("ng-invalid-email"));

        wizard.getCurrentStep().apply();
        wizard.next();

        verifyThat(newConnectionPage.getIndexStep().selectIndexButton(), displayed());
    }

    @Test
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

}
