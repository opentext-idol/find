package com.autonomy.abc.keywords;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.framework.KnownBug;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.element.TriggerForm;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.search.IndexFilter;
import com.autonomy.abc.selenium.search.LanguageFilter;
import com.autonomy.abc.selenium.search.SearchQuery;
import com.autonomy.abc.selenium.search.SearchService;
import com.autonomy.abc.selenium.util.ElementUtil;
import com.autonomy.abc.selenium.util.Errors;
import com.autonomy.abc.selenium.util.PageUtil;
import com.autonomy.abc.selenium.util.Waits;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.CommonMatchers.containsItems;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.thoughtworks.selenium.SeleneseTestBase.fail;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;

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
        keywordService = getApplication().createKeywordService(getElementFactory());
        searchService = getApplication().createSearchService(getElementFactory());

        keywordsPage = keywordService.deleteAll(KeywordFilter.ALL);
    }

    @After
    public void tearDown() {
        keywordService.deleteAll(KeywordFilter.ALL);
    }

    //Blacklisted terms can be created on the searchpage. This link has often broken
    @Test
    @KnownBug("CSA-1521")
    public void testCreateBlacklistedTermFromSearchPage() throws InterruptedException {
        search("noir", Language.FRENCH);

        assertThat("No results for search noir", searchPage.waitForDocLogo().isDisplayed());
        assertThat("No add to blacklist link displayed", searchPage.blacklistLink().isDisplayed());
        assertThat("No create synonyms link displayed", searchPage.createSynonymsLink().isDisplayed());

        searchPage.blacklistLink().click();
        Waits.loadOrFadeWait();
        assertThat("link not directing to blacklist wizard", getDriver().getCurrentUrl(), containsString("keywords/create"));
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        assertThat("link not directing to blacklist wizard", createKeywordsPage.getText(), containsString("Select terms to blacklist"));

        TriggerForm triggerForm = createKeywordsPage.getTriggerForm();

        assertThat(triggerForm.getNumberOfTriggers(), is(1));
        assertThat("keywords list does not include term 'noir'", triggerForm.getTriggersAsStrings().contains("noir"));

        triggerForm.addTrigger("noir");
        assertThat(triggerForm.getNumberOfTriggers(), is(1));
        assertThat("keywords list does not include term 'noir'", triggerForm.getTriggersAsStrings().contains("noir"));

        createKeywordsPage.enabledFinishWizardButton().click();
        waitForKeywordCreation();
        keywordsPage = getElementFactory().getKeywordsPage();

        assertThat("Blacklisted term not added", keywordsPage.getBlacklistedTerms().contains("noir"));
    }

    //There is a link to create synonym group from the search page that prepopulates the create synonyms wizard with the current search term. Often breaks.
    @Test
    public void testCreateSynonymGroupFromSearchPage() throws InterruptedException {
        search("rouge", Language.FRENCH);

        assertThat("No results for search rouge", searchPage.waitForDocLogo().isDisplayed());
        assertThat("No add to blacklist link displayed", searchPage.blacklistLink().isDisplayed());
        assertThat("No create synonyms link displayed", searchPage.createSynonymsLink().isDisplayed());

        searchPage.createSynonymsLink().click();
        Waits.loadOrFadeWait();
        assertThat("link not directing to synonym group wizard", getDriver().getCurrentUrl(), containsString("keywords/create"));
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        assertThat("link not directing to synonym group wizard", createKeywordsPage.getText(), containsString("Select synonyms"));

        TriggerForm triggerForm = createKeywordsPage.getTriggerForm();

        assertThat(triggerForm.getNumberOfTriggers(), is(1));
        assertThat("keywords list does not include term 'rouge'", triggerForm.getTriggersAsStrings(), hasItem("rouge"));
        assertThat("Finish button should be disabled until further synonyms added", ElementUtil.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

        triggerForm.addTrigger("rouge");
        assertThat(triggerForm.getNumberOfTriggers(), is(1));
        assertThat("keywords list does not include term 'rouge'", triggerForm.getTriggersAsStrings(), hasItem("rouge"));
        assertThat("Finish button should be disabled until further synonyms added", ElementUtil.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

        triggerForm.addTrigger("red");
        assertThat(triggerForm.getNumberOfTriggers(), is(2));
        assertThat("keywords list does not include term 'rouge'", triggerForm.getTriggersAsStrings(),hasItem("rouge"));
        assertThat("keywords list does not include term 'red'", triggerForm.getTriggersAsStrings(), hasItem("red"));
        assertThat("Finish button should be enabled", !ElementUtil.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

        createKeywordsPage.enabledFinishWizardButton().click();
        searchPage.waitForSynonymsLoadingIndicatorToDisappear();
        getElementFactory().getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        Waits.loadOrFadeWait();
        keywordsPage.filterView(KeywordFilter.SYNONYMS);
        new WebDriverWait(getDriver(), 20).until(ExpectedConditions.visibilityOf(keywordsPage.selectLanguageButton()));
        //assertEquals("Blacklist has been created in the wrong language", "French", keywordsPage.getSelectedLanguage());

        keywordsPage.selectLanguage(Language.FRENCH);

        assertThat("Synonym, group not added", keywordsPage.getSynonymGroupSynonyms("rouge"),hasItem("red"));
        assertThat("Synonym, group not added", keywordsPage.getSynonymGroupSynonyms("red"), hasItem("rouge"));
        assertEquals(1, keywordsPage.countSynonymLists());
        assertEquals(2, keywordsPage.countKeywords());
    }

    //There is a link to create synonym group from the search page that prepopulates the create synonyms wizard with the current multi term search. Often breaks.
    @Test
    public void testCreateSynonymGroupFromMultiTermSearchOnSearchPage() throws InterruptedException {
        search("lodge dodge podge", Language.ENGLISH);

        assertThat("No results for search", searchPage.waitForDocLogo().isDisplayed());
        assertThat("No add to blacklist link displayed", searchPage.blacklistLink().isDisplayed());
        assertThat("No create synonyms link displayed", searchPage.createSynonymsLink().isDisplayed());

        searchPage.createSynonymsLink().click();
        Waits.loadOrFadeWait();
        assertThat("link not directing to synonym group wizard", getDriver().getCurrentUrl(), containsString("keywords/create"));
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        assertThat("link not directing to synonym group wizard", createKeywordsPage.getText(), containsString("Select synonyms"));

        TriggerForm triggerForm = createKeywordsPage.getTriggerForm();

        assertThat(triggerForm.getNumberOfTriggers(), is(3));
        assertThat("Wrong prospective blacklisted terms added", triggerForm.getTriggersAsStrings(), hasItems("lodge", "dodge", "podge"));
        assertThat("Finish button should be enabled", !ElementUtil.isAttributePresent(createKeywordsPage.enabledFinishWizardButton(), "disabled"));

        createKeywordsPage.enabledFinishWizardButton().click();
        new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
        getElementFactory().getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        Waits.loadOrFadeWait();
        keywordsPage.filterView(KeywordFilter.SYNONYMS);

        keywordsPage.selectLanguage(Language.ENGLISH);

        assertThat("Synonym, group not complete", keywordsPage.getSynonymGroupSynonyms("lodge"), hasItems("lodge", "dodge", "podge"));
        assertThat("Synonym, group not complete", keywordsPage.getSynonymGroupSynonyms("podge"), hasItems("lodge", "dodge", "podge"));
        assertThat("Synonym, group not complete", keywordsPage.getSynonymGroupSynonyms("dodge"), hasItems("lodge", "dodge", "podge"));

        assertEquals(1, keywordsPage.countSynonymLists());
        assertEquals(3, keywordsPage.countKeywords());
    }

    @Test
    public void testSearchPageKeywords() throws InterruptedException {
        assumeThat("Cannot modify keywords from search page in Hosted", getConfig().getType(), not(ApplicationType.HOSTED));

        List<String> synonymListBears = Arrays.asList("grizzly", "brownbear", "bigbear");
        searchPage = keywordService.addSynonymGroup(synonymListBears);

        for (final String synonym : synonymListBears) {
            assertThat(synonym + " not included in title", PageUtil.getPageTitle(getDriver()),containsString(synonym));
            assertThat(synonym + " not included in 'You searched for' section", searchPage.youSearchedFor(),hasItem(synonym));
            verifyThat(synonym + " synonym group complete in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym),containsItems(synonymListBears));
            verifyThat(searchPage.countSynonymLists(), is(1));
            verifyThat(searchPage.countKeywords(), is(synonymListBears.size()));
        }

        searchPage.addSynonymToGroup("kodiak", searchPage.synonymGroupContaining("grizzly"));
        for (final String synonym : synonymListBears) {
            assertThat(synonym + " not included in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym), containsItems(synonymListBears));
            assertThat("kodiak not included in synonym group " + synonym, searchPage.getSynonymGroupSynonyms(synonym), hasItem("kodiak"));
            assertEquals(1, searchPage.countSynonymLists());
            assertEquals(4, searchPage.countKeywords());
        }

        searchPage.deleteSynonym("bigbear");
        Waits.loadOrFadeWait();
        synonymListBears = Arrays.asList("grizzly", "brownbear");
        for (final String synonym : synonymListBears) {
            assertThat(synonym + " not included in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym),containsItems(synonymListBears));
            assertThat("bigbear not deleted from group " + synonym, searchPage.getSynonymGroupSynonyms(synonym),not(hasItem("bigbear")));
            assertThat("kodiak not included in synonym group " + synonym, searchPage.getSynonymGroupSynonyms(synonym),hasItem("kodiak"));
            assertEquals(1, searchPage.countSynonymLists());
            assertEquals(3, searchPage.countKeywords());
        }

        getElementFactory().getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        Waits.loadOrFadeWait();
        keywordsPage.selectLanguage(Language.ENGLISH);
        keywordsPage.filterView(KeywordFilter.SYNONYMS);
        assertEquals(1, keywordsPage.countSynonymLists());
        assertEquals(3, keywordsPage.countKeywords());

        synonymListBears = Arrays.asList("grizzly", "brownbear", "kodiak");
        for (final String synonym : synonymListBears) {
            assertThat(synonym + " group incomplete", keywordsPage.getSynonymGroupSynonyms(synonym), containsItems(synonymListBears));
            assertEquals(1, keywordsPage.countSynonymLists());
            assertEquals(3, keywordsPage.countKeywords());
            assertThat("bigbear not deleted from group " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym), not(hasItem("bigbear")));
        }
    }


    @Test
    @KnownBug("CCUK-2703")
    public void testNoBlacklistLinkForBlacklistedSearch() throws InterruptedException {
        String blacklistMessage = Errors.Search.BLACKLIST;
        if (config.getType().equals(ApplicationType.HOSTED)) {
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

        if(getConfig().getType().equals(ApplicationType.ON_PREM)){
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
        // TODO: CSA-1724 CSA-1893
//        verifyThat("blacklist term appears in query analysis", searchPage.getBlacklistedTerms(), hasItem("wizard"));
        assertThat("link to blacklist or create synonyms should not be present", searchPage,
                not(containsText("You can create synonyms or blacklist these search terms")));

        if (config.getType().equals(ApplicationType.ON_PREM)) {
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
            assertThat("Synonym group does not contain all its members", searchPage.getSynonymGroupSynonyms(synonym),containsItems(synonymListCars));
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
        verifyThat("Synonym group does not contain all its members", searchPage.getSynonymGroupSynonyms("house"), containsItems(houses));

        searchPage.addSynonymToGroup("lodging", searchPage.synonymGroupContaining("house"));
        houses.add("lodging");
        assertThat("New synonym has not been added to the group", searchPage.getSynonymGroupSynonyms("house"), containsItems(houses));

        searchPage.addSynonymToGroup("residence", searchPage.synonymGroupContaining("house"));
        houses.add("residence");
        assertThat("New synonym has not been added to the group", searchPage.getSynonymGroupSynonyms("house"), containsItems(houses));

        getElementFactory().getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        Waits.loadOrFadeWait();
        keywordsPage.filterView(KeywordFilter.ALL);
        assertThat("New synonym has not been added to the group", keywordsPage.getSynonymGroupSynonyms("house"), containsItems(houses));

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

        getElementFactory().getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        Waits.loadOrFadeWait();
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
        assertThat(getDriver().getCurrentUrl(), containsString("keywords/create"));
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();

        createKeywordsPage.getTriggerForm().addTrigger("한국");
        new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(createKeywordsPage.enabledFinishWizardButton())).click();
        searchPage = getElementFactory().getSearchPage();

        search("Korea", Language.CHINESE);
        verifyThat("synonyms appear on search page for correct language", searchPage.countSynonymLists(), is(1));

        searchPage.selectLanguage(Language.FRENCH);
        verifyThat("synonyms do not appear on search page for wrong language", searchPage.countSynonymLists(), is(0));

        getElementFactory().getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
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

        assertThat(getDriver().getCurrentUrl(), containsString("keywords/create"));

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
    @KnownBug({"CSA-1719", "CSA-1792"})
    public void testBlacklistTermsBehaveAsExpected() throws InterruptedException {
        String blacklistOne = "cheese";
        String blacklistTwo = "mouse";

        keywordsPage = keywordService.addBlacklistTerms(Language.ENGLISH, blacklistOne);
        assertThat(keywordsPage.getBlacklistedTerms(), hasItem(blacklistOne));

        search(blacklistOne, Language.ENGLISH);
        assertThat(searchPage.getText(), containsString(Errors.Search.NO_RESULTS));

        getDriver().navigate().refresh();

        getElementFactory().getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        keywordsPage = getElementFactory().getKeywordsPage();
        assertThat(keywordsPage.getBlacklistedTerms(), hasItem(blacklistOne));

        keywordsPage = keywordService.addBlacklistTerms(blacklistTwo);
        assertThat(keywordsPage.getBlacklistedTerms(), hasItem(blacklistOne));
        assertThat(keywordsPage.getBlacklistedTerms(), hasItem(blacklistTwo));

        search(blacklistTwo, Language.ENGLISH);
        assertThat(searchPage.getText(), containsString(Errors.Search.NO_RESULTS));

        search(blacklistOne, Language.ENGLISH);
        assertThat(searchPage.getText(), containsString(Errors.Search.NO_RESULTS));
    }

    @Test
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
        SearchQuery query = new SearchQuery(searchTerm).withFilter(new LanguageFilter(language));
        if (getConfig().getType().equals(ApplicationType.HOSTED)) {
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
