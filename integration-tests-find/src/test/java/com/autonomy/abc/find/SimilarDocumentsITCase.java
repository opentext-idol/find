package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.hsod.IsoHsodApplication;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.autonomy.abc.shared.SharedPreviewTests;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBug;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.find.FindResult;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.SimilarDocumentsView;
import com.autonomy.abc.selenium.indexes.Index;
import com.autonomy.abc.selenium.promotions.HsodPromotionService;
import com.autonomy.abc.selenium.promotions.Promotion;
import com.autonomy.abc.selenium.promotions.SpotlightPromotion;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.ParametricFilter;
import com.autonomy.abc.selenium.query.Query;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.url;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.urlContains;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeThat;
import static org.openqa.selenium.lift.Matchers.displayed;

@RelatedTo("CSA-2090")
//TODO have this extend FindITCase but change the setUp()?
public class SimilarDocumentsITCase extends FindTestBase {
    private FindResultsPage results;
    private FindService findService;
    private SimilarDocumentsView similarDocuments;

    public SimilarDocumentsITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
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
            verifyThat(similarDocuments.getTitle(), equalToIgnoringCase("Similar results to document with title \"" + title + "\""));
            verifyThat(similarDocuments.getTotalResults(), greaterThan(0));
            verifyThat(similarDocuments.getResults(1), not(empty()));

            similarDocuments.backButton().click();
        }
    }

    @Test
    @KnownBug("CSA-3678")
    public void testTitle(){
        findService.search(new Query("Bill Murray").withFilter(new ParametricFilter("Source Connector","SimpsonsArchive")));

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
        findService.search(new Query("bart").withFilter(new IndexFilter("simpsonsarchive")));

        for (int i = 1; i <= 5; i++) {
            similarDocuments = findService.goToSimilarDocuments(i);
            WebElement seedLink  = similarDocuments.seedLink();
            String seedTitle = seedLink.getText();
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
            similarDocuments.backButton().click();
        }
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
        findService.search(new Query("Hammer").withFilter(IndexFilter.PUBLIC));

        for(int i = 1; i <= 5; i++){
            verifySimilarDocsNotEmpty(i);
        }
    }

    @Test
    @KnownBug("CCUK-3542")
    public void testPromotedDocuments(){
        Window findWindow = getWindow();

        IsoHsodApplication searchApp = new IsoHsodApplication();
        Window searchWindow = launchInNewWindow(searchApp);
        searchWindow.activate();
        Waits.loadOrFadeWait();

        String trigger = "Riga";
        HsodPromotionService promotionService = searchApp.promotionService();
        try {
            promotionService.setUpPromotion(new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, trigger), "Have Mercy", 3);

            findWindow.activate();
            results = findService.search(trigger);

            for (int i = 1; i <= results.promotions().size(); i++) {
                verifySimilarDocsNotEmpty(i);
            }
        } finally {
            searchWindow.activate();
            promotionService.deleteAll();
        }
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

    private void verifySimilarDocsNotEmpty(int i) {
        similarDocuments = findService.goToSimilarDocuments(i);
        verifyThat(similarDocuments.resultsContainer().getText(), not(isEmptyOrNullString()));
        similarDocuments.backButton().click();
    }

    @Test
    public void testInfiniteScroll(){
        results = findService.search(new Query("Heaven is Earth").withFilter(IndexFilter.ALL));

        similarDocuments = findService.goToSimilarDocuments(1);
        assumeThat(similarDocuments.getResults().size(), is(30));

        for(int i = 30; i <= 150; i += 30) {
            verifyThat(similarDocuments.getVisibleResultsCount(), is(i));
            DocumentViewer documentViewer = similarDocuments.getResult(i).openDocumentPreview();
            verifyThat(documentViewer.getTotalDocumentsNumber(), is(i));
            documentViewer.close();
            results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);
        }
    }

    @Test
    public void testSortByDate() throws ParseException {
        findService.search(new Query("Fade").withFilter(new IndexFilter("news_eng")));
        similarDocuments = findService.goToSimilarDocuments(1);

        similarDocuments.sortByDate();

        List<FindResult> searchResults = similarDocuments.getResults();

        Date previousDate = null;
        for(int i = 1; i <= 10; i++){
            DocumentViewer documentViewer = searchResults.get(i).openDocumentPreview();
            String date = documentViewer.getField("Date");
            if(date == null) {
                date = documentViewer.getField("Date Created");
            }

            Date currentDate = SimilarDocumentsView.DATE_FORMAT.parse(date);

            if(previousDate != null){
                verifyThat(currentDate, lessThanOrEqualTo(previousDate));
            }

            previousDate = currentDate;

            documentViewer.close();
        }
    }

    @Test
    public void testDocumentPreview(){
        findService.search(new Query("stars").withFilter(new IndexFilter(Index.DEFAULT)));
        similarDocuments = findService.goToSimilarDocuments(1);

        SharedPreviewTests.testDocumentPreviews(getMainSession(), similarDocuments.getResults(5), Index.DEFAULT);
    }
}
