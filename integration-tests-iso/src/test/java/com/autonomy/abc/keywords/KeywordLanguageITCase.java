package com.autonomy.abc.keywords;

import com.autonomy.abc.base.IdolIsoTestBase;
import com.autonomy.abc.fixtures.KeywordTearDownStrategy;
import com.autonomy.abc.selenium.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.keywords.KeywordsPage;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.SearchPage;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.urlContains;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static org.hamcrest.Matchers.*;

public class KeywordLanguageITCase extends IdolIsoTestBase {
    private KeywordService keywordService;

    private KeywordsPage keywordsPage;
    private SearchPage searchPage;

    public KeywordLanguageITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        keywordService = getApplication().keywordService();
    }

    @After
    public void tearDown() {
        new KeywordTearDownStrategy().tearDown(this);
    }

    @Test
    public void testOnlyLanguagesWithDocumentsAvailableOnSearchPage() {
        keywordService.addBlacklistTerms(Language.AZERI, "Baku");

        final SearchPage searchPage = getApplication().searchService().search("Baku");
        assertThat(searchPage.getLanguageList(), not(hasItem("Azeri")));
    }

    @Test
    public void testKeywordsLanguage() {
        keywordService.addBlacklistTerms(Language.GEORGIAN, "Atlanta");
        keywordService.addBlacklistTerms(Language.ALBANIAN, "Tirana");
        keywordService.addSynonymGroup(Language.CHINESE, "China Chine Xina");

        keywordsPage = keywordService.goToKeywords();

        keywordsPage.filterView(KeywordFilter.ALL);
        keywordsPage.selectLanguage(Language.GEORGIAN);
        assertThat(keywordsPage.getBlacklistedTerms().size(), is(1));
        assertThat(keywordsPage.countSynonymLists(), is(0));

        keywordsPage.selectLanguage(Language.ALBANIAN);
        assertThat(keywordsPage.getBlacklistedTerms().size(), is(1));
        assertThat(keywordsPage.countSynonymLists(), is(0));

        keywordsPage.selectLanguage(Language.CHINESE);
        assertThat(keywordsPage.getBlacklistedTerms().size(), is(0));
        assertThat(keywordsPage.countSynonymLists(), is(1));
        assertThat(keywordsPage.countKeywords(), is(3));
    }


    @Test
    public void testLanguageOfSearchPageKeywords() throws InterruptedException {
        keywordService.addSynonymGroup(Language.FRENCH, "road rue strasse", "French");
        search("Korea", Language.CHINESE);

        searchPage.createSynonymsLink().click();
        Waits.loadOrFadeWait();
        assertThat(getWindow(), urlContains("keywords/create"));
        final CreateNewKeywordsPage createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();

        createKeywordsPage.getTriggerForm().addTrigger("韩国");
        new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(createKeywordsPage.enabledFinishWizardButton())).click();
        searchPage = getElementFactory().getSearchPage();

        keywordsPage = keywordService.goToKeywords();
        keywordsPage.filterView(KeywordFilter.ALL);

        keywordsPage.selectLanguage(Language.FRENCH);
        verifyThat("synonym not assigned to wrong language", keywordsPage, not(containsText("韩国")));

        keywordsPage.selectLanguage(Language.CHINESE);
        verifyThat(keywordsPage.countSynonymLists(), is(1));
        verifyThat("synonym assigned to correct language", keywordsPage, containsText("韩国"));
    }

    private void search(final String searchTerm, final Language language) {
        final Query query = new Query(searchTerm).withFilter(new LanguageFilter(language));
        searchPage = getApplication().searchService().search(query);
    }
}
