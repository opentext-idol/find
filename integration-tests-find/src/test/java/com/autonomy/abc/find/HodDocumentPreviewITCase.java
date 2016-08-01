package com.autonomy.abc.find;

import com.autonomy.abc.base.HsodFindTestBase;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.results.FindResult;
import com.autonomy.abc.selenium.find.results.ResultsView;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.shared.SharedPreviewTests;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Frame;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.Locator;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriverException;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static com.thoughtworks.selenium.SeleneseTestBase.fail;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.openqa.selenium.lift.Matchers.displayed;

public class HodDocumentPreviewITCase extends HsodFindTestBase {
    private FindPage findPage;
    private FindService findService;

    public HodDocumentPreviewITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        findPage = getElementFactory().getFindPage();
        findService = getApplication().findService();
    }

    @Test
    @ResolvedBug("CSA-1767 - footer not hidden properly")
    public void testViewDocumentsOpenFromFind(){
        ResultsView results = findService.search("Review");

        for(final FindResult result : results.getResults(5)){
            final DocumentViewer docViewer = result.openDocumentPreview();
            verifyDocumentViewer(docViewer);
            docViewer.close();
        }
    }

    private void verifyDocumentViewer(final DocumentViewer docViewer) {
        final Frame frame = new Frame(getWindow(), docViewer.frame());

        verifyThat("document visible", docViewer, displayed());

        frame.activate();

        final Locator errorHeader = new Locator()
                .withTagName("h1")
                .containingText("500");
        final Locator errorBody = new Locator()
                .withTagName("h2")
                .containingCaseInsensitive("error");
        verifyThat("no backend error", frame.content().findElements(errorHeader), empty());
        verifyThat("no view server error", frame.content().findElements(errorBody), empty());
        frame.deactivate();
    }

    @Test
    public void testBetween30And60Results(){
        ResultsView results = findService.search(new Query("connectors"));
        findPage.filterBy(new IndexFilter("sitesearch"));

        findPage.scrollToBottom();

        verifyThat(results.resultsDiv(), containsText("No more results found"));
    }

    @Test
    @ResolvedBug("CSA-1767 - footer not hidden properly")
    @RelatedTo({"CSA-946", "CSA-1656", "CSA-1657", "CSA-1908"})
    public void testDocumentPreview(){
        final Index index = new Index("fifa");
        ResultsView results = findService.search(new Query("document preview").withFilter(new IndexFilter(index)));

        SharedPreviewTests.testDocumentPreviews(getMainSession(), results.getResults(5), index);
    }

    @Test
    public void testOpenDocumentFromSearch(){
        final Window original = getWindow();

        ResultsView results = findService.search("Refuse to Feel");

        for(int i = 1; i <= 5; i++){
            final FindResult result = results.getResult(i);
            final String reference = result.getReference();
            result.title().click();
            Waits.loadOrFadeWait();
            final Window newWindow = getMainSession().switchWindow(getMainSession().countWindows() - 1);

            verifyThat(getDriver().getCurrentUrl(), containsString(reference));

            if(!newWindow.equals(original)) {
                newWindow.close();
            }

            original.activate();
        }
    }
}
