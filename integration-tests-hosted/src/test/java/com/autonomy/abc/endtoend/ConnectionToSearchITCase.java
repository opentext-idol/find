package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.HSODTearDown;
import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.connections.ConnectionsPage;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.control.Frame;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.element.Dropdown;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.Matchers.*;

public class ConnectionToSearchITCase extends HostedTestBase {
    private ConnectionService connectionService;
    private SearchService searchService;

    private final WebConnector connector = new WebConnector("http://www.havenondemand.com", "hod");
    private final Index index = new Index("hod");

    public ConnectionToSearchITCase(TestConfig config) {
        super(config);
        setInitialUser(config.getUser("index_tests"));
    }

    @Before
    public void setUp() {
        connectionService = getApplication().connectionService();
        searchService = getApplication().searchService();
    }

    @After
    public void tearDown() {
        HSODTearDown.INDEXES.tearDown(this);
    }

    @Test
    public void testConnectionToSearch() {
        connectionService.setUpConnection(connector);
        String searchTerm = "haven";
        SearchPage searchPage = searchService.search(new Query(searchTerm).withFilter(new IndexFilter(index)));
        verifyThat("index shows up on search page", searchPage.indexesTree().getSelected(), hasItem(index));
        verifyThat("index has search results", searchPage.getHeadingResultsCount(), greaterThan(0));

        searchPage.getSearchResult(1).title().click();
        DocumentViewer documentViewer = DocumentViewer.make(getDriver());
        Frame frame = new Frame(getWindow(), documentViewer.frame());
        verifyThat("search result in correct index", documentViewer.getIndex(), is(index));

        verifyThat("search result is viewable", frame.getText(), containsString(searchTerm));

        documentViewer.close();

        ConnectionsDetailPage connectionsDetailPage = connectionService.goToDetails(connector);
        connectionsDetailPage.backButton().click();
        ConnectionsPage connectionsPage = getElementFactory().getConnectionsPage();

        FormInput input = connectionsPage.connectionFilterBox();
        Dropdown dropdown = connectionsPage.connectionFilterDropdown();
        input.setValue(connector.getName());
        dropdown.select("Web");
        verifyThat("connection shows up in correct filter", connectionsPage.connectionsList(), not(empty()));
        dropdown.select("Dropbox");
        verifyThat("connection does not show up in incorrect filter", connectionsPage.connectionsList(), empty());
        input.clear();
        dropdown.select("All Types");
    }
}
