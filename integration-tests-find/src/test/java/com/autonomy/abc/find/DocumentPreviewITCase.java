package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.preview.DetailedPreviewPage;
import com.autonomy.abc.selenium.find.preview.InlinePreview;
import com.autonomy.abc.selenium.find.results.FindResultsPage;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.QueryResult;
import com.hp.autonomy.frontend.selenium.config.Browser;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Frame;
import com.hp.autonomy.frontend.selenium.control.Session;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;
import static org.openqa.selenium.lift.Matchers.displayed;

public class DocumentPreviewITCase extends FindTestBase {
    private FindPage findPage;
    private FindResultsPage results;
    private FindService findService;

    public DocumentPreviewITCase(final TestConfig config) {
        super(config);}

    @Before
    public void setUp(){
        findPage = getElementFactory().getFindPage();
        results = getElementFactory().getResultsPage();
        findService = getApplication().findService();
    }

    @Test
    public void testShowDocumentPreview(){
        findService.search("cake");
        findPage.filterBy(new IndexFilter(filters().getIndex(1).getName()));

        final DocumentViewer docPreview = results.searchResult(1).openDocumentPreview();
        final InlinePreview inlinePreview = getElementFactory().getInlinePreview();

        if (inlinePreview.loadingIndicatorExists()) {
            assertThat("Preview not stuck loading", inlinePreview.loadingIndicator(), not(displayed()));
        }
        assertThat("There is content in preview", inlinePreview.getContents(), not(isEmptyOrNullString()));
        assertThat("Index displayed", docPreview.getIndex(),not(nullValue()));
        assertThat("Reference displayed",docPreview.getReference(),not(nullValue()));

        final Frame previewFrame = new Frame(getWindow(), docPreview.frame());
        final String frameText=previewFrame.getText();

        verifyThat("Preview document has content",frameText,not(isEmptyOrNullString()));
        assertThat("Preview document has no error",previewFrame.getText(),not(containsString("encountered an error")));

        docPreview.close();
    }

    @Test
    public void testOpenOriginalDocInNewTab(){
        final Session session = getMainSession();

        findService.search("flail");
        if (isHosted()) {
            // e.g. FIFA contains links that redirect to new pages
            findPage.filterBy(new IndexFilter("simpsonsarchive"));
        }

        for (final QueryResult queryResult : results.getResults(4)) {
            final DocumentViewer docViewer = queryResult.openDocumentPreview();
            final String reference = docViewer.getReference();

            getElementFactory().getInlinePreview().openDetailedPreview();
            final DetailedPreviewPage detailedPreviewPage = getElementFactory().getDetailedPreview();

            final Window original = session.getActiveWindow();
            detailedPreviewPage.openOriginalDoc();
            final Window newWindow = session.switchWindow(session.countWindows() - 1);
            newWindow.activate();
            Waits.loadOrFadeWait();
            verifyThat(session.getDriver().getCurrentUrl(), is(reformatReference(reference)));

            newWindow.close();
            original.activate();

            detailedPreviewPage.goBackToSearch();
        }
    }

    private String reformatReference(final String badFormatReference){
        return badFormatReference.replace(" ","_");
    }

    @Test
    public void testDetailedPreview() {
        findService.search("tragic");
        findPage.filterBy(new IndexFilter(filters().getIndex(1).getName()));

        results.getResult(1).openDocumentPreview();
        getElementFactory().getInlinePreview().openDetailedPreview();

        final DetailedPreviewPage detailedPreviewPage = getElementFactory().getDetailedPreview();

        //loading
        final String frameText = new Frame(getMainSession().getActiveWindow(), detailedPreviewPage.frame()).getText();
        verifyThat("Frame has content", frameText, not(isEmptyOrNullString()));
        verifyThat("Preview frame has no error",frameText,not(containsString("encountered an error")));

        checkHasMetaDataFields(detailedPreviewPage);

        checkSimilarDocuments(detailedPreviewPage);

        checkSimilarDates(detailedPreviewPage);

        detailedPreviewPage.goBackToSearch();

    }

    private void checkHasMetaDataFields(final DetailedPreviewPage detailedPreviewPage){
        verifyThat("Tab loads",!(detailedPreviewPage.loadingIndicator().isDisplayed()));
        verifyThat("Detailed Preview has reference",detailedPreviewPage.getReference(),not(nullValue()));
        if(isHosted()){
        verifyThat("Detailed Preview has index",detailedPreviewPage.getIndex(),not(nullValue()));}
        else{verifyThat("Detailed Preview has database",detailedPreviewPage.getDatabase(),not(nullValue()));}
        verifyThat("Detailed Preview has title",detailedPreviewPage.getTitle(),not(nullValue()));
        verifyThat("Detailed Preview has summary", detailedPreviewPage.getSummary(), not(nullValue()));
//        verifyThat("Detailed Preview has date",detailedPreviewPage.getDate(),not(nullValue()));
    }

    private void checkSimilarDocuments(final DetailedPreviewPage detailedPreviewPage){
        detailedPreviewPage.similarDocsTab().click();
        verifyThat("Tab loads",!(detailedPreviewPage.loadingIndicator().isDisplayed()));
    }

    private void checkSimilarDates(final DetailedPreviewPage detailedPreviewPage){
        detailedPreviewPage.similarDatesTab().click();
        verifyThat("Tab loads",!(detailedPreviewPage.loadingIndicator().isDisplayed()));
        changeDateSliderToYearBefore(detailedPreviewPage);
        verifyThat("Can change to similar docs from year before", detailedPreviewPage.getSimilarDatesSummary(), containsString("Between 1 year"));
    }

    private void changeDateSliderToYearBefore(final DetailedPreviewPage detailedPreviewPage){
        detailedPreviewPage.ithTick(1).click();
    }

    @Test
    @ActiveBug(value = "FIND-86", browsers = Browser.FIREFOX)
    public void testOneCopyOfDocInDetailedPreview(){
        findService.search("face");
        results.getResult(1).openDocumentPreview();

        getElementFactory().getInlinePreview().openDetailedPreview();
        final DetailedPreviewPage detailedPreviewPage = getElementFactory().getDetailedPreview();

        verifyThat("Only 1 copy of that document in detailed preview",detailedPreviewPage.numberOfHeadersWithDocTitle(),lessThanOrEqualTo(1));

        detailedPreviewPage.goBackToSearch();

    }

    private FilterPanel filters() {
        return getElementFactory().getFilterPanel();
    }
}

