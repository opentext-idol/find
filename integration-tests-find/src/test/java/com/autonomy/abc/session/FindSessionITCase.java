package com.autonomy.abc.session;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.results.FindResult;
import com.autonomy.abc.selenium.find.results.ResultsView;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Frame;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.remote.RemoteWebDriver;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.urlContains;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;
import static org.openqa.selenium.lift.Matchers.displayed;

@RelatedTo("CSA-1567")
public class FindSessionITCase extends FindTestBase {
    private ResultsView results;
    private FindService findService;

    public FindSessionITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        findService = getApplication().findService();
    }

    @Test
    public void testSearch(){
        deleteCookies();
        try {
            findService.search("XYZ");
        } catch (NoSuchElementException | StaleElementReferenceException | TimeoutException e) {
            /* Probably refreshed page quicker than .search could complete */
        }
        verifyRefreshedSession();
    }

    @Test
    public void testDocumentPreview(){
        assumeThat(((RemoteWebDriver) getDriver()).getCapabilities().getBrowserName(), is("firefox"));

        results = findService.search("The Season");
        final FindResult searchResult = results.searchResult(1);

        deleteCookies();

        final DocumentViewer docViewer = searchResult.openDocumentPreview();
        final Frame frame = new Frame(getWindow(), docViewer.frame());
        frame.activate();

        verifyThat("Authentication Fail frame displayed correctly", frame.content(), allOf(
                containsText("403"),
                containsText("Authentication Failed"),
                containsText("You do not have permission to view this page")
        ));

        frame.deactivate();
    }

    @Test
    public void testRelatedConcepts(){
        results = findService.search("Come and Gone");

        deleteCookies();

        getElementFactory().getRelatedConceptsPanel().hoverOverRelatedConcept(0);

        Waits.loadOrFadeWait();
        verifyRefreshedSession();
    }

    private void verifyRefreshedSession(){
        if (isHosted()) {
            verifyThat("Directed to splash screen", getElementFactory().getFindPage().footerLogo(), displayed());
        } else {
            verifyThat(getWindow(), urlContains("login"));
        }
    }

    private void deleteCookies(){
        getDriver().manage().deleteAllCookies();
    }
}
