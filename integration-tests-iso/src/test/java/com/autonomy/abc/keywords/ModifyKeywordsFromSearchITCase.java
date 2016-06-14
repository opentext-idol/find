package com.autonomy.abc.keywords;

import com.autonomy.abc.base.HybridIsoTestBase;
import com.autonomy.abc.fixtures.KeywordTearDownStrategy;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.keywords.KeywordsPage;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.query.IndexFilter;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.CommonMatchers.containsItems;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

@Ignore("Cannot modify keywords from search page - is this functionality coming back?")
@RelatedTo("ISO-31")
public class ModifyKeywordsFromSearchITCase extends HybridIsoTestBase {
    private KeywordService keywordService;
    private SearchService searchService;

    private KeywordsPage keywordsPage;
    private SearchPage searchPage;

    public ModifyKeywordsFromSearchITCase(final TestConfig config) {
        super(config);
    }


    @Before
    public void setUp() {
        keywordService = getApplication().keywordService();
        searchService = getApplication().searchService();

        keywordsPage = keywordService.deleteAll(KeywordFilter.ALL);
    }

    @After
    public void tearDown() {
        new KeywordTearDownStrategy().tearDown(this);
    }

    @Test
    public void testSearchPageKeywords() throws InterruptedException {
        List<String> synonymListBears = Arrays.asList("grizzly", "brownbear", "bigbear");
        searchPage = keywordService.addSynonymGroup(synonymListBears);

        for (final String synonym : synonymListBears) {
            assertThat(synonym + " included in title", searchPage.getPageTitle(), containsString(synonym));
            assertThat(synonym + " included in 'You searched for' section", searchPage.youSearchedFor(),hasItem(synonym));
            verifyThat(synonym + " synonym group complete in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym), containsItems(synonymListBears));
            verifyThat(searchPage.countSynonymLists(), is(1));
            verifyThat(searchPage.countKeywords(), is(synonymListBears.size()));
        }

        searchPage.addSynonymToGroup("kodiak", searchPage.synonymGroupContaining("grizzly"));
        for (final String synonym : synonymListBears) {
            assertThat(synonym + " included in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym), containsItems(synonymListBears));
            assertThat("kodiak included in synonym group " + synonym, searchPage.getSynonymGroupSynonyms(synonym), hasItem("kodiak"));
            assertEquals(1, searchPage.countSynonymLists());
            assertEquals(4, searchPage.countKeywords());
        }

        searchPage.deleteSynonym("bigbear");
        Waits.loadOrFadeWait();
        synonymListBears = Arrays.asList("grizzly", "brownbear");
        for (final String synonym : synonymListBears) {
            assertThat(synonym + " included in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym),containsItems(synonymListBears));
            assertThat("bigbear deleted from group " + synonym, searchPage.getSynonymGroupSynonyms(synonym),not(hasItem("bigbear")));
            assertThat("kodiak included in synonym group " + synonym, searchPage.getSynonymGroupSynonyms(synonym),hasItem("kodiak"));
            assertEquals(1, searchPage.countSynonymLists());
            assertEquals(3, searchPage.countKeywords());
        }

        keywordsPage = keywordService.goToKeywords();
        keywordsPage.selectLanguage(Language.ENGLISH);
        keywordsPage.filterView(KeywordFilter.SYNONYMS);

        assertEquals(1, keywordsPage.countSynonymLists());
        assertEquals(3, keywordsPage.countKeywords());

        synonymListBears = Arrays.asList("grizzly", "brownbear", "kodiak");
        for (final String synonym : synonymListBears) {
            assertThat(synonym + " group complete", keywordsPage.getSynonymGroupSynonyms(synonym), containsItems(synonymListBears));
            assertEquals(1, keywordsPage.countSynonymLists());
            assertEquals(3, keywordsPage.countKeywords());
            assertThat("bigbear not from group " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym), not(hasItem("bigbear")));
        }
    }

    @Test
    public void testSynonymGroupMembersSearchWholeGroup() throws InterruptedException {
        final List<String> synonymListCars = Arrays.asList("car", "auto", "motor");
        searchPage = keywordService.addSynonymGroup(Language.SWAHILI, synonymListCars);

        for (final String synonym : synonymListCars) {
            search(synonym, Language.SWAHILI);

            assertEquals(1, searchPage.countSynonymLists());
            assertEquals(3, searchPage.countKeywords());
            assertThat("Synonym group contains all its members", searchPage.getSynonymGroupSynonyms(synonym),containsItems(synonymListCars));
        }
    }


    @Test
    public void testAddTwoSynonymsToSynonymGroupFromSearchPage() throws InterruptedException {
        final List<String> houses = new ArrayList<>(Arrays.asList("house", "home", "dwelling", "abode"));

        keywordService.addSynonymGroup(houses);
        search("house", Language.ENGLISH);
        searchPage.waitForSynonymsLoadingIndicatorToDisappear();

        verifyThat(searchPage.countSynonymLists(), is(1));
        verifyThat(searchPage.countKeywords(), is(4));
        verifyThat("Synonym group contains all its members", searchPage.getSynonymGroupSynonyms("house"), containsItems(houses));

        searchPage.addSynonymToGroup("lodging", searchPage.synonymGroupContaining("house"));
        houses.add("lodging");
        assertThat("New synonym has been added to the group", searchPage.getSynonymGroupSynonyms("house"), containsItems(houses));

        searchPage.addSynonymToGroup("residence", searchPage.synonymGroupContaining("house"));
        houses.add("residence");
        assertThat("New synonym has been added to the group", searchPage.getSynonymGroupSynonyms("house"), containsItems(houses));

        keywordsPage = keywordService.goToKeywords();

        keywordsPage.filterView(KeywordFilter.ALL);
        assertThat("New synonym has been added to the group", keywordsPage.getSynonymGroupSynonyms("house"), containsItems(houses));

        keywordService.deleteAll(KeywordFilter.ALL);
        assertThat(keywordsPage.allKeywordGroups(), hasSize(0));
    }

    @Test
    public void testRemoveTwoSynonymsFromSynonymGroupFromSearchPage() throws InterruptedException {
        keywordService.addSynonymGroup(Language.ENGLISH, "house home dwelling abode residence");
        search("house", Language.ENGLISH);

        verifyThat(searchPage.countSynonymLists(), is(1));
        verifyThat(searchPage.countKeywords(), is(5));
        verifyThat(searchPage.getSynonymGroupSynonyms("house"), hasItems("home", "dwelling", "abode", "residence"));

        searchPage.deleteSynonym("residence");
        Waits.loadOrFadeWait();
        verifyThat("Synonym has been deleted", searchPage.getSynonymGroupSynonyms("house"), not(hasItem("residence")));
        verifyThat("Synonym has not been deleted", searchPage.getSynonymGroupSynonyms("house"), hasItem("abode"));
        verifyThat("1 synonym deleted", searchPage.getSynonymGroupSynonyms("house"), hasItems("home", "dwelling", "abode"));

        searchPage.deleteSynonym("abode");
        Waits.loadOrFadeWait();
        verifyThat("Synonym has been deleted", searchPage.getSynonymGroupSynonyms("house"), not(hasItem("abode")));
        verifyThat("2 synonyms deleted", searchPage.getSynonymGroupSynonyms("house"), hasItems("home", "dwelling"));

        searchPage.deleteSynonym("dwelling");
        Waits.loadOrFadeWait();
        verifyThat("Synonym has been deleted", searchPage.getSynonymGroupSynonyms("house"), not(hasItem("dwelling")));
        verifyThat("Synonym has been deleted", searchPage.getSynonymGroupSynonyms("house"), not(hasItem("abode")));
        verifyThat("Synonym has been deleted", searchPage.getSynonymGroupSynonyms("house"), not(hasItem("residence")));
        verifyThat("3 synonyms deleted", searchPage.getSynonymGroupSynonyms("house"), hasItem("home"));

        keywordsPage = keywordService.goToKeywords();
        keywordsPage.filterView(KeywordFilter.ALL);
        assertThat("Synonyms have been removed from the group", keywordsPage.getSynonymGroupSynonyms("house"), hasItems("home", "house"));

        keywordService.deleteAll(KeywordFilter.ALL);
        keywordsPage.filterView(KeywordFilter.SYNONYMS);
        verifyThat(keywordsPage.countSynonymLists(), is(0));
    }

    private void search(final String searchTerm, final Language language) {
        Query query = new Query(searchTerm).withFilter(new LanguageFilter(language));
        if (isHosted()) {
            query = query.withFilter(new IndexFilter("news_eng"));
        }
        searchPage = searchService.search(query);
    }
}
