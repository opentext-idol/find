package com.autonomy.abc.indexes;

import com.autonomy.abc.base.HSODTearDown;
import com.autonomy.abc.base.HostedTestBase;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBug;
import com.autonomy.abc.selenium.connections.ConnectionService;
import com.autonomy.abc.selenium.connections.Connector;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.hp.autonomy.frontend.selenium.control.Resolution;
import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.indexes.IndexesDetailPage;
import com.autonomy.abc.selenium.indexes.IndexesPage;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.urlContains;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class IndexDetailsPageITCase extends HostedTestBase {

    private IndexService indexService;
    private IndexesPage indexesPage;
    private IndexesDetailPage indexesDetailPage;
    private Index indexOne = new Index("one");
    private Index indexTwo = new Index("two");

    public IndexDetailsPageITCase(TestConfig config) {
        super(config);
        useIndexTestsUser();
    }

    @Before
    public void setUp(){
        indexService = getApplication().indexService();
        indexesPage = indexService.setUpIndex(indexOne);
        indexesDetailPage = indexService.goToDetails(indexOne);
    }

    @After
    public void tearDown(){
        HSODTearDown.INDEXES.tearDown(this);
    }

    @Test
    @KnownBug("CSA-1643")
    public void testAssociatedConnections(){
        ConnectionService connectionService = getApplication().connectionService();
        Connector connector = new WebConnector("http://www.bbc.co.uk", "connector", indexOne).withDuration(60);

        indexService.setUpIndex(indexTwo);
        try {
            connectionService.setUpConnection(connector);

            indexesPage = indexService.goToIndexes();
            verifyThat(indexesPage.getNumberOfConnections(indexOne), is(1));
            verifyThat(indexesPage.getNumberOfConnections(indexTwo), is(0));

            indexesDetailPage = indexService.goToDetails(indexOne);
            List<String> associatedConnections = indexesDetailPage.getAssociatedConnectors();
            verifyThat(associatedConnections.size(), is(1));
            verifyThat(associatedConnections, hasItem(connector.getName()));

            connectionService.changeIndex(connector, indexTwo);

            verifyThat(getElementFactory().getConnectionsDetailPage().getIndexName(), is(indexTwo.getName()));

            indexesPage = indexService.goToIndexes();
            verifyThat(indexesPage.getNumberOfConnections(indexOne), is(0));
            verifyThat(indexesPage.getNumberOfConnections(indexTwo), is(1));

            indexesDetailPage = indexService.goToDetails(indexOne);
            verifyThat(indexesDetailPage.getAssociatedConnectors().size(), is(0));

            indexesDetailPage = indexService.goToDetails(indexTwo);
            associatedConnections = indexesDetailPage.getAssociatedConnectors();
            verifyThat(associatedConnections.size(), is(1));
            verifyThat(associatedConnections, hasItem(connector.getName()));
        } finally {
            connectionService.deleteAllConnections(true);
        }
    }

    @Test
    @KnownBug("CSA-1703")
    public void testGraphNoDataMessage(){
        new WebDriverWait(getDriver(),20).until(ExpectedConditions.invisibilityOfElementLocated(By.className("loadingIconSmall")));

        verifyThat(indexesDetailPage.filesIngestedGraph(), containsText("There is no data for this time period"));
    }

    @Test
    @KnownBug("CSA-1685")
    public void testButtonsNotObscuredAfterScroll(){
        getWindow().resize(new Resolution(1100, 700));

        ((JavascriptExecutor) getDriver()).executeScript("window.scrollTo(0, document.body.scrollHeight)");
        Waits.loadOrFadeWait();

        ((JavascriptExecutor) getDriver()).executeScript("window.scrollTo(0, -document.body.scrollHeight)");
        Waits.loadOrFadeWait();

        indexesDetailPage.backButton().click();

        verifyThat(getWindow(), urlContains("indexes"));
    }

    @Test
    public void testDeleteIndex(){
        indexesDetailPage.deleteIndex();
        new WebDriverWait(getDriver(), 30).until(GritterNotice.notificationContaining("Index " + indexOne.getDisplayName() + " successfully deleted"));
        indexesPage = getElementFactory().getIndexesPage();
        verifyThat(indexesPage.getIndexDisplayNames(), not(hasItem(indexOne.getDisplayName())));
    }
}
