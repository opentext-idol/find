package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.find.DetailedPreviewPage;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.query.QueryResult;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Frame;
import com.hp.autonomy.frontend.selenium.control.Session;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collections;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;

public class IdolDocumentPreviewITCase extends FindTestBase {
    private FindPage findPage;
    private FindResultsPage results;
    private FindService findService;

    public IdolDocumentPreviewITCase(TestConfig config) {
        super(config);}

    @Parameterized.Parameters
    public static Iterable<Object[]> parameters() throws IOException {
        return parameters(Collections.singleton(ApplicationType.ON_PREM));
    }

    @Before
    public void setUp(){
        findPage = getElementFactory().getFindPage();
        results = findPage.getResultsPage();
        findService = getApplication().findService();
    }

    @Test
    public void testShowDocumentPreview(){
        findService.search("cake");

        DocumentViewer docPreview = results.searchResult(1).openDocumentPreview();

        if (findPage.loadingIndicatorExists()) {
            assertThat("Preview not stuck loading", !findPage.loadingIndicator().isDisplayed());
        }
        assertThat("There is content in preview",findPage.previewContents().getText(),not(isEmptyOrNullString()));
        assertThat("Index displayed",docPreview.getIndex(),not(nullValue()));
        assertThat("Reference displayed",docPreview.getReference(),not(nullValue()));

        Frame previewFrame = new Frame(getWindow(), docPreview.frame());
        String frameText=previewFrame.getText();

        verifyThat("Preview document has content",frameText,not(isEmptyOrNullString()));
        assertThat("Preview document has no error",previewFrame.getText(),not(containsString("encountered an error")));

        docPreview.close();
    }

    @Test
    public void testOpenOriginalDocInNewTab(){
        Session session = getMainSession();

        findService.search("flail");
        for (QueryResult queryResult : results.getResults(5)) {
            DocumentViewer docViewer = queryResult.openDocumentPreview();
            String reference = docViewer.getReference();

            findPage.openDetailedPreview();
            DetailedPreviewPage detailedPreviewPage = getElementFactory().getDetailedPreview();

            Window original = session.getActiveWindow();
            detailedPreviewPage.openOriginalDoc();
            Window newWindow = session.switchWindow(session.countWindows() - 1);
            newWindow.activate();
            Waits.loadOrFadeWait();
            verifyThat(session.getDriver().getCurrentUrl(), is(reformatReference(reference)));

            newWindow.close();
            original.activate();

            detailedPreviewPage.goBackToSearch();
        }
    }

    private String reformatReference(String badFormatReference){
        return badFormatReference.replace(" ","_");
    }

    @Test
    public void testDetailedPreview() {
        findService.search("tragic");
        results.getResult(1).openDocumentPreview();

        findPage.openDetailedPreview();
        DetailedPreviewPage detailedPreviewPage = getElementFactory().getDetailedPreview();

        //loading
        verifyThat("Preview not stuck loading", !detailedPreviewPage.serverLoadingIndicator().isDisplayed());
        String frameText = new Frame(getMainSession().getActiveWindow(), detailedPreviewPage.frame()).getText();
        verifyThat("Frame has content", frameText, not(isEmptyOrNullString()));
        verifyThat("Preview frame has no error",frameText,not(containsString("encountered an error")));

        checkHasMetaDataFields(detailedPreviewPage);

        checkSimilarDocuments(detailedPreviewPage);

        checkSimilarDates(detailedPreviewPage);

        detailedPreviewPage.goBackToSearch();

    }

    private void checkHasMetaDataFields(DetailedPreviewPage detailedPreviewPage){
        verifyThat("Tab loads",!(detailedPreviewPage.loadingIndicator().isDisplayed()));
        verifyThat("Detailed Preview has reference",detailedPreviewPage.getReference(),not(nullValue()));
        verifyThat("Detailed Preview has index",detailedPreviewPage.getIndex(),not(nullValue()));
        verifyThat("Detailed Preview has title",detailedPreviewPage.getTitle(),not(nullValue()));
        verifyThat("Detailed Preview has summary", detailedPreviewPage.getSummary(), not(nullValue()));
        verifyThat("Detailed Preview has date",detailedPreviewPage.getDate(),not(nullValue()));
    }

    private void checkSimilarDocuments(DetailedPreviewPage detailedPreviewPage){
        detailedPreviewPage.similarDocsTab().click();
        verifyThat("Tab loads",!(detailedPreviewPage.loadingIndicator().isDisplayed()));
    }

    private void checkSimilarDates(DetailedPreviewPage detailedPreviewPage){
        detailedPreviewPage.similarDatesTab().click();
        verifyThat("Tab loads",!(detailedPreviewPage.loadingIndicator().isDisplayed()));
        changeDateSliderToYearBefore(detailedPreviewPage);
        verifyThat("Can change to similar docs from year before", detailedPreviewPage.getSimilarDatesSummary(), containsString("Between 1 year"));
    }

    private void changeDateSliderToYearBefore(DetailedPreviewPage detailedPreviewPage){
        detailedPreviewPage.ithTick(1).click();
    }

    @Test
    @ActiveBug("FIND-86")
    public void testOneCopyOfDocInDetailedPreview(){
        findService.search("face");
        results.getResult(1).openDocumentPreview();

        findPage.openDetailedPreview();
        DetailedPreviewPage detailedPreviewPage = getElementFactory().getDetailedPreview();

        verifyThat("Only 1 copy of that document in detailed preview",detailedPreviewPage.numberOfHeadersWithDocTitle(),lessThanOrEqualTo(1));

        detailedPreviewPage.goBackToSearch();

    }
}
