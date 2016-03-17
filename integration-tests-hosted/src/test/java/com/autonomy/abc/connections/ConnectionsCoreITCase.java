package com.autonomy.abc.connections;

import com.autonomy.abc.config.HSODTearDown;
import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.categories.CoreFeature;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.connections.Connector;
import com.autonomy.abc.selenium.connections.WebConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.matchers.CommonMatchers.hasItemThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static org.hamcrest.Matchers.not;
import static org.openqa.selenium.lift.Matchers.displayed;

@Category(CoreFeature.class)
public class ConnectionsCoreITCase extends HostedTestBase {
    private ConnectionService connectionService;

    public ConnectionsCoreITCase(TestConfig config) {
        super(config);
        useIndexTestsUser();
    }

    @Before
    public void setUp() {
        connectionService = getApplication().connectionService();
    }

    @After
    public void tearDown() {
        HSODTearDown.INDEXES.tearDown(this);
    }

    @Test
    public void testCreateWebConnector() {
        Connector connector = new WebConnector("http://www.bbc.co.uk", "connect").withDepth(0).withDuration(60);
        connectionService.setUpConnection(connector);
        ConnectionsDetailPage detailPage = connectionService.goToDetails(connector);
        assertThat(detailPage.backButton(), displayed());
    }

    @Test
    public void testDeleteConnector() {
        Connector connector = new WebConnector("http://golang.org", "gogogo").withDepth(0).withDuration(60);
        connectionService.setUpConnection(connector);
        connectionService.deleteConnection(connector, true);
        assertThat(
                connectionService.goToConnections().connectionsList(),
                not(hasItemThat(containsText(connector.getName()))));
    }

}
