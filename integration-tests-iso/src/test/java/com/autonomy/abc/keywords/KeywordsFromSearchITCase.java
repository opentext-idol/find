package com.autonomy.abc.keywords;

import com.autonomy.abc.base.HybridIsoTestBase;
import com.autonomy.abc.fixtures.KeywordTearDownStrategy;
import com.autonomy.abc.selenium.element.TriggerForm;
import com.autonomy.abc.selenium.error.Errors;
import com.autonomy.abc.selenium.keywords.CreateNewKeywordsPage;
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
import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import com.hp.autonomy.frontend.selenium.framework.categories.SlowTest;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.CommonMatchers.containsItems;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.urlContains;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.StringMatchers.containsString;
import static com.thoughtworks.selenium.SeleneseTestBase.fail;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.openqa.selenium.lift.Matchers.displayed;

public class KeywordsFromSearchITCase extends HybridIsoTestBase {
    private CreateNewKeywordsPage createKeywordsPage;
    private SearchPage searchPage;
    private KeywordsPage keywordsPage;
    private KeywordService keywordService;
    private SearchService searchService;

    public KeywordsFromSearchITCase(TestConfig config) {
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

    //Blacklisted terms can be created on the searchpage. This link has often broken
    @Test
    @ResolvedBug("CSA-1521")
    public void testCreateBlacklistedTermFromSearchPage() throws InterruptedException {
        search("noir", Language.FRENCH);

        assertThat("Results for search noir shown", searchPage.visibleDocumentsCount(), not(0));
        assertThat("Add to blacklist link displayed", searchPage.blacklistLink(), displayed());
        assertThat("Create synonyms link displayed", searchPage.createSynonymsLink(), displayed());

        searchPage.blacklistLink().click();
        Waits.loadOrFadeWait();
        assertThat("link directed to blacklist wizard", getWindow(), urlContains("keywords/create"));
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        assertThat("link directed to blacklist wizard", createKeywordsPage.getText(), containsString("Select terms to blacklist"));

        TriggerForm triggerForm = createKeywordsPage.getTriggerForm();

        assertThat(triggerForm.getNumberOfTriggers(), is(1));
        assertThat("keywords list includes term 'noir'", triggerForm.getTriggersAsStrings().contains("noir"));

        triggerForm.addTrigger("noir");
        assertThat(triggerForm.getNumberOfTriggers(), is(1));
        assertThat("keywords list includes term 'noir'", triggerForm.getTriggersAsStrings().contains("noir"));

        createKeywordsPage.enabledFinishWizardButton().click();
        waitForKeywordCreation();
        keywordsPage = getElementFactory().getKeywordsPage();

        assertThat("Blacklisted term added", keywordsPage.getBlacklistedTerms().contains("noir"));
    }

    //There is a link to create synonym group from the search page that prepopulates the create synonyms wizard with the current search term. Often breaks.
    @Test
    public void testCreateSynonymGroupFromSearchPage() throws InterruptedException {
        search("rouge", Language.FRENCH);

        assertThat("Results for search rouge shown", searchPage.visibleDocumentsCount(), not(0));
        assertThat("Add to blacklist link displayed", searchPage.blacklistLink(), displayed());
        assertThat("Create synonyms link displayed", searchPage.createSynonymsLink(), displayed());

        searchPage.createSynonymsLink().click();
        Waits.loadOrFadeWait();
        assertThat("link directed to synonym group wizard", getWindow(), urlContains("keywords/create"));
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        assertThat("link directed to synonym group wizard", createKeywordsPage.getText(), containsString("Select synonyms"));

        TriggerForm triggerForm = createKeywordsPage.getTriggerForm();

        assertThat(triggerForm.getNumberOfTriggers(), is(1));
        assertThat("keywords list includes term 'rouge'", triggerForm.getTriggersAsStrings(), hasItem("rouge"));
        assertThat("Finish button disabled until further synonyms added", ElementUtil.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

        triggerForm.addTrigger("rouge");
        assertThat(triggerForm.getNumberOfTriggers(), is(1));
        assertThat("keywords list includes term 'rouge'", triggerForm.getTriggersAsStrings(), hasItem("rouge"));
        assertThat("Finish button disabled until further synonyms added", ElementUtil.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

        triggerForm.addTrigger("red");
        assertThat(triggerForm.getNumberOfTriggers(), is(2));
        assertThat("keywords list includes term 'rouge'", triggerForm.getTriggersAsStrings(),hasItem("rouge"));
        assertThat("keywords list includes term 'red'", triggerForm.getTriggersAsStrings(), hasItem("red"));
        assertThat("Finish button enabled", !ElementUtil.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

        createKeywordsPage.enabledFinishWizardButton().click();
        searchPage.waitForSynonymsLoadingIndicatorToDisappear();

        keywordsPage = keywordService.goToKeywords();
        keywordsPage.filterView(KeywordFilter.SYNONYMS);
        new WebDriverWait(getDriver(), 20).until(ExpectedConditions.visibilityOf(keywordsPage.selectLanguageButton()));
        //assertEquals("Blacklist has been created in the wrong language", "French", keywordsPage.getSelectedLanguage());

        keywordsPage.selectLanguage(Language.FRENCH);

        assertThat("Synonym group added", keywordsPage.getSynonymGroupSynonyms("rouge"),hasItem("red"));
        assertThat("Synonym group added", keywordsPage.getSynonymGroupSynonyms("red"), hasItem("rouge"));
        assertEquals(1, keywordsPage.countSynonymLists());
        assertEquals(2, keywordsPage.countKeywords());
    }

    //There is a link to create synonym group from the search page that pre-populates the create synonyms wizard with the current multi term search. Often breaks.
    @Test
    public void testCreateSynonymGroupFromMultiTermSearchOnSearchPage() throws InterruptedException {
        search("lodge dodge podge", Language.ENGLISH);

        assertThat("results for search shown", searchPage.visibleDocumentsCount(), not(0));
        assertThat("add to blacklist link displayed", searchPage.blacklistLink(), displayed());
        assertThat("create synonyms link displayed", searchPage.createSynonymsLink(), displayed());

        searchPage.createSynonymsLink().click();
        Waits.loadOrFadeWait();
        assertThat("link directed to synonym group wizard", getWindow(), urlContains("keywords/create"));
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        assertThat("link directed to synonym group wizard", createKeywordsPage.getText(), containsString("Select synonyms"));

        TriggerForm triggerForm = createKeywordsPage.getTriggerForm();

        assertThat(triggerForm.getNumberOfTriggers(), is(3));
        assertThat("prospective blacklisted terms added", triggerForm.getTriggersAsStrings(), hasItems("lodge", "dodge", "podge"));
        assertThat("Finish button enabled", !ElementUtil.isAttributePresent(createKeywordsPage.enabledFinishWizardButton(), "disabled"));

        createKeywordsPage.enabledFinishWizardButton().click();
        getElementFactory().getSearchPage();

        keywordsPage = keywordService.goToKeywords();
        keywordsPage.filterView(KeywordFilter.SYNONYMS);
        keywordsPage.selectLanguage(Language.ENGLISH);

        assertThat("Synonym group complete", keywordsPage.getSynonymGroupSynonyms("lodge"), hasItems("lodge", "dodge", "podge"));
        assertThat("Synonym group complete", keywordsPage.getSynonymGroupSynonyms("podge"), hasItems("lodge", "dodge", "podge"));
        assertThat("Synonym group complete", keywordsPage.getSynonymGroupSynonyms("dodge"), hasItems("lodge", "dodge", "podge"));

        assertEquals(1, keywordsPage.countSynonymLists());
        assertEquals(3, keywordsPage.countKeywords());
    }


    @Test
    @ResolvedBug("CCUK-2703")
    @RelatedTo({"CSA-1724", "CSA-1893"})
    public void testNoBlacklistLinkForBlacklistedSearch() throws InterruptedException {
        Serializable blacklistMessage = Errors.Search.BLACKLIST;
        if (isHosted()) {
            blacklistMessage = Errors.Search.NO_RESULTS;
        }

        search("wizard", Language.ARABIC);

        searchPage.blacklistLink().click();
        try {
            createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
            createKeywordsPage.enabledFinishWizardButton().click();
        } catch (final NoSuchElementException e) {
            fail("blacklist link on search page has not navigated to the wizard");
        }

        keywordsPage.selectLanguageButton();	//Wait for select Language button

        if(isOnPrem()){
            assertThat("blacklist has been created in the correct language", keywordsPage.getSelectedLanguage(), is(Language.ARABIC));
        }

        Waits.loadOrFadeWait();
        new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
        keywordsPage.filterView(KeywordFilter.BLACKLIST);

        keywordsPage.selectLanguage(Language.ARABIC);

        assertThat("blacklisted term created successfully", keywordsPage.getBlacklistedTerms(), hasItem("wizard"));

        search("wizard", Language.ARABIC);

        assertThat(searchPage, containsText(blacklistMessage));
        assertThat("'You searched for:' section correct", searchPage.youSearchedFor(), hasItem("wizard"));
        // TODO: re-enable query analysis
//        verifyThat("blacklist term appears in query analysis", searchPage.getBlacklistedTerms(), hasItem("wizard"));
        assertThat("link to blacklist or create synonyms not present", searchPage,
                not(containsText("You can create synonyms or blacklist these search terms")));

        if (isOnPrem()) {
            searchPage.selectLanguage(Language.ENGLISH);
            searchPage.waitForSynonymsLoadingIndicatorToDisappear();

            assertThat("term is not blacklisted in English", searchPage, not(containsText(blacklistMessage)));
        }
    }

    @Test
    public void testAddingSynonymGroupFromSearchPageOnlyAddsWords(){
        String phrase = "the quick brown fox jumps over the lazy dog";
        search(phrase, Language.ENGLISH);
        searchPage.createSynonymsLink().click();

        assertThat(getWindow(), urlContains("keywords/create"));

        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();

        ArrayList<String> wordsInPhrase = new ArrayList<>(Arrays.asList(phrase.split(" ")));
        wordsInPhrase.removeAll(Collections.singleton("the"));

        List<String> prospectiveKeywords = createKeywordsPage.getTriggerForm().getTriggersAsStrings();

        assertThat(prospectiveKeywords, containsItems(wordsInPhrase));
        assertThat(prospectiveKeywords, not(hasItem("the")));
        assertThat(wordsInPhrase.size(), is(prospectiveKeywords.size()));
    }

    @Test
    @ResolvedBug("CSA-1694")
    public void testCancellingKeywordsWizardDoesntBreakSearch(){
        search("apu", Language.ENGLISH);
        searchPage.createSynonymsLink().click();

        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        createKeywordsPage.cancelWizardButton().click();

        try {
            searchPage.waitForSearchLoadIndicatorToDisappear();
        } catch (TimeoutException e) {
            fail("Search does not find results after cancelling synonym wizard");
        }
    }

    //isn't working if there are already keywords there to delete: see the setup
    @Test
    @ResolvedBug({"CSA-1719", "CSA-1792", "CSA-2064"})
    public void testBlacklistTermsBehaveAsExpected() throws InterruptedException {

        String blacklistOne = "cheese";
        String blacklistTwo = "mouse";

        assertThat("Keywords Load",!keywordsPage.loadingIndicator().isDisplayed());

        keywordsPage = keywordService.addBlacklistTerms(Language.ENGLISH, blacklistOne);
        assertThat(keywordsPage.getBlacklistedTerms(), hasItem(blacklistOne));

        search(blacklistOne, Language.ENGLISH);
        checkNoResults();
        getWindow().refresh();
        keywordsPage = keywordService.goToKeywords();

        assertThat(keywordsPage.getBlacklistedTerms(), hasItem(blacklistOne));

        keywordsPage = keywordService.addBlacklistTerms(blacklistTwo);
        assertThat(keywordsPage.getBlacklistedTerms(), hasItem(blacklistOne));
        assertThat(keywordsPage.getBlacklistedTerms(), hasItem(blacklistTwo));

        search(blacklistTwo, Language.ENGLISH);
        checkNoResults();

        search(blacklistOne, Language.ENGLISH);
        checkNoResults();
    }

    private void checkNoResults() {
        verifyThat(searchPage.getText(), containsString(Errors.Search.BLACKLIST));
    }

    @Test
    @Category(SlowTest.class)
    @ResolvedBug({"CCUK-3471", "CSA-1808"})
    public void testCreateLargeSynonymGroup() {
        List<String> synonyms = new ArrayList<>();
        for (int i=0; i<10; i++) {
            synonyms.add("term" + i);
        }

        searchPage = keywordService.addSynonymGroup(synonyms);
        assertThat(searchPage, not(containsText(Errors.Search.BACKEND)));
    }

    @Test
    @Category(SlowTest.class)
    @ActiveBug("HOD-2135")
    public void testCreateLargeDuplicateSynonymGroups() {
        List<String> synonyms;
        for (int outer=0; outer<10; outer++) {
            synonyms = new ArrayList<>();
            synonyms.add("everywhere");
            for (int inner=0; inner<10; inner++) {
                synonyms.add("term" + outer + "" + inner);
            }
            keywordService.addSynonymGroup(synonyms);
        }
        search("everywhere", Language.ENGLISH);
        assertThat(searchPage, not(containsText(Errors.Search.BACKEND)));
    }

    @Test
    @Category(SlowTest.class)
    @ActiveBug("HOD-2135")
    public void testCreateLargeDistinctSynonymGroups() {
        List<String> synonyms;
        for (int outer=0; outer<10; outer++) {
            synonyms = new ArrayList<>();
            for (int inner=0; inner<10; inner++) {
                synonyms.add("term" + outer + "" + inner);
            }
            keywordService.addSynonymGroup(synonyms);
        }
        synonyms = new ArrayList<>();
        for (int outer=0; outer<10; outer++) {
            synonyms.add("term" + outer + "0");
        }
        search(StringUtils.join(synonyms, " "), Language.ENGLISH);
        assertThat(searchPage, not(containsText(Errors.Search.BACKEND)));
    }

    private void search(String searchTerm, Language language) {
        Query query = new Query(searchTerm).withFilter(new LanguageFilter(language));
        if (isHosted()) {
            query = query.withFilter(new IndexFilter("news_eng"));
        }
        searchPage = searchService.search(query);
    }

    private void waitForKeywordCreation() {
        FluentWait<WebDriver> wait = new WebDriverWait(getDriver(), 30).withMessage("waiting for keywords to be created");
        wait.until(GritterNotice.notificationAppears());
        wait.until(GritterNotice.notificationsDisappear());
    }
}
