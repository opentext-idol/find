package com.autonomy.abc.session;

import com.autonomy.abc.config.FindTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.selenium.control.Frame;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.find.FindSearchResult;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.util.DriverUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.List;

import static com.autonomy.abc.framework.TestStateAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;
import static org.openqa.selenium.lift.Matchers.displayed;

@RelatedTo("CSA-1567")
public class FindSessionITCase extends FindTestBase {
    private FindResultsPage results;
    private FindService findService;

    public FindSessionITCase(TestConfig config) {
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
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            /* Probably refreshed page quicker than .search could complete */
        }
        verifyRefreshedSession();
    }

    @Test
    public void testDocumentPreview(){
        assumeThat(((RemoteWebDriver) getDriver()).getCapabilities().getBrowserName(), is("firefox"));

        results = findService.search("The Season");
        FindSearchResult searchResult = results.searchResult(1);

        deleteCookies();

        DocumentViewer docViewer = searchResult.openDocumentPreview();
        Frame frame = new Frame(getWindow(), docViewer.frame());
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
        List<WebElement> relatedConcepts = results.relatedConcepts();

        deleteCookies();

        DriverUtil.hover(getDriver(), relatedConcepts.get(0));

        Waits.loadOrFadeWait();
        verifyRefreshedSession();
    }

    private void verifyRefreshedSession(){
        verifyThat("Directed to splash screen", getElementFactory().getFindPage().footerLogo(), displayed());
    }

    private void deleteCookies(){
        getDriver().manage().deleteAllCookies();
    }
}
