package com.autonomy.abc.search;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.selenium.control.Frame;
import com.autonomy.abc.selenium.element.DocumentViewer;
import com.autonomy.abc.selenium.element.Pagination;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.SearchBase;
import com.autonomy.abc.selenium.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import java.util.Arrays;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.StringMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

public class SearchLanguageITCase extends ABCTestBase {
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

        assertThat(searchPage.promotionsBucketWebElements(), hasSize(4));

        final String url = getWindow().getUrl().replace("french", "arabic");
        getWindow().goTo(url);
        searchPage = getElementFactory().getSearchPage();
        Waits.loadOrFadeWait();
        assertThat(searchPage.promoteThisQueryButton(), displayed());
        assertThat(searchPage.promotionsBucketWebElements(), hasSize(0));
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
        assertThat("There should be one document in the bucket", searchPage.promotionsBucketList(), hasSize(1));
        searchPage.selectLanguage(Language.FRENCH);
        assertThat("The promotions bucket should close when the language is changed", searchPage.promotionsBucket(), not(displayed()));

        searchPage.openPromotionsBucket();
        assertThat("There should be no documents in the bucket after changing language", searchPage.promotionsBucketList(), hasSize(0));

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
        final List<String> englishConcepts = ElementUtil.webElementListToStringList(searchPage.relatedConcepts());
        searchPage.selectLanguage(Language.FRENCH);
        searchPage.expand(SearchBase.Facet.RELATED_CONCEPTS);
        searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
        final List<String> frenchConcepts = ElementUtil.webElementListToStringList(searchPage.relatedConcepts());

        assertThat("Concepts should be different in different languages", englishConcepts, not(containsInAnyOrder(frenchConcepts.toArray())));

        searchPage.selectLanguage(Language.ENGLISH);
        searchPage.expand(SearchBase.Facet.RELATED_CONCEPTS);
        searchPage.waitForRelatedConceptsLoadIndicatorToDisappear();
        final List<String> secondEnglishConcepts = ElementUtil.webElementListToStringList(searchPage.relatedConcepts());
        assertThat("Related concepts have changed on second search of same query text", englishConcepts, contains(secondEnglishConcepts.toArray()));
    }

    @Test
    @KnownBug("CCUK-2882")
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
        ElementUtil.scrollIntoViewAndClick(searchPage.promotionBucketElementByTitle(docTitle), getDriver());
        DocumentViewer viewer = DocumentViewer.make(getDriver());
        Frame frame = new Frame(getWindow(), viewer.frame());
        verifyThat("view frame displays", frame.getText(), containsString(docTitle));
        viewer.close();
    }
}
