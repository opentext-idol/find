package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.find.*;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.shared.SharedPreviewTests;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Frame;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBug;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.url;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.urlContains;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsIgnoringCase;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;
import static org.junit.Assume.assumeThat;
import static org.openqa.selenium.lift.Matchers.displayed;

@RelatedTo("CSA-2090")
//TODO have this extend FindITCase but change the setUp()?
public class SimilarDocumentsITCase extends FindTestBase {
    private FindPage findPage;
    private FindResultsPage results;
    private FindService findService;
    private SimilarDocumentsView similarDocuments;

    public SimilarDocumentsITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        findPage=getElementFactory().getFindPage();
        results = getElementFactory().getResultsPage();
        findService = getApplication().findService();
    }

    @Test
    public void testSimilarDocumentsShowUp() throws InterruptedException {
        findService.search(new Query("Doe"));

        for (int i = 1; i <= 5; i++) {
            String title = results.getResult(i).getTitleString();
            similarDocuments = findService.goToSimilarDocuments(i);

            verifyThat(getWindow(), urlContains("suggest"));

            verifyThat(similarDocuments.getTitle(),allOf(containsIgnoringCase("Similar results"),containsIgnoringCase("\"" + title + "\"")));
            verifyThat(similarDocuments.getTotalResults(), greaterThan(0));
            verifyThat(similarDocuments.getResults(1), not(empty()));

            similarDocuments.backButton().click();
        }
    }

    @Test
    @KnownBug("CSA-3678")
    public void testTitle(){
        findService.search(new Query("Bill Murray"));

        for(int i = 1; i <= 5; i++){
            similarDocuments = findService.goToSimilarDocuments(i);
            WebElement seedLink = similarDocuments.seedLink();
            verifyThat(seedLink, displayed());
            verifyThat(seedLink.getText(), not(isEmptyOrNullString()));
            similarDocuments.backButton().click();
        }
    }

    @Test
    public void testPreviewSeed() throws InterruptedException {
        findService.search(new Query("bart"));

        for (int i = 1; i <= 5; i++) {
            similarDocuments = findService.goToSimilarDocuments(i);
            WebElement seedLink  = similarDocuments.seedLink();
            String seedTitle = seedLink.getText();

            if (isHosted()){
                previewSeedHosted(seedLink,seedTitle);
            }
            else{
                previewSeedOnPrem(seedLink,seedTitle);
            }
            similarDocuments.backButton().click();
        }
    }

    //this is bad but the behaviour is so different...
    private void previewSeedHosted(WebElement seedLink, String seedTitle){
        Window firstWindow = getWindow();
        Window secondWindow = openSeed(seedLink);

        verifyThat("opened in new tab", secondWindow, not(firstWindow));
        verifyThat(getDriver().getTitle(), containsString(seedTitle));
        verifyThat("not using viewserver", getWindow(), url(not(containsString("viewDocument"))));
        //TODO check if 500

        if (secondWindow != null) {
                secondWindow.close();
            }
        firstWindow.activate();
    }

    private void previewSeedOnPrem(WebElement seedLink,String seedTitle){
        seedLink.click();
        verifyThat("SeedLink goes to detailed document preview",getDriver().getCurrentUrl(),containsString("document"));

    }
    private Window openSeed(WebElement seedLink) {
        final int windowCount = getMainSession().countWindows();
        final Window currentWindow = getWindow();

        seedLink.click();
        new WebDriverWait(getDriver(), 5)
                .withMessage("opening seed document")
                .until(getMainSession().windowCountIs(windowCount + 1));

        Window secondWindow = null;
        for (Window openWindow : getMainSession()) {
            if (!openWindow.equals(currentWindow)) {
                openWindow.activate();
                secondWindow = openWindow;
            }
        }
        return secondWindow;
    }

    @Test
    @KnownBug("CCUK-3676")
    public void testPublicIndexesSimilarDocs(){
        assumeThat(getConfig().getType(), Matchers.is(ApplicationType.HOSTED));

        findService.search(new Query("Hammertime").withFilter(IndexFilter.PUBLIC));

        for(int i = 1; i <= 5; i++){
            verifySimilarDocsNotEmpty(i);
        }
    }

    private void verifySimilarDocsNotEmpty(int i) {
        similarDocuments = findService.goToSimilarDocuments(i);
        verifyThat(similarDocuments.mainResultsContent().getText(), not(isEmptyOrNullString()));
        similarDocuments.backButton().click();
    }

    @Test
    public void testSimilarDocumentsFromSimilarDocuments(){
        findService.search("Self Defence Family");

        similarDocuments = findService.goToSimilarDocuments(1);
        assumeThat(similarDocuments.getResults().size(), not(0));

        String previousTitle = similarDocuments.seedLink().getText();
        for(int i = 0; i < 5; i++) {
            //Generate a random number between 1 and 5
            int number = (int) (Math.random() * 5 + 1);

            FindResult doc = similarDocuments.getResult(number);
            String docTitle = doc.getTitleString();

            doc.similarDocuments().click();
            Waits.loadOrFadeWait();
            similarDocuments = getElementFactory().getSimilarDocumentsView();

            verifyThat("Going from " + previousTitle + " to " + docTitle + " worked successfully",similarDocuments.seedLink(), containsText(docTitle));

            previousTitle = docTitle;
        }
        //TODO what is meant to happen when clicking back
    }

    @Test
    public void testInfiniteScroll(){
        results = findService.search(new Query("blast").withFilter(IndexFilter.ALL));

        similarDocuments = findService.goToSimilarDocuments(1);
        assumeThat(similarDocuments.getResults().size(), is(30));

        for(int i = 30; i <= 150; i += 30) {
            verifyThat(similarDocuments.getVisibleResultsCount(), is(i));
            DocumentViewer documentViewer = similarDocuments.getResult(i).openDocumentPreview();
            assertThat("Have opened preview container",documentViewer.previewPresent());
            documentViewer.close();
            verifyThat(similarDocuments.getVisibleResultsCount(),is(i+30));
            results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        }
    }

    @Test
    public void testSortByDate() throws ParseException {

        assumeThat(getConfig().getType(), Matchers.is(ApplicationType.ON_PREM));

        findService.search(new Query("Fade"));
        similarDocuments = findService.goToSimilarDocuments(1);
        similarDocuments.sortByDate();
        List<FindResult> searchResults = similarDocuments.getResults();

        Date previousDate = null;

        for(int i = 1; i <= 10; i++){
            String badFormatDate = searchResults.get(i).getDate();
            String date = similarDocuments.convertDate(badFormatDate);
            Date currentDate = SimilarDocumentsView.DATE_FORMAT.parse(date);

            if(previousDate != null){
                verifyThat(currentDate, lessThanOrEqualTo(previousDate));
            }

            previousDate = currentDate;
        }
    }

    @Test
    public void testDocumentPreview(){
        if (isHosted()){
            findService.search(new Query("stars").withFilter(new IndexFilter(Index.DEFAULT)));
            similarDocuments = findService.goToSimilarDocuments(1);
            SharedPreviewTests.testDocumentPreviews(getMainSession(), similarDocuments.getResults(5), Index.DEFAULT);
        }
        else{
            findService.search(new Query("stars"));
            similarDocuments = findService.goToSimilarDocuments(1);
            testIdolDocPreview(similarDocuments.getResults(5));
        }

    }
    private void testIdolDocPreview(List<FindResult> results) {
        for (FindResult result : results) {
            DocumentViewer docPreview = result.openDocumentPreview();

            assertThat("Have opened preview container",docPreview.previewPresent());
            verifyThat("Preview not stuck loading", !similarDocuments.loadingIndicator().isDisplayed());
            verifyThat("There is content in preview", similarDocuments.previewContents().getText(), not(isEmptyOrNullString()));
            verifyThat("Index displayed", docPreview.getIndex(), not(nullValue()));
            verifyThat("Reference displayed", docPreview.getReference(), not(nullValue()));

            Frame previewFrame = new Frame(getWindow(), docPreview.frame());
            String frameText = previewFrame.getText();

            verifyThat("Preview document has content", frameText, not(isEmptyOrNullString()));
            assertThat("Preview document has no error", previewFrame.getText(), not(containsString("encountered an error")));

            docPreview.close();
        }
    }
}
