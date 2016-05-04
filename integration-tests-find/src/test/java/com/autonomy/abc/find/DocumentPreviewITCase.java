package com.autonomy.abc.find;

import com.autonomy.abc.base.FindTestBase;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.*;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.ParametricFilter;
import com.autonomy.abc.selenium.query.Query;
import com.hp.autonomy.frontend.selenium.application.ApplicationType;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBug;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assume.assumeThat;

public class DocumentPreviewITCase extends FindTestBase{
    private FindPage findPage;
    private FindTopNavBar navBar;
    private FindResultsPage results;
    private FindService findService;

    public DocumentPreviewITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        findPage = getElementFactory().getFindPage();
        navBar = getElementFactory().getTopNavBar();
        results = findPage.getResultsPage();
        findService = getApplication().findService();
    }



    @Test
    @KnownBug("CCUK-3641")
    public void testAuthor(){
        assumeThat(getConfig().getType(),is(ApplicationType.HOSTED));
        String author = "FIFA.COM";

        findService.search(new Query("football")
                .withFilter(new IndexFilter("Fifa"))
                .withFilter(new ParametricFilter("Author", author)));

        assertThat(results.resultsDiv(), not(containsText(Errors.Find.GENERAL)));

        List<FindResult> searchResults = results.getResults();

        for(int i = 0; i < 6; i++){
            DocumentViewer documentViewer = searchResults.get(i).openDocumentPreview();
            verifyThat(documentViewer.getAuthor(), equalToIgnoringCase(author));
            documentViewer.close();
        }
    }
}
