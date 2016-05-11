package com.autonomy.abc.search;

import com.autonomy.abc.base.IdolIsoTestBase;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.SearchBase;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Frame;
import com.hp.autonomy.frontend.selenium.element.Pagination;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import java.util.Arrays;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

public class SearchLanguageITCase extends IdolIsoTestBase {
    private SearchService searchService;
    private SearchPage searchPage;

    public SearchLanguageITCase(TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        searchService = getApplication().searchService();
    }

    private void search(String term) {
        searchPage = searchService.search(term);
    }


    @Test
    public void testChangeLanguage() {
        String docTitle = searchPage.getSearchResult(1).getTitleString();
        search("1");

        List<Language> languages = Arrays.asList(Language.ENGLISH, Language.AFRIKAANS, Language.FRENCH, Language.ARABIC, Language.URDU, Language.HINDI, Language.CHINESE, Language.SWAHILI);
        for (final Language language : languages) {
            searchPage.selectLanguage(language);
            assertThat(searchPage.getSelectedLanguage(), is(language));

            searchPage.waitForSearchLoadIndicatorToDisappear();
            assertThat(searchPage.getSearchResult(1).getTitleString(), not(docTitle));

            docTitle = searchPage.getSearchResult(1).getTitleString();
        }
    }

    @Test
    public void testBucketEmptiesWhenLanguageChangedInURL() {
        search("arc");
        searchPage.selectLanguage(Language.FRENCH);
        searchPage.waitForSearchLoadIndicatorToDisappear();
        searchPage.openPromotionsBucket();
        searchPage.addDocsToBucket(4);

        assertThat(searchPage.getBucketTitles(), hasSize(4));

        final String url = getWindow().getUrl().replace("french", "arabic");
        getWindow().goTo(url);
        searchPage = getElementFactory().getSearchPage();
        Waits.loadOrFadeWait();
        assertThat(searchPage.promoteThisQueryButton(), displayed());
        assertThat(searchPage.getBucketTitles(), empty());
    }

    @Test
    public void testLanguageDisabledWhenBucketOpened() {
        //This test currently fails because language dropdown is not disabled when the promotions bucket is open
        searchPage.selectLanguage(Language.ENGLISH);
        search("al");
        Waits.loadOrFadeWait();
        assertThat("Languages should be enabled", !ElementUtil.isAttributePresent(searchPage.languageButton(), "disabled"));

        searchPage.openPromotionsBucket();
        searchPage.addDocToBucket(1);
        assertThat("There should be one document in the bucket", searchPage.getBucketTitles(), hasSize(1));
        searchPage.selectLanguage(Language.FRENCH);
        assertThat("The promotions bucket should close when the language is changed", searchPage.promotionsBucket(), not(displayed()));

        searchPage.openPromotionsBucket();
        assertThat("There should be no documents in the bucket after changing language", searchPage.getBucketTitles(), hasSize(0));

        searchPage.selectLanguage(Language.ENGLISH);
        assertThat("The promotions bucket should close when the language is changed", searchPage.promotionsBucket(), not(displayed()));
    }

    @Test
    public void testSearchAlternateScriptToSelectedLanguage() {
        List<Language> languages = Arrays.asList(Language.FRENCH, Language.ENGLISH, Language.ARABIC, Language.URDU, Language.HINDI, Language.CHINESE);
        for (final Language language : languages) {
            searchPage.selectLanguage(language);

            for (final String script : Arrays.asList("निर्वाण", "العربية", "עברית", "сценарий", "latin", "ελληνικά", "ქართული", "བོད་ཡིག")) {
                search(script);
                Waits.loadOrFadeWait();
                assertThat("Undesired error message for language: " + language + " with script: " + script, searchPage.findElement(By.cssSelector(".search-results-view")).getText(),not(containsString("error")));
            }
        }
    }

    @Test
    public void testRelatedConceptsDifferentInDifferentLanguages() {
        search("France");
        searchPage.expand(SearchBase.Facet.RELATED_CONCEPTS);
        searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
        final List<String> englishConcepts = searchPage.getRelatedConcepts();
        searchPage.selectLanguage(Language.FRENCH);
        searchPage.expand(SearchBase.Facet.RELATED_CONCEPTS);
        searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
        final List<String> frenchConcepts = searchPage.getRelatedConcepts();

        assertThat("Concepts should be different in different languages", englishConcepts, not(containsInAnyOrder(frenchConcepts.toArray())));

        searchPage.selectLanguage(Language.ENGLISH);
        searchPage.expand(SearchBase.Facet.RELATED_CONCEPTS);
        searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
        final List<String> secondEnglishConcepts = searchPage.getRelatedConcepts();
        assertThat("Related concepts have changed on second search of same query text", englishConcepts, contains(secondEnglishConcepts.toArray()));
    }

    @Test
    @ResolvedBug("CCUK-2882")
    public void testNonLatinUrlEncoding() {
        Query nonLatin = new Query("جيمس")
                .withFilter(new LanguageFilter(Language.ARABIC));
        searchPage = searchService.search(nonLatin);
        searchPage.openPromotionsBucket();

        for (int j = 1; j <=2; j++) {
            for (int i = 1; i <= 3; i++) {
                searchPage.addDocToBucket(i);
                final String docTitle = searchPage.getSearchResult(i).getTitleString();
                checkViewResult(docTitle);
            }
            searchPage.switchResultsPage(Pagination.NEXT);
        }
    }

    @RelatedTo("CCUK-3728")
    private void checkViewResult(String docTitle) {
        DriverUtil.scrollIntoViewAndClick(getDriver(), searchPage.promotionBucketElementByTitle(docTitle));
        DocumentViewer viewer = DocumentViewer.make(getDriver());
        Frame frame = new Frame(getWindow(), viewer.frame());
        verifyThat("view frame displays", frame.getText(), containsString(docTitle));
        viewer.close();
    }
}
