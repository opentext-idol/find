package com.autonomy.abc.indexes;

import com.autonomy.abc.base.HSODTearDown;
import com.autonomy.abc.base.HostedTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.selenium.actions.wizard.Wizard;
import com.autonomy.abc.selenium.actions.wizard.WizardStep;
import com.autonomy.abc.selenium.analytics.AnalyticsPage;
import com.autonomy.abc.selenium.connections.ConnectionsPage;
import com.autonomy.abc.selenium.connections.Connector;
import com.autonomy.abc.selenium.connections.NewConnectionPage;
import com.autonomy.abc.selenium.connections.WebConnector;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.HSODFind;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.indexes.IndexService;
import com.autonomy.abc.selenium.indexes.IndexesPage;
import com.autonomy.abc.selenium.indexes.tree.IndexCategoryNode;
import com.autonomy.abc.selenium.indexes.tree.IndexNodeElement;
import com.autonomy.abc.selenium.util.SOPageUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.autonomy.abc.framework.TestStateAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

public class IndexDisplayNameITCase extends HostedTestBase {
    private IndexService indexService;
    private IndexesPage indexesPage;
    private Index testIndex;

    public IndexDisplayNameITCase(TestConfig config) {
        super(config);
        useIndexTestsUser();
    }

    @Before
    public void setUp(){
        indexService = getApplication().indexService();
        testIndex = new Index("thisisabittooyobbish5me","D1spl4y Nam3 AbC 123");
        indexesPage = indexService.setUpIndex(testIndex);
    }

    @After
    public void tearDown(){
        HSODTearDown.INDEXES.tearDown(this);
    }

    @Test
    public void testIndexPageList(){
        verifyThat(indexesPage.getIndexDisplayNames(), hasItem(testIndex.getDisplayName()));
    }

    @Test
    public void testSearchFilter(){
        getElementFactory().getTopNavBar().search("Crickets Throw Their Voices");
        verifyIndexOrDefault(getElementFactory().getSearchPage().indexesTree().privateIndexes());
    }

    @Test
    @KnownBug("CSA-2020")
    public void testPieChartLink(){
        AnalyticsPage analyticsPage = getApplication().switchTo(AnalyticsPage.class);
        analyticsPage.indexSizeChart().click();

        verifyThat(SOPageUtil.getWrapperContent(getDriver()), not(containsText(Errors.Index.INVALID_INDEX)));
    }

    @Test
    public void testFindIndex(){
        Window searchWindow = getWindow();
        HSODFind findApp = new HSODFind();
        Window findWindow = launchInNewWindow(findApp);

        try {
            FindPage findPage = findApp.elementFactory().getFindPage();
            findApp.findService().search("This woman's work");

            verifyIndexOrDefault(findPage.indexesTree().privateIndexes());
        } finally {
            findWindow.close();
            searchWindow.activate();
        }
    }

    private void verifyIndexOrDefault(IndexCategoryNode category){
        for(IndexNodeElement node : category){
            verifyThat(node.getName(), anyOf(is(Index.DEFAULT.getDisplayName()), is(testIndex.getDisplayName())));
        }
    }

    @Test
    public void testConnectionsIndex(){
        Connector connector = new WebConnector("http://www.bbc.co.uk", "bbc", testIndex).withDuration(60);

        getApplication().switchTo(ConnectionsPage.class).newConnectionButton().click();

        NewConnectionPage newConnectionPage = getElementFactory().getNewConnectionPage();

        Wizard wizard = connector.makeWizard(newConnectionPage);

        try {
            for (WizardStep step : wizard.getSteps()) {
                wizard.getCurrentStep().apply();

                if (step.getTitle().contains("Index")) {
                    //TODO change this to make sure that the indexes are equal in general if that's ever something that can happen
                    verifyThat(newConnectionPage.getIndexStep().getChosenIndexOnPage().getDisplayName(), is(testIndex.getDisplayName()));
                }

                wizard.next();
            }

            new WebDriverWait(getDriver(), 300).withMessage("connection " + connector + " timed out").until(GritterNotice.notificationContaining(connector.getFinishedNotification()));

            verifyThat(getElementFactory().getConnectionsPage().getIndexOf(connector), is(testIndex.getDisplayName()));
        } finally {
            getApplication().connectionService().deleteAllConnections(true);
        }
    }
}
