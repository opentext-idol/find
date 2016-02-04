package com.autonomy.abc.search;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.framework.categories.CoreFeature;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

@Category(CoreFeature.class)
public class SearchCoreITCase extends ABCTestBase {
    private SearchPage searchPage;

    public SearchCoreITCase(TestConfig config) {
        super(config);
    }

    @Test
    @KnownBug("CSA-2058")
    public void testSearchResultsNotEmpty() {
        searchPage = getApplication().searchService().search("luke");
        for (String title : searchPage.getSearchResultTitles(SearchPage.RESULTS_PER_PAGE)) {
            assertThat(title, not(isEmptyOrNullString()));
        }
    }
}
