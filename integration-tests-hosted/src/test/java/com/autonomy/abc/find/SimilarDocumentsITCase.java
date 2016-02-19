package com.autonomy.abc.find;

import com.autonomy.abc.config.FindTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.find.FindSearchResult;
import com.autonomy.abc.selenium.find.SimilarDocumentsView;
import org.junit.Before;
import org.junit.Test;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.Matchers.*;

public class SimilarDocumentsITCase extends FindTestBase {
    private FindPage findPage;
    private FindResultsPage results;

    public SimilarDocumentsITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        findPage = getElementFactory().getFindPage();
        results = findPage.getResultsPage();
    }

    @Test
    public void testSimilarDocumentsShowUp() throws InterruptedException {
        findPage.search("Doe");

        for (int i = 1; i <= 5; i++) {
            FindSearchResult searchResult = results.getResult(i);
            String title = searchResult.getTitleString();
            searchResult.similarDocuments().click();

            results.waitForSearchLoadIndicatorToDisappear(FindResultsPage.Container.MIDDLE);

            SimilarDocumentsView similarDocuments = getElementFactory().getSimilarDocumentsView();

            verifyThat(getDriver().getCurrentUrl(), containsString("suggest"));
            verifyThat(similarDocuments.getTitle(), equalToIgnoringCase("Similar results to document with title \"" + title + "\""));
            verifyThat(similarDocuments.getTotalResults(), greaterThan(0));
            verifyThat(similarDocuments.getResults(1), not(empty()));

            similarDocuments.backButton().click();
        }
    }
}
