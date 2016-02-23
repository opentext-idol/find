package com.autonomy.abc.find;

import com.autonomy.abc.config.FindTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.control.Window;
import com.autonomy.abc.selenium.find.FindResultsPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.SimilarDocumentsView;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.SearchQuery;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static org.hamcrest.Matchers.*;

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
        findService.search(new SearchQuery("Doe"));

        for (int i = 1; i <= 5; i++) {
            String title = results.getResult(i).getTitleString();
            similarDocuments = findService.goToSimilarDocuments(i);

            verifyThat(getDriver().getCurrentUrl(), containsString("suggest"));
            verifyThat(similarDocuments.getTitle(), equalToIgnoringCase("Similar results to document with title \"" + title + "\""));
            verifyThat(similarDocuments.getTotalResults(), greaterThan(0));
            verifyThat(similarDocuments.getResults(1), not(empty()));

            similarDocuments.backButton().click();
        }
    }

    @Test
    public void testPreviewSeed() throws InterruptedException {
        findService.search(new SearchQuery("bart").withFilter(new IndexFilter("simpsonsarchive")));

        for (int i = 1; i <= 5; i++) {
            similarDocuments = findService.goToSimilarDocuments(i);
            WebElement seedLink  = similarDocuments.seedLink();
            String seedTitle = seedLink.getText();
            Window firstWindow = getWindow();

            Window secondWindow = openSeed(seedLink);

            verifyThat("opened in new tab", secondWindow, not(firstWindow));
            verifyThat(getDriver().getTitle(), containsString(seedTitle));
            verifyThat("not using viewserver", getDriver().getCurrentUrl(), not(containsString("viewDocument")));

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
                .until(new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(WebDriver input) {
                        return getMainSession().countWindows() == windowCount + 1;
                    }
                });

        Window secondWindow = null;
        for (Window openWindow : getMainSession()) {
            if (!openWindow.equals(currentWindow)) {
                openWindow.activate();
                secondWindow = openWindow;
            }
        }
        return secondWindow;
    }
}
