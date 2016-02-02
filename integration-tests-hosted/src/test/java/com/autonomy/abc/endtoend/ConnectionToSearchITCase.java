package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.element.Dropdown;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.page.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.search.DocumentViewer;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.SearchQuery;
import com.autonomy.abc.selenium.search.SearchService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.*;

public class ConnectionToSearchITCase extends HostedTestBase {
    private ConnectionsPage connectionsPage;
    private ConnectionsDetailPage connectionsDetailPage;
    private SearchPage searchPage;
    private ConnectionService connectionService;
    private SearchService searchService;

    private final WebConnector connector = new WebConnector("http://www.havenondemand.com", "hod");
    private final Index index = new Index("hod");
    private final String searchTerm = "haven";

    public ConnectionToSearchITCase(TestConfig config) {
        super(config);
        setInitialUser(config.getUser("index_tests"));
    }

    @Before
    public void setUp() {
        connectionService = getApplication().connectionService();
        searchService = getApplication().searchService();
    }

    @Test
    public void testConnectionToSearch() {
        connectionService.setUpConnection(connector);
        searchPage = searchService.search(new SearchQuery(searchTerm).withFilter(new IndexFilter(index)));
        verifyThat("index shows up on search page", searchPage.indexesTree().getSelected(), hasItem(index));
        verifyThat("index has search results", searchPage.getHeadingResultsCount(), greaterThan(0));

        final String handle = getDriver().getWindowHandle();
        searchPage.getSearchResult(1).title().click();
        DocumentViewer documentViewer = DocumentViewer.make(getDriver());
        verifyThat("search result in correct index", documentViewer.getIndex(), is(index));
        getDriver().switchTo().frame(documentViewer.frame());
        verifyThat("search result is viewable", getDriver().findElement(By.xpath(".//*")).getText(), containsString(searchTerm));
        getDriver().switchTo().window(handle);
        documentViewer.close();

        connectionsDetailPage = connectionService.goToDetails(connector);
        connectionsDetailPage.backButton().click();
        connectionsPage = getElementFactory().getConnectionsPage();

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

    @After
    public void tearDown() {
        connectionService.deleteConnection(connector, true);
    }
}
