package com.autonomy.abc.keywords;

import com.autonomy.abc.base.HybridIsoTestBase;
import com.autonomy.abc.fixtures.KeywordTearDownStrategy;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.categories.CoreFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static org.hamcrest.Matchers.*;

@Category(CoreFeature.class)
public class KeywordsCoreITCase extends HybridIsoTestBase {
    private KeywordService keywordService;
    private SearchService searchService;

    public KeywordsCoreITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        keywordService = getApplication().keywordService();
        searchService = getApplication().searchService();
    }

    @After
    public void tearDown() {
        new KeywordTearDownStrategy().tearDown(this);
    }

    @Test
    public void testCreateBlacklist() {
        final String blacklist = "naughty";
        keywordService.addBlacklistTerms(blacklist);
        SearchPage searchPage = searchService.search(blacklist);
        assertThat(searchPage.visibleDocumentsCount(), is(0));
    }

    @Test
    public void testCreateSynonyms() {
        final String hasResults = "car";
        final String noResults = "zxpqw";
        keywordService.addSynonymGroup(hasResults, noResults);
        SearchPage searchPage = searchService.search(noResults);
        assertThat(searchPage.visibleDocumentsCount(), greaterThan(0));
    }

    @Test
    public void testDeleteKeyword() {
        final String keyword = "asdfghjkl";
        keywordService.addBlacklistTerms(keyword);
        keywordService.deleteKeyword(keyword);
        assertThat(getElementFactory().getKeywordsPage(), not(containsText(keyword)));
    }
}
