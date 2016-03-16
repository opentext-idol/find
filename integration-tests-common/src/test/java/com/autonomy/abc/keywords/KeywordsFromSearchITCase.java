package com.autonomy.abc.keywords;

import com.autonomy.abc.config.ABCTearDown;
import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.framework.RelatedTo;
import com.autonomy.abc.framework.categories.SlowTest;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.element.GritterNotice;
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
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.SOPageUtil;
import com.autonomy.abc.selenium.util.Waits;
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

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.CommonMatchers.containsItems;
import static com.autonomy.abc.matchers.StringMatchers.containsString;
import static com.autonomy.abc.matchers.ControlMatchers.urlContains;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.thoughtworks.selenium.SeleneseTestBase.fail;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;
import static org.openqa.selenium.lift.Matchers.displayed;

public class KeywordsFromSearchITCase extends ABCTestBase {
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
        ABCTearDown.KEYWORDS.tearDown(this);
    }

    //Blacklisted terms can be created on the searchpage. This link has often broken
    @Test
    @KnownBug("CSA-1521")
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

    //There is a link to create synonym group from the search page that prepopulates the create synonyms wizard with the current multi term search. Often breaks.
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
    public void testSearchPageKeywords() throws InterruptedException {
        assumeThat("Cannot modify keywords from search page in Hosted", getConfig().getType(), not(ApplicationType.HOSTED));

        List<String> synonymListBears = Arrays.asList("grizzly", "brownbear", "bigbear");
        searchPage = keywordService.addSynonymGroup(synonymListBears);

        for (final String synonym : synonymListBears) {
            assertThat(synonym + " included in title", SOPageUtil.getPageTitle(getDriver()),containsString(synonym));
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
    @KnownBug("CCUK-2703")
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
    public void testSynonymGroupMembersSearchWholeGroup() throws InterruptedException {
        assumeThat("Cannot modify keywords from search page in Hosted", getConfig().getType(), not(ApplicationType.HOSTED));

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
        assumeThat("Cannot modify keywords from search page in Hosted", getConfig().getType(), not(ApplicationType.HOSTED));
        List<String> houses = new ArrayList<>(Arrays.asList("house", "home", "dwelling", "abode"));

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
        assumeThat("Cannot modify keywords from search page in Hosted", getConfig().getType(), not(ApplicationType.HOSTED));

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

    @Test
    public void testLanguageOfSearchPageKeywords() throws InterruptedException {
        assumeThat("Language not implemented in Hosted", getConfig().getType(), not(ApplicationType.HOSTED));

        keywordService.addSynonymGroup(Language.FRENCH, "road rue strasse", "French");
        search("Korea", Language.CHINESE);

        searchPage.createSynonymsLink().click();
        Waits.loadOrFadeWait();
        assertThat(getWindow(), urlContains("keywords/create"));
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();

        createKeywordsPage.getTriggerForm().addTrigger("한국");
        new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(createKeywordsPage.enabledFinishWizardButton())).click();
        searchPage = getElementFactory().getSearchPage();

        search("Korea", Language.CHINESE);
        verifyThat("synonyms appear on search page for correct language", searchPage.countSynonymLists(), is(1));

        searchPage.selectLanguage(Language.FRENCH);
        verifyThat("synonyms do not appear on search page for wrong language", searchPage.countSynonymLists(), is(0));

        keywordsPage = keywordService.goToKeywords();
        keywordsPage.filterView(KeywordFilter.ALL);

        keywordsPage.selectLanguage(Language.FRENCH);
        verifyThat("synonym not assigned to wrong language", keywordsPage, not(containsText("한국")));

        keywordsPage.selectLanguage(Language.CHINESE);
        verifyThat(keywordsPage.countSynonymLists(), is(1));
        verifyThat("synonym assigned to correct language", keywordsPage, containsText("한국"));
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
    @KnownBug("CSA-1694")
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

    @Test
    @KnownBug({"CSA-1719", "CSA-1792", "CSA-2064"})
    public void testBlacklistTermsBehaveAsExpected() throws InterruptedException {
        String blacklistOne = "cheese";
        String blacklistTwo = "mouse";

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
        verifyThat(searchPage.getText(), containsString(Errors.Search.NO_RESULTS));
        verifyThat(searchPage.getHeadingResultsCount(), is(0));
    }

    @Test
    @Category(SlowTest.class)
    @KnownBug({"CCUK-3471", "CSA-1808"})
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
    @KnownBug("IOD-8445")
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
    @KnownBug("IOD-8445")
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
