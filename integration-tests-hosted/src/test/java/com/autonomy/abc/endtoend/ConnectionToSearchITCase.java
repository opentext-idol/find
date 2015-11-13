package com.autonomy.abc.endtoend;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.element.Dropdown;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.connections.ConnectionsDetailPage;
import com.autonomy.abc.selenium.page.connections.ConnectionsPage;
import com.autonomy.abc.selenium.page.search.DocumentViewer;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.Search;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;

public class ConnectionToSearchITCase extends ABCTestBase {
    private ConnectionsPage connectionsPage;
    private ConnectionsDetailPage connectionsDetailPage;
    private SearchPage searchPage;
    private ConnectionService connectionService;
    private SearchActionFactory searchActionFactory;
    private HSOElementFactory elementFactory;

    private final WebConnector connector = new WebConnector("http://www.havenondemand.com", "hod");
    private final String indexName = "hod";
    private final String searchTerm = "haven";

    public ConnectionToSearchITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
        assumeThat(type, is(ApplicationType.HOSTED));
    }

    @Before
    public void setUp() {
        elementFactory = (HSOElementFactory) getElementFactory();
        connectionService = new ConnectionService(getApplication(), getElementFactory());
        searchActionFactory = new SearchActionFactory(getApplication(), getElementFactory());
    }

    @Test
    public void testConnectionToSearch() {
        connectionService.setUpConnection(connector);
        Search search = searchActionFactory.makeSearch(searchTerm);
        search.applyFilter(new IndexFilter(indexName));
        search.apply();
        searchPage = getElementFactory().getSearchPage();
        verifyThat("index shows up on search page", searchPage.getSelectedDatabases(), hasItem(indexName));
        verifyThat("index has search results", searchPage.countSearchResults(), greaterThan(0));

        final String handle = getDriver().getWindowHandle();
        searchPage.getSearchResult(1).click();
        DocumentViewer documentViewer = DocumentViewer.make(getDriver());
        verifyThat("search result in correct index", documentViewer.getIndex(), is(indexName));
        getDriver().switchTo().frame(documentViewer.frame());
        verifyThat("search result is viewable", getDriver().findElement(By.xpath(".//*")).getText(), containsString(searchTerm));
        getDriver().switchTo().window(handle);
        documentViewer.close();

        connectionsDetailPage = connectionService.goToDetails(connector);
        connectionsDetailPage.backButton().click();
        connectionsPage = elementFactory.getConnectionsPage();

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
