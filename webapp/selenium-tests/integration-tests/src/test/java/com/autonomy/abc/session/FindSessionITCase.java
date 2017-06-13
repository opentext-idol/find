package com.autonomy.abc.session;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.results.DocumentViewer;
import com.autonomy.abc.selenium.find.results.FindResult;
import com.autonomy.abc.selenium.find.results.ListView;
import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Frame;
import com.hp.autonomy.frontend.selenium.framework.Session.SessionReuse;
import com.hp.autonomy.frontend.selenium.framework.Session.SessionReuseParam;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assumeThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@SessionReuse(SessionReuseParam.CLEAN)
public class FindSessionITCase extends FindTestBase {
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
        assumeThat("Should only run on prem", !isHosted());
        deleteCookies();
        try {
            findService.search("XYZ");
        } catch (final NoSuchElementException | StaleElementReferenceException | TimeoutException ignored) {
            /* Probably refreshed page quicker than .search could complete */
        }
        verifyThat(getDriver().getCurrentUrl(), containsString("login"));
    }

    @Test
    public void testDocumentPreview(){
        assumeThat(((HasCapabilities) getDriver()).getCapabilities().getBrowserName(), is("firefox"));

        final ListView results = findService.search("The Season");
        final FindResult searchResult = results.searchResult(1);

        deleteCookies();

        final DocumentViewer docViewer = searchResult.openDocumentPreview();
        final Frame frame = new Frame(getDriver(), docViewer.frame());
        frame.operateOnContent(content -> {
            verifyThat("Authentication Fail frame displayed correctly", content, allOf(
                    containsText("403"),
                    containsText("Authentication Failed"),
                    containsText("You do not have permission to view this page")
            ));
            return null;
        });
    }

    @Test
    @Role(UserRole.FIND)
    public void testRelatedConcepts(){
        //TODO try to update qa-infrastructure with assumeThat(reason, Matcher<Bool>)
            //At the moment getting ambiguous call errors via this method
        assumeThat("Runs only on-prem", !isHosted());

        findService.search("Come and Gone");

        deleteCookies();

        getElementFactory().getRelatedConceptsPanel().concept(0).click();

        Waits.loadOrFadeWait();
        verifyThat(getDriver().getCurrentUrl(), containsString("login"));
    }

    @Test
    @Role(UserRole.FIND)
    public void testSearchSurvivesLogin() {
        final LoginService loginService = getApplication().loginService();
        loginService.logout();

        final String query = "cat";
        // Navigate to the search page when logged out to store the "cat" query in the application's request cache for our session
        navigateToAppUrl(findService.getQueryUrl(query));

        loginService.login(getInitialUser());
        assertThat(getElementFactory().getSearchBox().getValue(), is(query));
    }

    private void deleteCookies(){
        getDriver().manage().deleteAllCookies();
    }
}
