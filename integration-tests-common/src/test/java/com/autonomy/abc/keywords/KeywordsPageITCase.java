package com.autonomy.abc.keywords;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import com.autonomy.abc.selenium.util.Language;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.util.AppElement.getParent;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeThat;
import static org.openqa.selenium.lift.Matchers.displayed;

public class KeywordsPageITCase extends ABCTestBase {
	private final static Logger LOGGER = LoggerFactory.getLogger(KeywordsPageITCase.class);
	private KeywordsPage keywordsPage;
	private CreateNewKeywordsPage createKeywordsPage;
	private SearchPage searchPage;
	private NotificationsDropDown notifications;
	private KeywordService keywordService;
	private SearchActionFactory searchFactory;
	
	public KeywordsPageITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	@Before
	public void setUp() throws MalformedURLException {
		keywordService = new KeywordService(getApplication(), getElementFactory());
		searchFactory = new SearchActionFactory(getApplication(), getElementFactory());

		keywordsPage = keywordService.deleteAll(KeywordFilter.ALL);
    }

	@After
	public void tearDown() {
		keywordService.deleteAll(KeywordFilter.ALL);
	}

	@Test
	public void testKeywordsFilter() throws InterruptedException {
		final WebDriverWait wait = new WebDriverWait(getDriver(), 5);
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("dog hound canine", "English");

		searchPage = getElementFactory().getSearchPage();
		wait.until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		assertThat("New keyword not searched for", searchPage.searchTitle().getText(), containsString("dog"));
		assertThat("New keyword not searched for", searchPage.searchTitle().getText(), containsString("hound"));
		assertThat("New keyword not searched for", searchPage.searchTitle().getText(), containsString("canine"));

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		wait.until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.filterView(KeywordFilter.ALL);
		keywordsPage.selectLanguage("English");
		assertThat("Synonym group dog not visible", keywordsPage.getSynonymGroupSynonyms("dog"), hasItems("hound", "canine"));
		assertThat("Synonym group hound not visible", keywordsPage.getSynonymGroupSynonyms("hound"), hasItems("dog", "canine"));
		assertThat("Synonym group canine not visible", keywordsPage.getSynonymGroupSynonyms("canine"), hasItems("dog", "hound"));

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("illegal", "English");

		new WebDriverWait(getDriver(),20).until(ExpectedConditions.visibilityOf(keywordsPage));

		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();
		new WebDriverWait(getDriver(),45).until(GritterNotice.notificationContaining("illegal"));

		assertThat("Blacklisted term 'illegal' not visible", keywordsPage.getBlacklistedTerms(), hasItem("illegal"));

		keywordsPage.filterView(KeywordFilter.SYNONYMS);
		assertThat("Blacklist terms are still visible", keywordsPage.getBlacklistedTerms().size() == 0);
		assertThat("A synonym list on row 2 is not visible", keywordsPage.synonymList(1).isDisplayed(), is(Boolean.FALSE));
		assertThat("Synonym group dog not visible", keywordsPage.getSynonymGroupSynonyms("dog"), hasItems("hound", "canine"));
		assertThat("Synonym group hound not visible", keywordsPage.getSynonymGroupSynonyms("hound"), hasItems("dog", "canine"));
		assertThat("Synonym group canine not visible", keywordsPage.getSynonymGroupSynonyms("canine"), hasItems("dog", "hound"));

		keywordsPage.filterView(KeywordFilter.BLACKLIST);
		assertThat("Blacklisted term 'illegal' not visible", keywordsPage.getBlacklistedTerms(), hasItem("illegal"));
		assertThat("There should not be a a synonym list on row 2", keywordsPage.countSynonymLists(), is(0));

		keywordsPage.filterView(KeywordFilter.ALL);
		assertThat("A synonym list should be visible on row 2", keywordsPage.synonymList(1).isDisplayed());
		assertThat("Synonym group dog not visible", keywordsPage.getSynonymGroupSynonyms("dog"), hasItems("hound", "canine"));
		assertThat("Synonym group hound not visible", keywordsPage.getSynonymGroupSynonyms("hound"), hasItems("dog", "canine"));
		assertThat("Synonym group canine not visible", keywordsPage.getSynonymGroupSynonyms("canine"),hasItems("dog", "hound"));
		assertThat("Blacklist term illegal is not visible", keywordsPage.getBlacklistedTerms(), hasItem("illegal"));
	}

	@Test
	public void testDeleteKeywords() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("frog toad amphibian tadpole", "English");
		searchPage = getElementFactory().getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.filterView(KeywordFilter.ALL);

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		assertThat("synonym group not fully created", keywordsPage.getSynonymGroupSynonyms("frog"), hasItems("frog", "toad", "amphibian", "tadpole"));
		assertThat("Wrong number of synonym lists displayed", keywordsPage.countSynonymLists(), is(1));
		assertThat("Wrong number of synonyms in group frog", keywordsPage.getSynonymGroupSynonyms("frog").size(), is(4));

		keywordsPage.deleteSynonym("amphibian", "toad");
		assertThat("Wrong number of synonym lists displayed", keywordsPage.countSynonymLists(), is(1));
		assertThat("Wrong number of synonyms in group toad", keywordsPage.getSynonymGroupSynonyms("toad").size(), is(3));
		assertThat("the synonym amphibian should be deleted from every synonym list", keywordsPage.getSynonymGroupSynonyms("tadpole"), not(hasItems("amphibian")));
		assertThat("the synonym amphibian should be deleted from every synonym list", keywordsPage.getSynonymGroupSynonyms("toad"), not(hasItems("amphibian")));
		assertThat("the synonym amphibian should be deleted from every synonym list", keywordsPage.getSynonymGroupSynonyms("frog"), not(hasItems("amphibian")));

		keywordsPage.deleteSynonym("frog", "frog");
		assertThat("Wrong number of synonym lists displayed", keywordsPage.countSynonymLists(), is(1));
		assertThat("Wrong number of synonyms in group toad", keywordsPage.getSynonymGroupSynonyms("toad").size(), is(2));
		assertThat("the synonym frog should be deleted from every synonym list", keywordsPage.getSynonymGroupSynonyms("toad"), not(hasItems("frog")));
		assertThat("the synonym frog should be deleted from every synonym list", keywordsPage.getSynonymGroupSynonyms("tadpole"), not(hasItems("frog")));

		keywordsPage.deleteSynonym("tadpole", "toad");
		assertThat("Wrong number of synonym lists displayed", keywordsPage.countSynonymLists(), is(0));
	}


	//The keyword 'wine' exists in two different synonym groups. Tests that deleting this keyword does not effect the other synonym group
	@Test
	public void testDeleteSynonymsFromOverlappingSynonymGroups() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("wine merlot shiraz bordeaux", "English");
		searchPage = getElementFactory().getSearchPage();
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);

		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("wine red scarlet burgundy", "English");
		searchPage = getElementFactory().getSearchPage();
		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);

		new WebDriverWait(getDriver(), 10).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.filterView(KeywordFilter.SYNONYMS);
		keywordsPage.selectLanguage("English");
		assertThat("synonym group not fully created", keywordsPage.getSynonymGroupSynonyms("red"), hasItems("red", "scarlet", "wine", "burgundy"));
		assertThat(keywordsPage.countSynonymLists(), is(2));
		assertThat(keywordsPage.countKeywords(), is(8));
		assertThat(keywordsPage.getSynonymGroupSynonyms("burgundy").size(), is(4));
		assertThat(keywordsPage.getSynonymGroupSynonyms("merlot").size(), is(4));
		assertThat(keywordsPage.countSynonymGroupsWithSynonym("wine"), is(2));

		keywordsPage.deleteSynonym("bordeaux", "shiraz");
		assertThat(keywordsPage.countSynonymLists(), is(2));
		assertThat(keywordsPage.countKeywords(), is(7));
		assertThat(keywordsPage.getSynonymGroupSynonyms("merlot").size(), is(3));
		assertThat(keywordsPage.getSynonymGroupSynonyms("scarlet").size(), is(4));
		assertThat(keywordsPage.countSynonymGroupsWithSynonym("wine"), is(2));

		keywordsPage.deleteSynonym("burgundy", "red");
		assertThat(keywordsPage.countSynonymLists(), is(2));
		assertThat(keywordsPage.countKeywords(), is(6));
		assertThat(keywordsPage.getSynonymGroupSynonyms("merlot").size(), is(3));
		assertThat(keywordsPage.getSynonymGroupSynonyms("scarlet").size(), is(3));
		assertThat(keywordsPage.countSynonymGroupsWithSynonym("wine"), is(2));

		keywordsPage.deleteSynonym("wine", keywordsPage.getSynonymGroup("red"));
		assertThat(keywordsPage.countSynonymLists(), is(2));
		assertThat(keywordsPage.countKeywords(), is(5));
		assertThat(keywordsPage.getSynonymGroupSynonyms("merlot").size(), is(3));
		assertThat(keywordsPage.getSynonymGroupSynonyms("scarlet").size(), is(2));
		assertThat(keywordsPage.countSynonymGroupsWithSynonym("wine"), is(1));

		keywordsPage.deleteSynonym("shiraz", "wine");
		assertThat(keywordsPage.countSynonymLists(), is(2));
		assertThat(keywordsPage.countKeywords(), is(4));
		assertThat(keywordsPage.getSynonymGroupSynonyms("merlot").size(), is(2));
		assertThat(keywordsPage.getSynonymGroupSynonyms("scarlet").size(), is(2));
		assertThat(keywordsPage.countSynonymGroupsWithSynonym("wine"), is(1));

		keywordsPage.deleteSynonym("scarlet", "red");
		assertThat(keywordsPage.countSynonymLists(), is(1));
		assertThat(keywordsPage.countKeywords(), is(2));
		assertThat(keywordsPage.getSynonymGroupSynonyms("merlot").size(), is(2));
		assertThat(keywordsPage.countSynonymGroupsWithSynonym("wine"), is(1));

		keywordsPage.deleteSynonym("wine", "merlot");
		assertThat(keywordsPage.countSynonymLists(), is(0));
	}

	@Ignore
	// This takes too long for a nightly test but is a useful test that need run periodically as the application has failed in the past with a large number of synonym groups.
	// Failure can present itself on other pages other than the KeywordsPage
	@Test
	public void testAddLotsOfSynonymGroups() throws IOException, InterruptedException {
		keywordsPage.deleteAllSynonyms();
		keywordsPage.deleteAllBlacklistedTerms();
		final List<String> groupsOfFiveSynonyms = keywordsPage.loadTextFileLineByLineIntoList("C://dev//res//100.txt");

		for (final String synonymGroup : groupsOfFiveSynonyms) {
			keywordsPage.createNewKeywordsButton().click();
			createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
			createKeywordsPage.createSynonymGroup(synonymGroup, "English");

			body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
			assertThat("Wrong number of synonym lists", keywordsPage.countSynonymLists() == groupsOfFiveSynonyms.indexOf(synonymGroup) + 1);
		}
	}

	//Whitespace, Odd number of quotes or quotes with blank text, boolean operators or proximity operators should not be able to added as keywords. This test checks they can't be added to existing synonyms on the Keywords Page
	@Test
	public void testAddingWhitespaceQuotesBooleansProximityOperatorsOnKeywordsPage() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("one two three", "English");
		searchPage = (SearchPage) new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOf(getElementFactory().getSearchPage()));
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.filterView(KeywordFilter.SYNONYMS);

		//keywordsPage.selectLanguage("English");
		LOGGER.warn("Cannot select language for blacklists yet");

		keywordsPage.addSynonymToGroup("four", "one");
		keywordsPage.waitForRefreshIconToDisappear();
		assertThat("there should be four synonyms in a group", keywordsPage.getSynonymGroupSynonyms("two"), hasItem("four"));
		assertThat(keywordsPage.countSynonymLists(), is(1));
		assertThat(keywordsPage.countKeywords(), is(4));

		keywordsPage.synonymGroupPlusButton("three").click();
		keywordsPage.synonymGroupTextBox("three").clear();
		keywordsPage.synonymGroupTickButton("three").click();
		assertThat("add synonym box should still be displayed", keywordsPage.synonymGroupTextBox("three").isDisplayed());

		keywordsPage.searchFilterTextBox().click();
		assertThat("there should be four synonyms in a group", keywordsPage.getSynonymGroupSynonyms("two"), hasItem("four"));
		assertThat(keywordsPage.countSynonymLists(), is(1));
		assertThat(keywordsPage.countKeywords(), is(4));

		notifications = body.getTopNavBar().getNotifications();

		for (final String badSynonym : Arrays.asList(" ", "\t", "\"")) {
			addSynonymKeywordsPage("three",badSynonym);
			assertThat("add synonym box should still be displayed. Offending term is " + badSynonym, keywordsPage.synonymGroupTextBox("three").isDisplayed());

			new WebDriverWait(getDriver(),30).until(new ExpectedCondition<Boolean>() {
				@Override
				public Boolean apply(WebDriver webDriver) {
					return keywordsPage.synonymGroupTextBox("three").isEnabled();
				}
			});

			keywordsPage.loadOrFadeWait();
			keywordsPage.searchFilterTextBox().click();
			assertThat("there should be four synonyms in a group. Offending term is " + badSynonym, keywordsPage.getSynonymGroupSynonyms("one").size() == 4);
			assertThat(keywordsPage.countSynonymLists(), is(1));
			assertThat(keywordsPage.countKeywords(), is(4));
		}

		int synonymGroupSize = 4;
		for(final String operatorSynonym : Arrays.asList("NOT", "NEAR", "DNEAR", "XNEAR", "YNEAR", "NEAR123", "SENTENCE2", "PARAGRAPH3", "AND", "BEFORE", "AFTER", "WHEN", "SENTENCE", "PARAGRAPH", "OR", "WNEAR", "EOR", "NOTWHEN")){
			addSynonymKeywordsPage("three", operatorSynonym);
			keywordsPage.waitForRefreshIconToDisappear();
			assertThat(keywordsPage.getSynonymGroupSynonyms("three"), hasItem(operatorSynonym.toLowerCase()));

			assertThat(keywordsPage.getSynonymGroupSynonyms("three"), hasSize(++synonymGroupSize));
			assertThat(keywordsPage.countSynonymLists(), is(1));
		}
	}

	private void addSynonymKeywordsPage(String leadSynonym,String addSynonym){
		keywordsPage.synonymGroupPlusButton(leadSynonym).click();
		keywordsPage.synonymGroupTextBox(leadSynonym).clear();
		keywordsPage.synonymGroupTextBox(leadSynonym).sendKeys(addSynonym);
		keywordsPage.synonymGroupTickButton(leadSynonym).click();
	}

	//Phrases should be able to be added as synonyms from the keywords page
	@Test
	public void testPhrasesCanBeAddedAsSynonymsOnKeywordsPage() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("one two three", "English");
		searchPage = getElementFactory().getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.filterView(KeywordFilter.SYNONYMS);
		keywordsPage.selectLanguage("English");
		keywordsPage.synonymGroupPlusButton("three").click();
		keywordsPage.synonymGroupTextBox("three").clear();
		keywordsPage.synonymGroupTextBox("three").sendKeys("four and five");
		keywordsPage.synonymGroupTickButton("three").click();

		//This is to make sure 'four and five' have finished adding before checking whether the box is still displayed
		new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOfElementLocated
				(By.xpath("//div[contains(concat(' ', normalize-space(@class), ' '), 'gritter-without-image')]//p[text()[contains(.,'four and five')]]")));

		assertThat("New Synonym Box is displayed", keywordsPage.synonymGroupTextBox("three"), not(displayed()));
		assertThat(keywordsPage.countSynonymLists(), is(1));
		assertThat(keywordsPage.countKeywords(), is(4));
		assertThat(keywordsPage.getSynonymGroupSynonyms("three"), hasItem("four and five"));
	}

	//Notification should drop down on creation of new keywords
	//CSA-1447
	@Test
	public void testNotificationForCreatedBlacklistedTermAndSynonymGroup() throws InterruptedException {
		List<String> notificationContents = new ArrayList<>();

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("orange", "English");
		notificationContents.add("Added \"orange\" to the blacklist");
		waitForNotification();
		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);

		notifications = body.getTopNavBar().getNotifications();
		verifyNotifications(notificationContents);

		WebDriverWait wait = new WebDriverWait(getDriver(),15);

		notifications.notificationNumber(1).click();
		verifyThat(notifications, displayed());
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("piano keyboard pianoforte", "English");
		notificationContents.add("Created a new synonym group containing: keyboard, piano, pianoforte");
		waitForNotification();

		verifyNotifications(notificationContents);

		wait.until(ExpectedConditions.visibilityOf(notifications.notificationNumber(1))).click();
		verifyThat(notifications, displayed());
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);

		keywordsPage.loadOrFadeWait();
		keywordsPage.deleteSynonym("keyboard", "piano");
		notificationContents.add("Removed \"keyboard\" from a synonym group");
		waitForNotification();
		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);

		verifyNotifications(notificationContents);

		wait.until(ExpectedConditions.visibilityOf(notifications.notificationNumber(1))).click();
		verifyThat(notifications, displayed());
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);

		keywordsPage.filterView(KeywordFilter.BLACKLIST);
		keywordsPage.selectLanguage("English");
		keywordsPage.deleteBlacklistedTerm("orange");
		notificationContents.add("Removed \"orange\" from the blacklist");
		waitForNotification();
		if (config.getType().equals(ApplicationType.HOSTED)) {
			// TODO: belongs in a hosted notifications test
			body.getSideNavBar().switchPage(NavBarTabId.ANALYTICS);
			((HSOElementFactory) getElementFactory()).getAnalyticsPage();
		}

		verifyNotifications(notificationContents);

		wait.until(ExpectedConditions.visibilityOf(notifications.notificationNumber(1))).click();
		verifyThat(notifications, displayed());
	}

	private void waitForNotification() {
		new WebDriverWait(getDriver(), 30).until(GritterNotice.notificationAppears());
	}

	private void verifyNotifications(List<String> contents) {
		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();


		int size = Math.min(contents.size(), 5);
		for (int i=1; i <= size ; i++) {
			verifyThat(notifications.notificationNumber(i), containsText(contents.get(size-i)));
		}
	}

	// This only tests the notifications dropdown and not the gritters
	@Test
	public void testHTMLEscapedInNotifications() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("<h1>Hi</h1>", "English");
//		body.waitForGritterToClear();

		new WebDriverWait(getDriver(),60).until(ExpectedConditions.visibilityOf(getElementFactory().getKeywordsPage()));

		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);

		body.getTopNavBar().waitForGritterToClear();

		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();
		assertThat("Notification text incorrect, HTML not escaped", notifications.notificationNumber(1).getText(),
				not(containsString("Added \"hi\" to the blacklist")));
		assertThat("Notification text incorrect", notifications.notificationNumber(1).getText(),
				containsString("Added \"<h1>hi</h1>\" to the blacklist"));

		new WebDriverWait(getDriver(),5).until(ExpectedConditions.elementToBeClickable(notifications.notificationNumber(1))).click();
		assertThat(notifications, displayed());
	}

	@Test
	public void testKeywordsSearchFilter() throws InterruptedException {
		WebDriverWait wait = new WebDriverWait(getDriver(),15);

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("grizzly brownBear bigBear", "English");
		//All keywords should be changed by the application to lowercase in all instances
		final List<String> synonymListBears = Arrays.asList("grizzly", "brownbear", "bigbear");
		wait.until(ExpectedConditions.visibilityOf(getElementFactory().getSearchPage()));
		searchPage = getElementFactory().getSearchPage();

		for (final String synonym : synonymListBears) {
			assertThat(synonym + " not included in title", searchPage.title(), containsString(synonym));
			assertThat(synonym + " not included in 'You searched for' section", searchPage.youSearchedFor(),hasItem(synonym));
			verifyThat(synonym + " synonym group not complete in'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym),containsItems(synonymListBears));
			verifyThat(searchPage.countSynonymLists(), is(1));
		}

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("honeyBee bumbleBee buzzyBee", "English");
		final List<String> synonymListBees = Arrays.asList("honeybee", "bumblebee", "buzzybee");
		wait.until(ExpectedConditions.visibilityOf(getElementFactory().getSearchPage()));
		searchPage = getElementFactory().getSearchPage();

		for (final String synonym : synonymListBees) {
			assertThat(synonym + " not included in title", searchPage.title(),containsString(synonym));
			assertThat(synonym + " not included in 'You searched for' section", searchPage.youSearchedFor(),hasItem(synonym));
			assertThat(synonym + " not included in 'Keywords' section", searchPage.getSynonymGroupSynonyms(synonym),containsItems(synonymListBees));
			assertThat(searchPage.countSynonymLists(), is(1));
			assertThat(createKeywordsPage.countKeywords(), is(3));
		}

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.selectLanguage("English");
		keywordsPage.filterView(KeywordFilter.SYNONYMS);
		assertThat(keywordsPage.countSynonymLists(), is(2));

		for (final List<String> synonymList : Arrays.asList(synonymListBears, synonymListBees)) {
			for (final String synonym : synonymList) {
				assertThat("synonym not included in synonym group: " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym),containsItems(synonymList));
			}
		}

		keywordsPage.searchFilterTextBox().clear();
		keywordsPage.searchFilterTextBox().sendKeys("zz");
		assertThat(keywordsPage.countSynonymLists(), is(2));

		for (final List<String> synonymList : Arrays.asList(synonymListBears, synonymListBees)) {
			for (final String synonym : synonymList) {
				assertThat("synonym not included in synonym group: " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym),containsItems(synonymList));
			}
		}

		keywordsPage.searchFilterTextBox().clear();
		keywordsPage.searchFilterTextBox().sendKeys("buzz");
		keywordsPage.loadOrFadeWait();
		assertThat(keywordsPage.countSynonymLists(), is(1));
		assertThat(keywordsPage.countKeywords(), is(3));

		for (final String synonym : synonymListBees) {
			assertThat("synonym not included in synonym group: " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym),containsItems(synonymListBees));
			assertThat("synonym included in synonym group: " + synonym + " that should not be there", keywordsPage.getSynonymGroupSynonyms(synonym),not(containsItems(synonymListBears)));
		}

		keywordsPage.searchFilterTextBox().clear();
		keywordsPage.searchFilterTextBox().sendKeys("a");
		keywordsPage.searchFilterTextBox().sendKeys(Keys.BACK_SPACE);
		keywordsPage.loadOrFadeWait();
		assertThat(keywordsPage.countSynonymLists(), is(2));

		for (final List<String> synonymList : Arrays.asList(synonymListBears, synonymListBees)) {
			for (final String synonym : synonymList) {
				assertThat("synonym not included in synonym group: " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym),containsItems(synonymList));
			}
		}

		keywordsPage.searchFilterTextBox().clear();
		keywordsPage.searchFilterTextBox().sendKeys("Bear");
		keywordsPage.loadOrFadeWait();
		assertThat(keywordsPage.countSynonymLists(), is(1));      //Fails because of capital letter
		assertThat(keywordsPage.countKeywords(), is(3));

		for (final String synonym : synonymListBears) {
			assertThat("synonym not included in synonym group: " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym),containsItems(synonymListBears));
			assertThat("synonym included in synonym group: " + synonym + " that should not be there", keywordsPage.getSynonymGroupSynonyms(synonym),not(containsItems(synonymListBees)));
		}

		keywordsPage.searchFilterTextBox().clear();
		keywordsPage.searchFilterTextBox().sendKeys("a");
		keywordsPage.searchFilterTextBox().sendKeys(Keys.BACK_SPACE);
		keywordsPage.loadOrFadeWait();
		assertThat(keywordsPage.countSynonymLists(), is(2));
		assertThat(keywordsPage.countKeywords(), is(6));

		for (final List<String> synonymList : Arrays.asList(synonymListBears, synonymListBees)) {
			for (final String synonym : synonymList) {
				assertThat("synonym not included in synonym group: " + synonym, keywordsPage.getSynonymGroupSynonyms(synonym),containsItems(synonymList));
			}
		}
	}

	private Matcher<Iterable<String>> containsItems(List<String> list) {
		return hasItems(list.toArray(new String[list.size()]));
	}

	@Test
	public void testOnlyLanguagesWithDocumentsAvailableOnSearchPage() {
		assumeThat("Lanugage not implemented in Hosted", getConfig().getType(), not(ApplicationType.HOSTED));

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("Baku", "Azeri");

		body.getTopNavBar().search("Baku");
		searchPage = getElementFactory().getSearchPage();
		assertThat(searchPage.getLanguageList(), not(hasItem("Azeri")));
	}

	@Test
	public void testKeywordsLanguage() {
		assumeThat("Lanugage not implemented in Hosted", getConfig().getType(), not(ApplicationType.HOSTED));

		WebDriverWait wait = new WebDriverWait(getDriver(),30);

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("Atlanta", "Georgian");

		wait.until(ExpectedConditions.visibilityOf(getElementFactory().getKeywordsPage()));

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("Tirana", "Albanian");

		wait.until(ExpectedConditions.visibilityOf(getElementFactory().getKeywordsPage()));

		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("Croatia Kroatia Hrvatska", "Croatian");

		if(getConfig().getType().equals(ApplicationType.ON_PREM)){
			wait.until(ExpectedConditions.visibilityOf(getElementFactory().getKeywordsPage()));
		} else {
			wait.until(ExpectedConditions.visibilityOf(getElementFactory().getSearchPage()));
			body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		}

		keywordsPage.filterView(KeywordFilter.ALL);
		keywordsPage.selectLanguage("Georgian");
		assertThat(keywordsPage.getBlacklistedTerms().size(), is(1));
		assertThat(keywordsPage.countSynonymLists(), is(0));

		keywordsPage.selectLanguage("Albanian");
		assertThat(keywordsPage.getBlacklistedTerms().size(), is(1));
		assertThat(keywordsPage.countSynonymLists(), is(0));

		keywordsPage.selectLanguage("Croatian");
		assertThat(keywordsPage.getBlacklistedTerms().size(), is(0));
		assertThat(keywordsPage.countSynonymLists(), is(1));
		assertThat(keywordsPage.countKeywords(), is(3));
	}

	@Test
	public void testLanguageOfSearchPageKeywords() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("road rue strasse", "French");
		searchPage = getElementFactory().getSearchPage();
		body.getTopNavBar().search("Korea");
		searchPage.selectLanguage("Chinese");
		searchPage.waitForSearchLoadIndicatorToDisappear();
		searchPage.createSynonymsLink().click();
		searchPage.loadOrFadeWait();
		assertThat(getDriver().getCurrentUrl(), containsString("keywords/create"));
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.addSynonyms("한국");
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(createKeywordsPage.enabledFinishWizardButton())).click();
		searchPage = getElementFactory().getSearchPage();

		body.getTopNavBar().search("Korea");
		searchPage.selectLanguage("Chinese");
		verifyThat(searchPage.countSynonymLists(), is(1));

		searchPage.selectLanguage("French");
		verifyThat(searchPage.countSynonymLists(), is(1));

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.filterView(KeywordFilter.ALL);

		keywordsPage.selectLanguage("French");
		verifyThat("synonym not assigned to wrong language", keywordsPage, not(containsText("한국")));

		keywordsPage.selectLanguage("Chinese");
		verifyThat(keywordsPage.countSynonymLists(), is(1));
		verifyThat("synonym assigned to correct language", keywordsPage, containsText("한국"));
	}

	@Test
	public void testKeywordsCreationAndDeletionOnSecondWindow() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("double duo two pair couple", "Urdu");
		new WebDriverWait(getDriver(),30).until(ExpectedConditions.visibilityOf(getElementFactory().getSearchPage()));
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.filterView(KeywordFilter.SYNONYMS);

		keywordsPage.selectLanguage("Urdu");

		keywordsPage.loadOrFadeWait();

		final String url = getDriver().getCurrentUrl();
		final List<String> browserHandles = keywordsPage.createAndListWindowHandles();

		getDriver().switchTo().window(browserHandles.get(1));
		getDriver().get(url);
		final KeywordsPage secondKeywordsPage = getElementFactory().getKeywordsPage();
		assertThat(secondKeywordsPage.countSynonymLists(), is(1));
		assertThat(secondKeywordsPage.countKeywords(), is(5));

		getDriver().switchTo().window(browserHandles.get(0));
		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.loadOrFadeWait();
		keywordsPage.deleteSynonym("couple", "two");

		getDriver().switchTo().window(browserHandles.get(1));
		assertThat(secondKeywordsPage.countSynonymLists(), is(1));
		assertThat(secondKeywordsPage.countKeywords(), is(4));

		getDriver().switchTo().window(browserHandles.get(0));
		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.loadOrFadeWait();
		keywordsPage.deleteSynonym("pair", "duo");

		getDriver().switchTo().window(browserHandles.get(1));
		assertThat(secondKeywordsPage.countSynonymLists(), is(1));
		assertThat(secondKeywordsPage.countKeywords(), is(3));
	}

	@Test
	public void testSynonymsNotCaseSensitive() {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.keywordsType(CreateNewKeywordsPage.KeywordType.SYNONYM).click();

		createKeywordsPage.selectLanguage("English");

		createKeywordsPage.continueWizardButton(	).click();
		createKeywordsPage.loadOrFadeWait();

		createKeywordsPage.addSynonyms("bear");
		assertThat(createKeywordsPage.countKeywords(), is(1));

		for (final String bearVariant : Arrays.asList("Bear", "beaR", "BEAR", "beAR", "BEar")) {
			createKeywordsPage.addSynonyms(bearVariant);
			assertThat(createKeywordsPage.countKeywords(), is(1));
			assertThat("bear not included as a keyword", createKeywordsPage.getProspectiveKeywordsList(),hasItem("bear"));
			assertThat("correct error message not showing", createKeywordsPage.getText(), containsString(bearVariant.toLowerCase() + " is a duplicate of an existing keyword."));

		}

		// disallows any adding of synonyms if disallowed synonym found
		createKeywordsPage.addSynonyms("Polar Bear");
		assertThat(createKeywordsPage.countKeywords(), is(1));
		assertThat("bear not included as a keyword", createKeywordsPage.getProspectiveKeywordsList(), hasItem("bear"));
		assertThat("correct error message not showing", createKeywordsPage.getText(), containsString("bear is a duplicate of an existing keyword."));

		//jam and jaM are case variants so none should be added
		createKeywordsPage.addSynonyms("jam jaM");
		assertThat(createKeywordsPage.countKeywords(), is(1));
	}


	@Test
	public void testSpinnerPresentOnLastSynonymWhilePenultimateSynonymSpinnerPresent() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("ying yang", "Korean");

		//keywordsPage.selectLanguage("Korean");
		LOGGER.warn("Cannot select language for blacklists yet");

		new WebDriverWait(getDriver(),40).until(ExpectedConditions.visibilityOf(getElementFactory().getSearchPage()));

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.filterView(KeywordFilter.SYNONYMS);
		assertThat(keywordsPage.countSynonymLists(), is(1));
		assertThat(keywordsPage.countKeywords(), is(2));

		keywordsPage.getSynonymIcon("ying", "yang").click();
		if (keywordsPage.getSynonymIcon("ying", "yang").getAttribute("class").contains("fa-spin")) {
			assertThat("Spinner not present on last synonym", keywordsPage.getSynonymIcon("yang", "yang").getAttribute("class"),containsString("fa-spin"));
		}
	}

	@Test
	public void testAddKeywordsBoxOpenClickDelete() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("бір екі үш төрт бес", "Kazakh");

		new WebDriverWait(getDriver(),40).until(ExpectedConditions.visibilityOf(getElementFactory().getSearchPage()));

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.filterView(KeywordFilter.SYNONYMS);

		keywordsPage.selectLanguage("Kazakh");

		keywordsPage.synonymGroupPlusButton("бір").click();
		assertThat(keywordsPage.synonymGroupTextBox("бір"), displayed());

		keywordsPage.deleteSynonym("екі", "үш");
		assertThat(keywordsPage.synonymGroupTextBox("бір"), displayed());
	}

	@Test
	//CCUK-3243
	public void testQuickSynonymDelete() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("string strong strang streng strung", "German");
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.filterView(KeywordFilter.SYNONYMS);
		keywordsPage.loadOrFadeWait();

		keywordsPage.selectLanguage("German");

		try {
			keywordsPage.scrollIntoViewAndClick(keywordsPage.getSynonymIcon("strong", "strung"));
			keywordsPage.scrollIntoViewAndClick(keywordsPage.getSynonymIcon("string", "strung"));
		} catch (final WebDriverException w) {
			throw new AssertionError("Unable to delete a synonym quickly", w);
		}
		Thread.sleep(15000);
		assertThat("Incorrect number of synonyms", keywordsPage.countSynonymLists(), is(1));
		assertThat(keywordsPage.countKeywords(), is(3));
	}

	@Test
	//CCUK-3245
	public void testAddingForbiddenKeywordsFromUrl() {
		String blacklistUrl = getConfig().getWebappUrl() + "/p/keywords/create/blacklisted/English/";
		String synonymsUrl = getConfig().getWebappUrl() + "/p/keywords/create/synonyms/English/";
		if (getConfig().getType().equals(ApplicationType.ON_PREM)) {
			blacklistUrl = getConfig().getWebappUrl() + "keywords/create/blacklisted/englishUTF8/";
			synonymsUrl = getConfig().getWebappUrl() + "keywords/create/synonyms/englishUTF8/";
		}
		for (final String forbidden : Arrays.asList("(", "\"", "OR")) {
			getDriver().get(blacklistUrl + forbidden);
			keywordsPage.loadOrFadeWait();
			createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
			assertThat(forbidden + " is a forbidden keyword and should not be included in the prospective blacklist list", createKeywordsPage.getProspectiveKeywordsList(),not(hasItem("(")));

			getDriver().get(synonymsUrl + forbidden);
			keywordsPage.loadOrFadeWait();
			createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
			assertThat(forbidden + " is a forbidden keyword and should not be included in the prospective synonyms list", createKeywordsPage.getProspectiveKeywordsList(),not(hasItem("(")));
		}
	}

	@Test
	public void testSynonymsDisplayedInAlphabeticalOrder() throws InterruptedException {
		for (final String synonyms : Arrays.asList("aa ba ca da", "ab bb cb db", "dc cc bc ac", "ca ba da aa")) {
			keywordsPage.createNewKeywordsButton().click();
			createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
			createKeywordsPage.createSynonymGroup(synonyms, "English");

			searchPage = getElementFactory().getSearchPage();
			searchPage.waitForSearchLoadIndicatorToDisappear();
			final List<String> keywords = searchPage.getLeadSynonymsList();

			for (int i = 0; i < keywords.size() - 1; i++) {
				assertThat(keywords.get(i).compareTo(keywords.get(i + 1)) <= 0, is(true));
			}
			searchPage.loadOrFadeWait();
			body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		}

		keywordsPage.loadOrFadeWait();
		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.filterView(KeywordFilter.SYNONYMS);
		keywordsPage.searchFilterTextBox().sendKeys("cc");
		final List<String> keywords = keywordsPage.getLeadSynonymsList();

		for (int i = 0; i < keywords.size() - 1; i++) {
			assertThat(keywords.get(i).compareTo(keywords.get(i + 1)) <= 0, is(true));
		}
	}

	@Test
	public void testBlacklistedKeywordsDisplayedInAlphabeticalOrder() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createBlacklistedTerm("aa ba ca da ab bb cb db", "English");

		getElementFactory().getKeywordsPage();

		keywordsPage.filterView(KeywordFilter.BLACKLIST);

		Thread.sleep(10000);    //TODO Need to find a better way to do this -- need to wait for all blacklist terms to be added

		final List<String> keywords = keywordsPage.getBlacklistedTerms();
		assertThat("Wrong number of blacklist items created", keywords.size(), is(8));

		for (int i = 0; i < keywords.size() - 1; i++) {
			assertThat(keywords.get(i).compareTo(keywords.get(i + 1)) <= 0, is(true));
		}
	}

	@Test
	public void testDeletingOfSynonymsAndBlacklistedTerms() throws InterruptedException {
		String blacklistTerms = "aa ba ca da ab bb cb db";
		String synonyms = "ea es ed ef eg eh";
		String[] blacklistTermsToDelete = {"db", "aa", "da"};
		String[] synonymsToDelete = {"es", "ea", "ef", "ed", "eg"};

		keywordService.addBlacklistTerms(blacklistTerms);
		keywordService.addSynonymGroup(synonyms);

		verifyDeletes(synonymsToDelete);

		keywordService.deleteAll(KeywordFilter.ALL);
		keywordService.addBlacklistTerms(blacklistTerms);
		keywordService.addSynonymGroup(synonyms);

		verifyDeletes(blacklistTermsToDelete);
	}

	private void verifyDeletes(String[] keywords) {
		for (String keyword : keywords) {
			keywordService.deleteKeyword(keyword);
			verifyThat("successfully removed keyword '" + keyword + "'", !keywordsPage.areAnyKeywordsDisabled());
		}
	}

	@Test
	public void testDoesDeletingSynonymDisableOtherSynonyms() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createKeywordsPage.createSynonymGroup("ea es ed ef eg eh", "English");
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.filterView(KeywordFilter.ALL);

		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();
		//Wait for the synonyms to be added
		new WebDriverWait(getDriver(),45).until(GritterNotice.notificationContaining("Created a new synonym group containing: "));

		final List<String> synonyms = Arrays.asList("ea", "es", "ed", "ef", "eg");
		for (final String synonym : synonyms) {
			keywordsPage.getSynonymIcon(synonym, synonym).click();

			if(getParent(getParent(getParent(keywordsPage.getSynonymIcon(synonym)))).findElements(By.tagName("li")).size() > 2) {
				assertThat("Too many synonyms are disabled on synonym delete", keywordsPage.countRefreshIcons(), is(1));
			} else {
				assertThat(keywordsPage.countRefreshIcons(), is(2));
			}

			//Wait for deletion to complete
			new WebDriverWait(getDriver(),30).until(new ExpectedCondition<Boolean>() {
				@Override
				public Boolean apply(WebDriver driver) {
					return keywordsPage.countRefreshIcons() == 0;
				}
			});

			assertThat("some keywords are disabled after the last keyword delete", keywordsPage.areAnyKeywordsDisabled(), is(false));
		}
	}

	@Test
	public void testSynonymNotificationText() throws InterruptedException {
		String synonymOne = "Flesh";
		String synonymTwo = "Meat";
		String synonymThree = "Skin";

		String[] synonyms = new String[]{synonymOne, synonymTwo, synonymThree};
		keywordsPage.createNewKeywordsButton().click();
		CreateNewKeywordsPage createNewKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createNewKeywordsPage.createSynonymGroup(String.join(" ", synonyms), "English");
		Arrays.sort(synonyms);
		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();
		new WebDriverWait(getDriver(),30).until(GritterNotice.notificationContaining("Created a new synonym group containing: "));
		assertThat(notifications.notificationNumber(1).getText(), is("Created a new synonym group containing: " + synonyms[0].toLowerCase() + ", " + synonyms[1].toLowerCase() + ", " + synonyms[2].toLowerCase()));
	}

	@Test
	public void testBlacklistNotificationText() throws InterruptedException {
		String blacklistOne = "Aardvark";
		String blacklistTwo = "Aardwolf";

		keywordsPage.createNewKeywordsButton().click();
		CreateNewKeywordsPage createNewKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createNewKeywordsPage.createBlacklistedTerm(blacklistOne + " " + blacklistTwo, "English");
		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();
		new WebDriverWait(getDriver(),30).until(GritterNotice.notificationContaining("blacklist"));
		assertThat(notifications.notificationNumber(1).getText(), anyOf(is("Added \"" + blacklistOne.toLowerCase() + "\" to the blacklist"), is("Added \"" + blacklistTwo.toLowerCase() + "\" to the blacklist")));
		assertThat(notifications.notificationNumber(2).getText(), anyOf(is("Added \"" + blacklistOne.toLowerCase() + "\" to the blacklist"), is("Added \"" + blacklistTwo.toLowerCase() + "\" to the blacklist")));
		assertThat(notifications.notificationNumber(1).getText(), not(notifications.notificationNumber(2).getText()));
	}

	@Test
	public void testClickingOnNotifications() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		CreateNewKeywordsPage createNewKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createNewKeywordsPage.createSynonymGroup("a b c d", "English");
		getElementFactory().getSearchPage();
		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
		getElementFactory().getPromotionsPage();
		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();

		verifyThat(notifications.notificationNumber(1).getText(), is("Created a new synonym group containing: a, b, c, d"));

		notifications.notificationNumber(1).click();

		verifyThat(notifications, displayed());

		if(!getDriver().getCurrentUrl().contains("keywords")){
			body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		}

		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.createNewKeywordsButton().click();
		createNewKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createNewKeywordsPage.createBlacklistedTerm("e", "English");
		getElementFactory().getKeywordsPage();

		// TODO: this is a shared test, does not belong
		if(getConfig().getType().equals(ApplicationType.HOSTED)) {
			body.getSideNavBar().switchPage(NavBarTabId.ANALYTICS);

			((HSOElementFactory) getElementFactory()).getAnalyticsPage();
			body = getBody();
		} else {
			body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
		}

		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();

		verifyThat(notifications.notificationNumber(1).getText(), is("Added \"e\" to the blacklist"));

		notifications.notificationNumber(1).click();

		verifyThat(notifications, displayed());

	}

	/**
	 * The assumed behaviour for where a blacklisted item is also within the synonym group is as follows:
	 *
	 * 		- Searching for the blacklisted term will bring up no results
	 * 		- Searching for the other terms within the synonym group will bring up results for ALL terms within the synonym group INCLUDING the blacklisted term
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void testOverlappingBlacklistSynonym() throws InterruptedException {
		String blacklist = "blacklist";
		String synonym = "synonym";
		String[] synonymGroup = new String[]{blacklist, synonym};

		keywordsPage.createNewKeywordsButton().click();
		CreateNewKeywordsPage createNewKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createNewKeywordsPage.createSynonymGroup(String.join(" ", synonymGroup), "English");
		getElementFactory().getSearchPage();
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.createNewKeywordsButton().click();
		createNewKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
		createNewKeywordsPage.createBlacklistedTerm(blacklist,"English");
		getElementFactory().getKeywordsPage();

		body.getTopNavBar().search(blacklist);

		searchPage = getElementFactory().getSearchPage();

		//Make sure no results show up for blacklisted terms
		assertThat(searchPage.visibleDocumentsCount(),is(0));

		body.getTopNavBar().search(synonym);

		searchPage = getElementFactory().getSearchPage();

		//Make sure some results show up for terms within the synonym group
		assertThat(searchPage.visibleDocumentsCount(), not(0));
	}

	@Test
	//CSA-1440
	public void testNavigatingAwayBeforeKeywordAdded() throws InterruptedException {
		keywordsPage.createNewKeywordsButton().click();
		getElementFactory().getCreateNewKeywordsPage().createBlacklistedTerm("Jeff", "English");
		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);

		new WebDriverWait(getDriver(),30).until(GritterNotice.notificationContaining("blacklist"));

		assertThat(getDriver().getCurrentUrl(), containsString("promotions"));
	}


	private void verifyBlacklisted(String blacklist) {
		verifyThat(blacklist + " is blacklisted", keywordsPage.getBlacklistedTerms(), hasItem(blacklist));
	}

	private void verifySynonymGroup(List<String> synonymGroup) {
		verifyThat(synonymGroup, everyItem(isIn(keywordsPage.getSynonymGroupSynonyms(synonymGroup.get(0)))));
	}

	private void verifyNoBlacklist() {
		verifyThat("no blacklist terms displayed", keywordsPage.getBlacklistedTerms(), empty());
	}

	private void verifyNoSynonyms() {
		verifyThat("no synonyms displayed", keywordsPage.countSynonymLists(), is(0));
	}

	private void verifyNumberOfSynonymGroups(int count) {
		verifyThat("number of synonym groups is " + count, keywordsPage.countSynonymLists(), is(count));
	}

	private void verifySynonymGroupSize(List<String> synonyms) {
		verifyThat(keywordsPage.getSynonymGroupSynonyms(synonyms.get(0)), hasSize(synonyms.size()));
	}

	private List<String> deleteSynonymAndVerify(List<String> synonyms, int index) {
		if (synonyms.size() == 2) {
			deleteSynonymGroupAndVerify(synonyms.get(index));
			return Collections.emptyList();
		}
		synonyms = new ArrayList<>(synonyms);
		String deleted = synonyms.remove(index);
		String remaining = synonyms.get(index == 0 ? 1 : 0);
		keywordService.deleteKeyword(deleted);
		verifySynonymGroup(synonyms);
		verifyThat(deleted + " is no longer in synonym group", keywordsPage.getSynonymGroupSynonyms(remaining), not(hasItem(deleted)));
		verifySynonymGroupSize(synonyms);
		return synonyms;
	}

	private void deleteSynonymGroupAndVerify(String synonym) {
		int expectedGroups = keywordsPage.countSynonymLists() - 1;
		int expectedKeywords = keywordsPage.countKeywords() - keywordsPage.getSynonymGroupSynonyms(synonym).size();
		keywordService.removeKeywordGroup(keywordsPage.synonymGroup(synonym));
		verifyNumberOfSynonymGroups(expectedGroups);
		verifyThat("number of keywords is " + expectedKeywords, keywordsPage.countKeywords(), is(expectedKeywords));
		verifyThat("no refresh icons", keywordsPage.countRefreshIcons(), is(0));
	}

}