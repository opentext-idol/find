package com.autonomy.abc.keywords;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.search.Search;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.thoughtworks.selenium.SeleneseTestBase.fail;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

public class KeywordsFromSearchITCase extends ABCTestBase {
    private CreateNewKeywordsPage createKeywordsPage;
    private SearchPage searchPage;
    private KeywordsPage keywordsPage;

    public KeywordsFromSearchITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    public void setUp() {
        body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        keywordsPage = getElementFactory().getKeywordsPage();
    }

    //CSA-1521
    //Blacklisted terms can be created on the searchpage. This link has often broken
    @Test
    public void testCreateBlacklistedTermFromSearchPage() throws InterruptedException {
        body.getTopNavBar().search("noir");
        searchPage = getElementFactory().getSearchPage();
        searchPage.selectLanguage("French");
        searchPage.waitForSearchLoadIndicatorToDisappear();

        if(getConfig().getType().equals(ApplicationType.HOSTED)) {
            searchPage.selectNewsEngIndex();
            searchPage.waitForSearchLoadIndicatorToDisappear();
        }

        assertThat("No results for search noir", searchPage.waitForDocLogo().isDisplayed());
        assertThat("No add to blacklist link displayed", searchPage.blacklistLink().isDisplayed());
        assertThat("No create synonyms link displayed", searchPage.createSynonymsLink().isDisplayed());

        searchPage.blacklistLink().click();
        searchPage.loadOrFadeWait();
        assertThat("link not directing to blacklist wizard", getDriver().getCurrentUrl(), containsString("keywords/create"));
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        assertThat("link not directing to blacklist wizard", createKeywordsPage.getText(), containsString("Select terms to blacklist"));
        assertEquals(1, createKeywordsPage.countKeywords(KeywordsPage.KeywordsFilter.BLACKLIST));
        assertThat("keywords list does not include term 'noir'", createKeywordsPage.getProspectiveKeywordsList().contains("noir"));

        createKeywordsPage.addBlacklistedTextBox().sendKeys("noir");
        createKeywordsPage.addBlacklistTermsButton().click();
        assertEquals(1, createKeywordsPage.countKeywords(KeywordsPage.KeywordsFilter.BLACKLIST));
        assertThat("keywords list does not include term 'noir'", createKeywordsPage.getProspectiveKeywordsList().contains("noir"));

        createKeywordsPage.enabledFinishWizardButton().click();
        new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
        assertThat("Blacklisted term not added", keywordsPage.getBlacklistedTerms().contains("noir"));
    }

    //There is a link to create synonym group from the search page that prepopulates the create synonyms wizard with the current search term. Often breaks.
    @Test
    public void testCreateSynonymGroupFromSearchPage() throws InterruptedException {
        body.getTopNavBar().search("rouge");
        searchPage = getElementFactory().getSearchPage();
        searchPage.waitForSearchLoadIndicatorToDisappear();

        if(getConfig().getType().equals(ApplicationType.HOSTED)) {
            searchPage.selectNewsEngIndex();
            searchPage.waitForSearchLoadIndicatorToDisappear();
        }

        searchPage.selectLanguage("French");

        assertThat("No results for search rouge", searchPage.waitForDocLogo().isDisplayed());
        assertThat("No add to blacklist link displayed", searchPage.blacklistLink().isDisplayed());
        assertThat("No create synonyms link displayed", searchPage.createSynonymsLink().isDisplayed());

        searchPage.createSynonymsLink().click();
        searchPage.loadOrFadeWait();
        assertThat("link not directing to synonym group wizard", getDriver().getCurrentUrl(),containsString("keywords/create"));
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        assertThat("link not directing to synonym group wizard", createKeywordsPage.getText(), containsString("Select synonyms"));
        assertEquals(1, createKeywordsPage.countKeywords(KeywordsPage.KeywordsFilter.SYNONYMS));
        assertThat("keywords list does not include term 'rouge'", createKeywordsPage.getProspectiveKeywordsList(), hasItem("rouge"));
        assertThat("Finish button should be disabled until further synonyms added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

        createKeywordsPage.addSynonymsTextBox().sendKeys("rouge");
        createKeywordsPage.addSynonymsButton().click();
        assertEquals(1, createKeywordsPage.countKeywords(KeywordsPage.KeywordsFilter.SYNONYMS));
        assertThat("keywords list does not include term 'rouge'", createKeywordsPage.getProspectiveKeywordsList(), hasItem("rouge"));
        assertThat("Finish button should be disabled until further synonyms added", createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

        createKeywordsPage.addSynonymsTextBox().clear();
        createKeywordsPage.addSynonymsTextBox().sendKeys("red");
        createKeywordsPage.addSynonymsButton().click();
        assertEquals(2, createKeywordsPage.countKeywords(KeywordsPage.KeywordsFilter.SYNONYMS));
        assertThat("keywords list does not include term 'rouge'", createKeywordsPage.getProspectiveKeywordsList(),hasItem("rouge"));
        assertThat("keywords list does not include term 'red'", createKeywordsPage.getProspectiveKeywordsList(), hasItem("red"));
        assertThat("Finish button should be enabled", !createKeywordsPage.isAttributePresent(createKeywordsPage.finishWizardButton(), "disabled"));

        createKeywordsPage.enabledFinishWizardButton().click();
        new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
        body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        keywordsPage.loadOrFadeWait();
        keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
        new WebDriverWait(getDriver(), 20).until(ExpectedConditions.visibilityOf(keywordsPage.selectLanguageButton()));
        //assertEquals("Blacklist has been created in the wrong language", "French", keywordsPage.getSelectedLanguage());

        keywordsPage.selectLanguage("French");

        assertThat("Synonym, group not added", keywordsPage.getSynonymGroupSynonyms("rouge"),hasItem("red"));
        assertThat("Synonym, group not added", keywordsPage.getSynonymGroupSynonyms("red"), hasItem("rouge"));
        assertEquals(1, keywordsPage.countSynonymLists());
        assertEquals(2, keywordsPage.countKeywords());
    }

    //There is a link to create synonym group from the search page that prepopulates the create synonyms wizard with the current multi term search. Often breaks.
    @Test
    public void testCreateSynonymGroupFromMultiTermSearchOnSearchPage() throws InterruptedException {
        body.getTopNavBar().search("lodge dodge podge");
        searchPage = getElementFactory().getSearchPage();
        searchPage.waitForSearchLoadIndicatorToDisappear();

        if(getConfig().getType().equals(ApplicationType.HOSTED)) {
            searchPage.selectNewsEngIndex();
            searchPage.waitForSearchLoadIndicatorToDisappear();
        }

        searchPage.selectLanguage("English");

        assertThat("No results for search", searchPage.waitForDocLogo().isDisplayed());
        assertThat("No add to blacklist link displayed", searchPage.blacklistLink().isDisplayed());
        assertThat("No create synonyms link displayed", searchPage.createSynonymsLink().isDisplayed());

        searchPage.createSynonymsLink().click();
        searchPage.loadOrFadeWait();
        assertThat("link not directing to synonym group wizard", getDriver().getCurrentUrl(), containsString("keywords/create"));
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        assertThat("link not directing to synonym group wizard", createKeywordsPage.getText(), containsString("Select synonyms"));
        assertEquals(3, createKeywordsPage.countKeywords(KeywordsPage.KeywordsFilter.SYNONYMS));
        assertThat("Wrong prospective blacklisted terms added", createKeywordsPage.getProspectiveKeywordsList(), hasItems("lodge", "dodge", "podge"));
        assertThat("Finish button should be enabled", !createKeywordsPage.isAttributePresent(createKeywordsPage.enabledFinishWizardButton(), "disabled"));

        createKeywordsPage.enabledFinishWizardButton().click();
        new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
        body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        keywordsPage.loadOrFadeWait();
        keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);

        keywordsPage.selectLanguage("English");

        assertThat("Synonym, group not complete", keywordsPage.getSynonymGroupSynonyms("lodge"), hasItems("lodge", "dodge", "podge"));
        assertThat("Synonym, group not complete", keywordsPage.getSynonymGroupSynonyms("podge"), hasItems("lodge", "dodge", "podge"));
        assertThat("Synonym, group not complete", keywordsPage.getSynonymGroupSynonyms("dodge"),hasItems("lodge", "dodge", "podge"));

        assertEquals(1, keywordsPage.countSynonymLists());
        assertEquals(3, keywordsPage.countKeywords());
    }

    @Ignore("Ignoring test modifying keywords from search page")
    @Test
    public void testSearchPageKeywords() throws InterruptedException {
        keywordsPage.createNewKeywordsButton().click();
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        List<String> synonymListBears = Arrays.asList("grizzly", "brownbear", "bigbear");
        createKeywordsPage.createSynonymGroup(StringUtils.join(synonymListBears, ' '), "English");
        searchPage = getElementFactory().getSearchPage();

        for (final String synonym : synonymListBears) {
            assertThat(synonym + " not included in title", searchPage.title(),containsString(synonym));
            assertThat(synonym + " not included in 'You searched for' section", searchPage.youSearchedFor(),hasItem(synonym));
            verifyThat(synonym + " synonym group complete in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym),containsItems(synonymListBears));
            verifyThat(searchPage.countSynonymLists(), is(1));
            verifyThat(searchPage.countKeywords(), is(1));
        }

        searchPage.addSynonymToGroup("kodiak", "grizzly");
        searchPage.loadOrFadeWait();
        for (final String synonym : synonymListBears) {
            assertThat(synonym + " not included in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym), containsItems(synonymListBears));
            assertThat("kodiak not included in synonym group " + synonym, searchPage.getSynonymGroupSynonyms(synonym),hasItem("kodiak"));
            assertEquals(1, searchPage.countSynonymLists());
            assertEquals(4, searchPage.countKeywords());
        }

        searchPage.deleteSynonym("bigbear");
        searchPage.loadOrFadeWait();
        synonymListBears = Arrays.asList("grizzly", "brownbear");
        for (final String synonym : synonymListBears) {
            assertThat(synonym + " not included in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym),containsItems(synonymListBears));
            assertThat("bigbear not deleted from group " + synonym, searchPage.getSynonymGroupSynonyms(synonym),not(hasItem("bigbear")));
            assertThat("kodiak not included in synonym group " + synonym, searchPage.getSynonymGroupSynonyms(synonym),hasItem("kodiak"));
            assertEquals(1, searchPage.countSynonymLists());
            assertEquals(3, searchPage.countKeywords());
        }

        body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
        keywordsPage.loadOrFadeWait();
        keywordsPage.selectLanguage("English");
        keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);
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
    //CCUK-2703
    public void testNoBlacklistLinkForBlacklistedSearch() throws InterruptedException {
        body.getTopNavBar().search("wizard");
        searchPage = getElementFactory().getSearchPage();

        searchPage.selectLanguage("Arabic");

        searchPage.blacklistLink().click();
        try {
            createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
            createKeywordsPage.enabledFinishWizardButton().click();
        } catch (final NoSuchElementException e) {
            fail("blacklist link on search page has not navigated to the wizard");
        }

        keywordsPage.selectLanguageButton();	//Wait for select Language button

        if(getConfig().getType().equals(ApplicationType.ON_PREM)){
            assertThat("Blacklist has been created in the wrong language", keywordsPage.getSelectedLanguage(), equalToIgnoringCase("Arabic"));
        }

        keywordsPage.loadOrFadeWait();
        new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
        keywordsPage.filterView(KeywordsPage.KeywordsFilter.BLACKLIST);

        keywordsPage.selectLanguage("Arabic");

        assertThat("Blacklisted term not created", keywordsPage.getBlacklistedTerms(), hasItem("wizard"));

        body.getTopNavBar().search("wizard");
        new WebDriverWait(getDriver(), 4).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));

        searchPage.selectLanguage("Arabic");

        assertThat("'You searched for:' section incorrect", searchPage.youSearchedFor(), hasItem("wizard"));
        verifyThat("Keywords incorrect", searchPage.getBlacklistedTerms(), hasItem("wizard"));
        assertThat("link to blacklist or create synonyms should not be present", searchPage.getText(),
                not(containsString("You can create synonyms or blacklist these search terms")));

        searchPage.selectLanguage("English");

        assertThat("Term should not be blacklisted in English", searchPage.getText(),not(containsString("Any query terms were either blacklisted or stop words")));
    }

    @Ignore("Ignoring test modifying keywords from search page")
    @Test
    public void testSynonymGroupMembersSearchWholeGroup() throws InterruptedException {
        keywordsPage.createNewKeywordsButton().click();
        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        final List<String> synonymListCars = Arrays.asList("car", "auto", "motor");
        createKeywordsPage.createSynonymGroup(StringUtils.join(synonymListCars, ' '), "Swahili");

        searchPage = getElementFactory().getSearchPage();

        for (final String synonym : synonymListCars) {
            body.getTopNavBar().search(synonym);

            searchPage.selectLanguage("Swahili");

            assertEquals(1, searchPage.countSynonymLists());
            assertEquals(3, createKeywordsPage.countKeywords());
            assertThat("Synonym group does not contain all its members", searchPage.getSynonymGroupSynonyms(synonym),containsInAnyOrder(synonymListCars.toArray()));
        }
    }

    @Ignore("Ignoring test modifying keywords from search page")
    @Test
    public void testAddTwoSynonymsToSynonymGroupFromSearchPage() throws InterruptedException {
        try {
            keywordsPage.createNewKeywordsButton().click();
            createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
            createKeywordsPage.createSynonymGroup("house home dwelling abode", "English");

            searchPage = getElementFactory().getSearchPage();
            body.getTopNavBar().search("house");

            searchPage.selectLanguage("English");

            searchPage.waitForSynonymsLoadingIndicatorToDisappear();
            assertEquals(1, searchPage.countSynonymLists());
            assertEquals(4, createKeywordsPage.countKeywords());
            verifyThat("Synonym group does not contain all its members", searchPage.getSynonymGroupSynonyms("house"), hasItems("home", "dwelling", "abode"));

            searchPage.addSynonymToGroup("lodging", "house");
            searchPage.waitForSynonymsLoadingIndicatorToDisappear();
            assertThat("New synonym has not been added to the group", searchPage.getSynonymGroupSynonyms("house"), hasItems("home", "dwelling", "abode", "lodging"));

            searchPage.addSynonymToGroup("residence", "house");
            searchPage.waitForSynonymsLoadingIndicatorToDisappear();
            assertThat("New synonym has not been added to the group", searchPage.getSynonymGroupSynonyms("house"), hasItems("home", "dwelling", "abode", "lodging", "residence"));

            body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
            keywordsPage.loadOrFadeWait();
            keywordsPage.filterView(KeywordsPage.KeywordsFilter.ALL_TYPES);
            assertThat("New synonym has not been added to the group", keywordsPage.getSynonymGroupSynonyms("house"), hasItems("home", "dwelling", "abode", "lodging", "residence"));

            keywordsPage.deleteKeywords();
            keywordsPage.loadOrFadeWait();

            (new WebDriverWait(getDriver(),10)).until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver driver) {
                    return keywordsPage.countSynonymLists() == 0;
                }
            });

            assertEquals(0, keywordsPage.countSynonymLists());
        } finally {
            getDriver().navigate().refresh();
        }
    }

    @Ignore("Ignoring test modifying keywords from search page")
    @Test
    public void testRemoveTwoSynonymsFromSynonymGroupFromSearchPage() throws InterruptedException {
        try {
            keywordsPage.createNewKeywordsButton().click();
            createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
            createKeywordsPage.createSynonymGroup("house home dwelling abode residence", "English");

            searchPage = getElementFactory().getSearchPage();
            body.getTopNavBar().search("house");

            searchPage.selectLanguage("English");

            verifyThat(searchPage.countSynonymLists(), is(1));
            verifyThat(searchPage.countKeywords(), is(5));
            verifyThat(searchPage.getSynonymGroupSynonyms("house"), hasItems("home", "dwelling", "abode", "residence"));

            searchPage.deleteSynonym("residence", "house");
            searchPage.loadOrFadeWait();
            verifyThat("Synonym has been deleted", searchPage.getSynonymGroupSynonyms("house"), not(hasItem("residence")));
            verifyThat("Synonym has not been deleted", searchPage.getSynonymGroupSynonyms("house"), hasItem("abode"));
            verifyThat("1 synonym deleted", searchPage.getSynonymGroupSynonyms("house"), hasItems("home", "dwelling", "abode"));

            searchPage.deleteSynonym("abode", "house");
            searchPage.loadOrFadeWait();
            verifyThat("Synonym has been deleted", searchPage.getSynonymGroupSynonyms("house"), not(hasItem("abode")));
            verifyThat("2 synonyms deleted", searchPage.getSynonymGroupSynonyms("house"), hasItems("home", "dwelling"));

            searchPage.deleteSynonym("dwelling", "house");
            searchPage.loadOrFadeWait();
            verifyThat("Synonym has been deleted", searchPage.getSynonymGroupSynonyms("house"), not(hasItem("dwelling")));
            verifyThat("Synonym has been deleted", searchPage.getSynonymGroupSynonyms("house"), not(hasItem("abode")));
            verifyThat("Synonym has been deleted", searchPage.getSynonymGroupSynonyms("house"), not(hasItem("residence")));
            verifyThat("3 synonyms deleted", searchPage.getSynonymGroupSynonyms("house"), hasItem("home"));

            body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
            keywordsPage.loadOrFadeWait();
            keywordsPage.filterView(KeywordsPage.KeywordsFilter.ALL_TYPES);
            assertThat("Synonyms have been removed from the group", keywordsPage.getSynonymGroupSynonyms("house"), hasItems("home", "house"));

            keywordsPage.deleteKeywords();		//TODO get deleteAllSynonyms to work again
            keywordsPage.loadOrFadeWait();

            keywordsPage.filterView(KeywordsPage.KeywordsFilter.SYNONYMS);

            verifyThat(keywordsPage.countSynonymLists(), is(0));
        } finally {
            getDriver().navigate().refresh();
        }
    }

    @Test
    public void testAddingSynonymGroupFromSearchPageOnlyAddsWords(){
        String phrase = "the quick brown fox jumps over the lazy dog";
        body.getTopNavBar().search(phrase);
        searchPage = getElementFactory().getSearchPage();
        searchPage.selectLanguage("English");

        searchPage.createSynonymsLink().click();

        assertThat(getDriver().getCurrentUrl(),containsString("keywords/create"));

        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();

        ArrayList<String> wordsInPhrase = new ArrayList<>(Arrays.asList(phrase.split(" ")));
        wordsInPhrase.removeAll(Collections.singleton("the"));

        List<String> prospectiveKeywords = createKeywordsPage.getProspectiveKeywordsList();

        assertThat(prospectiveKeywords, containsItems(wordsInPhrase));
        assertThat(prospectiveKeywords, not(hasItem("the")));
        assertThat(wordsInPhrase.size(),is(prospectiveKeywords.size()));
    }


    @Test
    //CSA1694
    public void testCancellingKeywordsWizardDoesntBreakSearch(){
        new Search(getApplication(),getElementFactory(),"apu").apply();

        searchPage = getElementFactory().getSearchPage();
        searchPage.createSynonymsLink().click();

        createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
        createKeywordsPage.cancelWizardButton().click();

        try {
            searchPage.waitForSearchLoadIndicatorToDisappear();
        } catch (TimeoutException e) {
            fail("Search does not find results after cancelling synonym wizard");
        }
    }

    private Matcher<Iterable<String>> containsItems(List<String> list) {
        return hasItems(list.toArray(new String[list.size()]));
    }
}
