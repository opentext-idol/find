package com.autonomy.abc.find;

import com.autonomy.abc.base.HsodFindTestBase;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.preview.DetailedPreviewPage;
import com.autonomy.abc.selenium.find.preview.InlinePreview;
import com.autonomy.abc.selenium.find.results.FindResult;
import com.autonomy.abc.selenium.find.results.ResultsView;
import com.autonomy.abc.selenium.indexes.tree.IndexCategoryNode;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.ParametricFilter;
import com.autonomy.abc.selenium.query.Query;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;

public class HodFilterITCase extends HsodFindTestBase {
    private FindPage findPage;
    private FindService findService;

    public HodFilterITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp(){
        findPage = getElementFactory().getFindPage();
        findService = getApplication().findService();
    }

    @Test
    public void testPdfContentTypeValue(){
        checkContentTypeFilter("APPLICATION/PDF", "pdf");
    }

    @Test
    public void testHtmlContentTypeValue(){
        checkContentTypeFilter("TEXT/HTML", "html");
    }

    private void checkContentTypeFilter(final String filterType, final String extension) {
        final Query query = new Query("red star")
                .withFilter(new ParametricFilter("Content Type", filterType));
        ResultsView results = findService.search(query);
        for(final String type : results.getDisplayedDocumentsDocumentTypes()){
            assertThat(type, containsString(extension));
        }
    }

    @Test
    public void testFileTypes(){
        ResultsView results = findService.search("love ");

        for(final FileType f : FileType.values()) {
            findPage.filterBy(new ParametricFilter("Content Type",f.getSidebarString()));

            for(final FindResult result : results.getResults()){
                assertThat(result.icon().getAttribute("class"), containsString(f.getFileIconString()));
            }

            findPage.filterBy(ParametricFilter.clearFilters());
        }
    }

    @Test
    @ResolvedBug("CCUK-3641")
    @ActiveBug("FIND-487")
    public void testAuthor(){
        final String author = "FIFA.COM";

        ResultsView results = findService.search(new Query("football")
                .withFilter(new IndexFilter("fifa"))
                .withFilter(new ParametricFilter("Author", author)));

        assertThat(results.resultsDiv(), not(containsText(Errors.Find.GENERAL)));

        final List<FindResult> searchResults = results.getResults();

        for(int i = 0; i < 6; i++){
            final InlinePreview documentViewer = (InlinePreview) searchResults.get(i).openDocumentPreview();

            DetailedPreviewPage preview = documentViewer.openPreview();
            verifyThat(preview.getAuthor(), equalToIgnoringCase(author));

            preview.goBackToSearch();
            documentViewer.close();
        }
    }

    @Test
    @ResolvedBug({"CSA-1726", "CSA-1763"})
    public void testPublicIndexesVisibleNotSelectedByDefault(){
        findService.search("Marina and the Diamonds");
        final IndexCategoryNode publicIndexes = filters().indexesTree().publicIndexes();

        verifyThat("public indexes are visible", publicIndexes, not(emptyIterable()));
        verifyThat("public indexes are collapsed", publicIndexes.isCollapsed(), is(true));
        verifyThat(publicIndexes.getSelectedNames(), empty());
    }

    private FilterPanel filters() {
        return getElementFactory().getFilterPanel();
    }

    private enum FileType {
        HTML("TEXT/HTML","html"),
        PDF("APPLICATION/PDF","pdf"),
        PLAIN("TEXT/PLAIN","file");

        private final String sidebarString;
        private final String fileIconString;

        FileType(final String sidebarString, final String fileIconString){
            this.sidebarString = sidebarString;
            this.fileIconString = fileIconString;
        }

        public String getFileIconString() {
            return fileIconString;
        }

        public String getSidebarString() {
            return sidebarString;
        }
    }
}
