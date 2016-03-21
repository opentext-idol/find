package com.autonomy.abc.search;

import com.autonomy.abc.base.SOTestBase;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.KnownBug;
import com.hp.autonomy.frontend.selenium.framework.categories.CoreFeature;
import com.autonomy.abc.selenium.search.SearchPage;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

@Category(CoreFeature.class)
public class SearchCoreITCase extends SOTestBase {

    public SearchCoreITCase(TestConfig config) {
        super(config);
    }

    @Test
    @KnownBug("CSA-2058")
    public void testSearchResultsNotEmpty() {
        SearchPage searchPage = getApplication().searchService().search("luke");
        for (String title : searchPage.getSearchResultTitles(SearchPage.RESULTS_PER_PAGE)) {
            assertThat(title, not(isEmptyOrNullString()));
        }
    }
}
