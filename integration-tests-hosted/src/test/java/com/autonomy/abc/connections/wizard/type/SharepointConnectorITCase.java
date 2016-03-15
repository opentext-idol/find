package com.autonomy.abc.connections.wizard.type;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.connections.ConnectionsPage;
import com.autonomy.abc.selenium.connections.CredentialsConfigurations;
import com.autonomy.abc.selenium.connections.NewConnectionPage;
import com.autonomy.abc.selenium.connections.SharepointConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
        connector = new SharepointConnector("http://www.bbc.co.uk", "esposito", "ryan", "castle", "stanakatic@katebeckett.com", false, CredentialsConfigurations.URLType.WEB_APP);
    }

    @After
    public void tearDown(){
        getApplication().connectionService().deleteAllConnections(true);
        getApplication().indexService().deleteAllIndexes();
    }

    @Test
    public void testRequiredFields(){
        connector.makeWizard(newConnectionPage).apply();
    }

}
