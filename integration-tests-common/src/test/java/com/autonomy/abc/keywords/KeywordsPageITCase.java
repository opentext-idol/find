package com.autonomy.abc.keywords;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.keywords.KeywordService;
import com.autonomy.abc.selenium.keywords.KeywordWizardType;
import com.autonomy.abc.selenium.menu.NavBarTabId;
import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.keywords.CreateNewKeywordsPage;
import com.autonomy.abc.selenium.keywords.KeywordFilter;
import com.autonomy.abc.selenium.page.keywords.KeywordsPage;
import com.autonomy.abc.selenium.page.keywords.SynonymGroup;
import com.autonomy.abc.selenium.page.search.SearchPage;
import com.autonomy.abc.selenium.search.SearchActionFactory;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.util.DriverUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.*;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.CommonMatchers.*;
import static com.autonomy.abc.matchers.ElementMatchers.containsText;
import static com.autonomy.abc.matchers.ElementMatchers.hasTextThat;
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
		List<String> synonyms = Arrays.asList("dog", "hound", "canine");
		String blacklist = "illegal";

		searchPage = keywordService.addSynonymGroup(synonyms);
		for (String synonym : synonyms) {
			verifyThat("search title contains " + synonym, searchPage.getHeadingSearchTerm(), containsString(synonym));
		}
		
		keywordService.addBlacklistTerms(blacklist);
		keywordsPage.filterView(KeywordFilter.ALL);
		keywordsPage.selectLanguage(Language.ENGLISH);
		verifySynonymGroup(synonyms);
		verifyBlacklisted(blacklist);

		keywordsPage.filterView(KeywordFilter.SYNONYMS);
		verifySynonymGroup(synonyms);
		verifyNoBlacklist();
		verifyThat("synonym list on row 2 is not visible", keywordsPage.synonymList(1), not(displayed()));

		keywordsPage.filterView(KeywordFilter.BLACKLIST);
		verifyNoSynonyms();
		verifyBlacklisted(blacklist);

		keywordsPage.filterView(KeywordFilter.ALL);
		verifySynonymGroup(synonyms);
		verifyBlacklisted(blacklist);
		verifyThat("A synonym list should be visible on row 2", keywordsPage.synonymList(1), displayed());
	}

	@Test
	public void testDeleteKeywords() throws InterruptedException {
		List<String> synonyms = Arrays.asList("frog", "toad", "amphibian", "tadpole");
		addSynonymsAndVerify(synonyms);
		verifyNumberOfSynonymGroups(1);

		synonyms = deleteSynonymAndVerify("frog", synonyms);
		synonyms = deleteSynonymAndVerify("tadpole", synonyms);

		deleteSynonymAndVerify("toad", synonyms);
		verifyNoSynonyms();
	}

	//The keyword 'orange' exists in two different synonym groups. Tests that deleting this keyword does not affect the other synonym group
	@Test
	public void testDeleteSynonymsFromOverlappingSynonymGroups() throws InterruptedException {
		String duplicate = "orange";
		List<String> fruits = Arrays.asList("apple", "pear", "banana", duplicate);
		List<String> colours = Arrays.asList("red", "blue", "yellow", duplicate);

		keywordService.addSynonymGroup(fruits);
		keywordService.addSynonymGroup(colours);

		keywordService.goToKeywords();
		keywordsPage.filterView(KeywordFilter.SYNONYMS);

		verifySynonymGroup(fruits);
		verifySynonymGroup(colours);
		verifyKeywordState(2, 8);
		verifyDuplicateCount(duplicate, 2);

		fruits = deleteSynonymAndVerify("apple", fruits);
		verifySynonymGroup(colours);
		verifyDuplicateCount(duplicate, 2);

		colours = deleteSynonymAndVerify("red", colours);
		verifySynonymGroup(fruits);
		verifyDuplicateCount(duplicate, 2);

		colours = deleteSynonymAndVerify(duplicate, colours);
		verifySynonymGroup(fruits);
		verifyDuplicateCount(duplicate, 1);

		fruits = deleteSynonymAndVerify("banana", fruits);
		verifyDuplicateCount(duplicate, 1);

		deleteSynonymAndVerify("yellow", colours);
		verifyDuplicateCount(duplicate, 1);

		deleteSynonymAndVerify(duplicate, fruits);
		verifyNoSynonyms();
	}

	private void verifyDuplicateCount(String duplicate, int count) {
		verifyThat(duplicate + " appears " + count + " times", keywordsPage.countSynonymGroupsWithSynonym(duplicate), is(count));
	}

	@Ignore
	// This takes too long for a nightly test but is a useful test that need run periodically as the application has failed in the past with a large number of synonym groups.
	// Failure can present itself on other pages other than the KeywordsPage
	@Test
	public void testAddLotsOfSynonymGroups() {
		final List<String> groupsOfFiveSynonyms = readSynonymFile();

		int expectedGroups = 0;
		for (final String synonymGroup : groupsOfFiveSynonyms) {
			keywordService.addSynonymGroup(synonymGroup);
			expectedGroups++;
			keywordService.goToKeywords();
			verifyKeywordState(expectedGroups, 5*expectedGroups);
		}
	}

	// TODO: this still does not work
	private List<String> readSynonymFile() {
		final List<String> groupsOfFiveSynonyms = new ArrayList<>();
		Scanner scanner = new Scanner(ClassLoader.getSystemResource("/100SynonymGroups.txt").getFile());
		while (scanner.hasNextLine()) {
			groupsOfFiveSynonyms.add(scanner.nextLine());
		}
		return groupsOfFiveSynonyms;
	}

	//Whitespace, Odd number of quotes or quotes with blank text, boolean operators or proximity operators should not be able to added as keywords. This test checks they can't be added to existing synonyms on the Keywords Page
	@Test
	public void testAddingWhitespaceQuotesBooleansProximityOperatorsOnKeywordsPage() throws InterruptedException {
		List<String> synonyms = Arrays.asList("one", "two", "three");

		keywordService.addSynonymGroup(synonyms);
		keywordService.goToKeywords();
		keywordsPage.filterView(KeywordFilter.SYNONYMS);

		synonyms = verifyAddToGroup("four", synonyms);

		for (final String badSynonym : Arrays.asList("", " ", "\t", "\"")) {
			verifyCannotAddToGroup(badSynonym, synonyms);
			// close input box
			keywordsPage.searchFilterTextBox().click();
		}

		for(final String operatorSynonym : Arrays.asList("NOT", "NEAR", "DNEAR", "XNEAR", "YNEAR", "NEAR123", "SENTENCE2", "PARAGRAPH3", "AND", "BEFORE", "AFTER", "WHEN", "SENTENCE", "PARAGRAPH", "OR", "WNEAR", "EOR", "NOTWHEN")){
			synonyms = verifyAddToGroup(operatorSynonym, synonyms);
			synonyms = deleteSingleSynonymAndVerify(operatorSynonym, synonyms);
		}
	}

	//Phrases should be able to be added as synonyms from the keywords page
	@Test
	public void testPhrasesCanBeAddedAsSynonymsOnKeywordsPage() throws InterruptedException {
		searchPage = keywordService.addSynonymGroup(Language.ENGLISH, "one two three");
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(keywordsPage.createNewKeywordsButton()));
		keywordsPage.filterView(KeywordFilter.SYNONYMS);
		keywordsPage.selectLanguage(Language.ENGLISH);
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

		keywordService.addBlacklistTerms("orange");
		notificationContents.add("Added \"orange\" to the blacklist");

		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
		verifyNotifications(notificationContents);

		keywordService.addSynonymGroup("piano keyboard pianoforte");
		notificationContents.add("Created a new synonym group containing: keyboard, piano, pianoforte");

		verifyNotifications(notificationContents);

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.loadOrFadeWait();
		keywordsPage.deleteSynonym("keyboard");
		notificationContents.add("Removed \"keyboard\" from a synonym group");

		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
		verifyNotifications(notificationContents);

		body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		keywordsPage.filterView(KeywordFilter.BLACKLIST);
		keywordsPage.selectLanguage(Language.ENGLISH);
		keywordsPage.deleteBlacklistedTerm("orange");
		notificationContents.add("Removed \"orange\" from the blacklist");

		if (config.getType().equals(ApplicationType.HOSTED)) {
			// TODO: belongs in a hosted notifications test
			body.getSideNavBar().switchPage(NavBarTabId.ANALYTICS);
			((HSOElementFactory) getElementFactory()).getAnalyticsPage();
		}
		verifyNotifications(notificationContents);
	}

	private void verifyNotifications(List<String> contents) {
		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();

		int size = Math.min(contents.size(), 5);
		for (int i=1; i <= size ; i++) {
			verifyThat(notifications.notificationNumber(i), containsText(contents.get(size-i)));
		}
		verifyClickNotification();
	}

	private void verifyClickNotification() {
		WebDriverWait wait = new WebDriverWait(getDriver(),15);
		wait.until(GritterNotice.notificationsDisappear());
		wait.until(ExpectedConditions.visibilityOf(notifications.notificationNumber(1))).click();
		verifyThat("clicking notification does nothing", notifications, displayed());
	}

	// This only tests the notifications dropdown and not the gritters
	@Test
	public void testHTMLEscapedInNotifications() throws InterruptedException {
		keywordService.addBlacklistTerms("<h1>Hi</h1>");

		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);

		body.getTopNavBar().waitForGritterToClear();

		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();
		assertThat("Notification text incorrect, HTML not escaped", notifications.notificationNumber(1).getText(),
				not(containsString("Added \"hi\" to the blacklist")));
		assertThat("Notification text incorrect", notifications.notificationNumber(1).getText(),
				containsString("Added \"<h1>hi</h1>\" to the blacklist"));

		verifyClickNotification();
	}

	@Test
	public void testKeywordsSearchFilter() throws InterruptedException {
		final List<String> synonymListBears = Arrays.asList("grizzly", "brownbear", "bigbear");

		//All keywords should be changed by the application to lowercase in all instances
		searchPage = keywordService.addSynonymGroup("grizzly brownBear bigBear");
		verifySearchKeywords(synonymListBears);
		verifyThat(searchPage.countSynonymLists(), is(1));

		final List<String> synonymListBees = Arrays.asList("honeybee", "bumblebee", "buzzybee");
		searchPage = keywordService.addSynonymGroup("honeyBee bumbleBee buzzyBee");
		verifySearchKeywords(synonymListBees);
		verifyThat(searchPage.countSynonymLists(), is(1));

		keywordService.goToKeywords();
		keywordsPage.selectLanguage(Language.ENGLISH);
		keywordsPage.filterView(KeywordFilter.SYNONYMS);
		FormInput filterBox = keywordsPage.searchFilterBox();

		verifyKeywordState(2, 6);
		verifySynonymGroup(synonymListBears);
		verifySynonymGroup(synonymListBees);

		filterBox.setValue("zz");
		verifyKeywordState(2, 6);
		verifySynonymGroup(synonymListBears);
		verifySynonymGroup(synonymListBees);

		filterBox.setValue("buzz");
		verifyKeywordState(1, 3);
		List<String> beeSynonyms = keywordsPage.getSynonymGroupSynonyms(synonymListBees.get(0));
		verifyThat(synonymListBees, everyItem(isIn(beeSynonyms)));
		verifyThat(synonymListBears, everyItem(not(isIn(beeSynonyms))));

		filterBox.clear();
		verifySynonymGroup(synonymListBears);
		verifySynonymGroup(synonymListBees);

		filterBox.setValue("Bear");
		verifyKeywordState(1, 3);
		List<String> bearSynonyms = keywordsPage.getSynonymGroupSynonyms(synonymListBears.get(0));
		verifyThat(synonymListBears, everyItem(isIn(bearSynonyms)));
		verifyThat(synonymListBees, everyItem(not(isIn(bearSynonyms))));

		filterBox.clear();
		verifyKeywordState(2, 6);
		verifySynonymGroup(synonymListBears);
		verifySynonymGroup(synonymListBees);
	}

	private void verifySearchKeywords(final List<String> synonyms) {
		for (final String synonym : synonyms) {
			verifyThat(synonym + " included in title", searchPage.title(), containsString(synonym));
			verifyThat(synonym + " included in 'You searched for' section", searchPage.youSearchedFor(), hasItem(synonym));
		}
		verifyThat("synonyms appear in query analysis", searchPage.getSynonymGroupSynonyms(synonyms.get(0)), containsItems(synonyms));
		verifyThat(searchPage.countKeywords(), is(synonyms.size()));
	}

	@Test
	public void testOnlyLanguagesWithDocumentsAvailableOnSearchPage() {
		assumeThat("Language not implemented in Hosted", getConfig().getType(), not(ApplicationType.HOSTED));

		keywordService.addBlacklistTerms(Language.AZERI, "Baku");

		body.getTopNavBar().search("Baku");
		searchPage = getElementFactory().getSearchPage();
		assertThat(searchPage.getLanguageList(), not(hasItem("Azeri")));
	}

	@Test
	public void testKeywordsLanguage() {
		assumeThat("Lanugage not implemented in Hosted", getConfig().getType(), not(ApplicationType.HOSTED));

		keywordService.addBlacklistTerms(Language.GEORGIAN, "Atlanta");
		keywordService.addBlacklistTerms(Language.ALBANIAN, "Tirana");
		keywordService.addSynonymGroup(Language.CROATIAN, "Croatia Kroatia Hrvatska");
		keywordService.goToKeywords();

		keywordsPage.filterView(KeywordFilter.ALL);
		keywordsPage.selectLanguage(Language.GEORGIAN);
		assertThat(keywordsPage.getBlacklistedTerms().size(), is(1));
		assertThat(keywordsPage.countSynonymLists(), is(0));

		keywordsPage.selectLanguage(Language.ALBANIAN);
		assertThat(keywordsPage.getBlacklistedTerms().size(), is(1));
		assertThat(keywordsPage.countSynonymLists(), is(0));

		keywordsPage.selectLanguage(Language.CROATIAN);
		assertThat(keywordsPage.getBlacklistedTerms().size(), is(0));
		assertThat(keywordsPage.countSynonymLists(), is(1));
		assertThat(keywordsPage.countKeywords(), is(3));
	}

	@Test
	public void testKeywordsCreationAndDeletionOnSecondWindow() throws InterruptedException {
		keywordService.addSynonymGroup(Language.URDU, "double duo two pair couple");
		keywordsPage = keywordService.goToKeywords();

		keywordsPage.filterView(KeywordFilter.SYNONYMS);
		keywordsPage.selectLanguage(Language.URDU);
		keywordsPage.loadOrFadeWait();

		final String url = getDriver().getCurrentUrl();
		final List<String> browserHandles = DriverUtil.createAndListWindowHandles(getDriver());

		getDriver().switchTo().window(browserHandles.get(1));
		getDriver().get(url);
		final KeywordsPage secondKeywordsPage = getElementFactory().getKeywordsPage();
		assertThat(secondKeywordsPage.countSynonymLists(), is(1));
		assertThat(secondKeywordsPage.countKeywords(), is(5));

		getDriver().switchTo().window(browserHandles.get(0));
		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.loadOrFadeWait();
		keywordsPage.deleteSynonym("couple");

		getDriver().switchTo().window(browserHandles.get(1));
		assertThat(secondKeywordsPage.countSynonymLists(), is(1));
		assertThat(secondKeywordsPage.countKeywords(), is(4));

		getDriver().switchTo().window(browserHandles.get(0));
		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.loadOrFadeWait();
		keywordsPage.deleteSynonym("pair");

		getDriver().switchTo().window(browserHandles.get(1));
		assertThat(secondKeywordsPage.countSynonymLists(), is(1));
		assertThat(secondKeywordsPage.countKeywords(), is(3));
	}

	@Test
	public void testSpinnerPresentOnLastSynonymWhilePenultimateSynonymSpinnerPresent() throws InterruptedException {
		keywordService.addSynonymGroup(Language.KOREAN, "ying yang");
		keywordsPage = keywordService.goToKeywords();

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
		keywordService.addSynonymGroup(Language.KAZAKH, "бір екі үш төрт бес");
		keywordService.goToKeywords();

		keywordsPage.filterView(KeywordFilter.SYNONYMS);
		keywordsPage.selectLanguage(Language.KAZAKH);

		keywordsPage.synonymGroupPlusButton("бір").click();
		assertThat(keywordsPage.synonymGroupTextBox("бір"), displayed());

		keywordsPage.deleteSynonym("екі");
		assertThat(keywordsPage.synonymGroupTextBox("бір"), displayed());
	}

	@Test
	//CCUK-3243
	public void testQuickSynonymDelete() throws InterruptedException {
		keywordService.addSynonymGroup(Language.GERMAN, "string strong strang streng strung");
		keywordService.goToKeywords();

		keywordsPage.filterView(KeywordFilter.SYNONYMS);
		keywordsPage.selectLanguage(Language.GERMAN);
		keywordsPage.loadOrFadeWait();

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
			searchPage = keywordService.addSynonymGroup(Language.ENGLISH, synonyms);
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
		keywordService.addBlacklistTerms(Language.ENGLISH, "aa ba ca da ab bb cb db");

		keywordsPage.filterView(KeywordFilter.BLACKLIST);

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
		final List<String> synonyms = Arrays.asList("ea", "es", "ed", "ef", "eg");

		keywordService.addSynonymGroup(synonyms);
		keywordService.goToKeywords();
		keywordsPage.filterView(KeywordFilter.ALL);

		// the last 2 synonyms should be removed together
		for (int i = 0; i < synonyms.size()-1; i++) {
			String synonym = synonyms.get(i);
			keywordsPage.getSynonymIcon(synonym, synonym).click();

			if(i < synonyms.size()-2) {
				assertThat("one refresh icon", keywordsPage.countRefreshIcons(), is(1));
			} else {
				assertThat("two refresh icons", keywordsPage.countRefreshIcons(), is(2));
			}

			keywordsPage.waitForRefreshIconToDisappear();
			assertThat("all keywords are enabled", keywordsPage.areAnyKeywordsDisabled(), is(false));
		}
		verifyNoSynonyms();
	}

	@Test
	public void testSynonymNotificationText() throws InterruptedException {
		String synonymOne = "Flesh";
		String synonymTwo = "Meat";
		String synonymThree = "Skin";

		String[] synonyms = new String[]{synonymOne, synonymTwo, synonymThree};
		keywordService.addSynonymGroup(synonyms);
		Arrays.sort(synonyms);
		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();
		assertThat(notifications.notificationNumber(1).getText(), is("Created a new synonym group containing: " + synonyms[0].toLowerCase() + ", " + synonyms[1].toLowerCase() + ", " + synonyms[2].toLowerCase()));
	}

	@Test
	public void testBlacklistNotificationText() throws InterruptedException {
		String blacklistOne = "Aardvark";
		String blacklistTwo = "Aardwolf";

		keywordService.addBlacklistTerms(blacklistOne, blacklistTwo);
		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();

		assertThat(notifications.notificationNumber(1).getText(), anyOf(is("Added \"" + blacklistOne.toLowerCase() + "\" to the blacklist"), is("Added \"" + blacklistTwo.toLowerCase() + "\" to the blacklist")));
		assertThat(notifications.notificationNumber(2).getText(), anyOf(is("Added \"" + blacklistOne.toLowerCase() + "\" to the blacklist"), is("Added \"" + blacklistTwo.toLowerCase() + "\" to the blacklist")));
		assertThat(notifications.notificationNumber(1).getText(), not(notifications.notificationNumber(2).getText()));
	}

	@Test
	public void testClickingOnNotifications() throws InterruptedException {
		keywordService.addSynonymGroup("a b c d");

		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);
		getElementFactory().getPromotionsPage();
		body.getTopNavBar().notificationsDropdown();
		notifications = body.getTopNavBar().getNotifications();
		verifyThat(notifications.notificationNumber(1), hasTextThat(startsWith("Created a new synonym group containing: ")));

		verifyClickNotification();
		if(!getDriver().getCurrentUrl().contains("keywords")){
			body.getSideNavBar().switchPage(NavBarTabId.KEYWORDS);
		}

		keywordService.addBlacklistTerms("e");

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
		verifyClickNotification();
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

		keywordService.addSynonymGroup(synonymGroup);
		keywordService.addBlacklistTerms(blacklist);

		body.getTopNavBar().search(blacklist);

		searchPage = getElementFactory().getSearchPage();

		//Make sure no results show up for blacklisted terms
		assertThat(searchPage.visibleDocumentsCount(), is(0));

		body.getTopNavBar().search(synonym);

		searchPage = getElementFactory().getSearchPage();

		//Make sure some results show up for terms within the synonym group
		assertThat(searchPage.visibleDocumentsCount(), not(0));
	}

	@Test
	//CSA-1440
	public void testNavigatingAwayBeforeKeywordAdded() throws InterruptedException {
		keywordService.addKeywords(KeywordWizardType.BLACKLIST, Language.ENGLISH, Collections.singletonList("Jeff"));

		body.getSideNavBar().switchPage(NavBarTabId.PROMOTIONS);

		new WebDriverWait(getDriver(),30).until(GritterNotice.notificationContaining("blacklist"));

		assertThat(getDriver().getCurrentUrl(), containsString("promotions"));
	}

	@Test
	//CSA-1686
	public void testBlacklistTermsNotOverwritten(){
		String blacklistOne = "uno";
		String blacklistTwo = "duo";

		keywordService.addBlacklistTerms(blacklistOne);

		verifyBlacklisted(blacklistOne);

		keywordService.addBlacklistTerms(blacklistTwo);

		verifyBlacklisted(blacklistTwo);
		verifyBlacklisted(blacklistOne);
	}

	private void verifyBlacklisted(String blacklist) {
		verifyThat("'" + blacklist + "' is blacklisted", keywordsPage.getBlacklistedTerms(), hasItem(blacklist));
	}

	private void verifySynonymGroup(List<String> synonymGroup) {
		verifyThat(keywordsPage.getSynonymGroupSynonyms(synonymGroup.get(0)), containsItems(synonymGroup, String.CASE_INSENSITIVE_ORDER));
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
		verifySynonymGroupSize(synonyms.get(0), synonyms.size());
	}

	private void verifySynonymGroupSize(String synonym, int size) {
		verifyThat(keywordsPage.getSynonymGroupSynonyms(synonym), hasSize(size));
	}

	private void addSynonymsAndVerify(List<String> synonyms) {
		keywordService.addSynonymGroup(synonyms);
		keywordService.goToKeywords();
		keywordsPage.filterView(KeywordFilter.ALL);
		verifySynonymGroup(synonyms);
		verifySynonymGroupSize(synonyms);
	}

	private List<String> deleteSynonymAndVerify(String toDelete, List<String> synonyms) {
		if (synonyms.size() == 2) {
			deleteSynonymGroupAndVerify(toDelete);
			return Collections.emptyList();
		}
		return deleteSingleSynonymAndVerify(toDelete, synonyms);
	}

	private List<String> deleteSingleSynonymAndVerify(String toDelete, List<String> synonyms) {
		synonyms = new ArrayList<>(synonyms);
		synonyms.remove(toDelete);
		int expectedGroups = keywordsPage.countSynonymLists();
		int expectedKeywords = keywordsPage.countKeywords() - 1;
		keywordsPage.deleteSynonym(toDelete, keywordsPage.synonymGroupContaining(synonyms.get(0)));

		verifySynonymGroup(synonyms);
		verifyThat("'" + toDelete + "' is no longer in synonym group", keywordsPage.getSynonymGroupSynonyms(synonyms.get(0)), not(hasItem(equalToIgnoringCase(toDelete))));
		verifySynonymGroupSize(synonyms);
		verifyKeywordState(expectedGroups, expectedKeywords);
		return synonyms;
	}

	private void deleteSynonymGroupAndVerify(String synonym) {
		int expectedGroups = keywordsPage.countSynonymLists() - 1;
		int expectedKeywords = keywordsPage.countKeywords() - keywordsPage.getSynonymGroupSynonyms(synonym).size();

		keywordService.removeKeywordGroup(keywordsPage.synonymGroup(synonym));
		verifyKeywordState(expectedGroups, expectedKeywords);
	}

	private void verifyKeywordState(int expectedGroups, int expectedKeywords) {
		verifyNumberOfSynonymGroups(expectedGroups);
		verifyThat("number of keywords is " + expectedKeywords, keywordsPage.countKeywords(), is(expectedKeywords));
	}

	private List<String> verifyAddToGroup(String toAdd, List<String> synonyms) {
		synonyms = new ArrayList<>(synonyms);
		synonyms.add(toAdd);
		int expectedGroups = keywordsPage.countSynonymLists();
		int expectedKeywords = keywordsPage.countKeywords() + 1;
		keywordsPage.addSynonymToGroup(toAdd, keywordsPage.synonymGroupContaining(synonyms.get(0)));

		verifyThat("'" + toAdd + "' added to synonym group", keywordsPage.getSynonymGroupSynonyms(synonyms.get(0)), hasItem(equalToIgnoringCase(toAdd)));
		verifySynonymGroup(synonyms);
		verifySynonymGroupSize(synonyms);
		verifyKeywordState(expectedGroups, expectedKeywords);
		return synonyms;
	}

	private void verifyCannotAddToGroup(String toAdd, List<String> synonyms) {
		int expectedGroups = keywordsPage.countSynonymLists();
		int expectedKeywords = keywordsPage.countKeywords();
		SynonymGroup group = keywordsPage.synonymGroupContaining(synonyms.get(0));
		group.synonymAddButton().click();
		group.synonymInput().setAndSubmit(toAdd);

		verifyThat("'" + toAdd + "' not added to synonym group", keywordsPage.getSynonymGroupSynonyms(synonyms.get(0)), not(hasItem(equalToIgnoringCase(toAdd))));
		verifyThat("synonym box not closed", keywordsPage.synonymGroupTextBox(synonyms.get(0)), displayed());
		verifySynonymGroup(synonyms);
		verifySynonymGroupSize(synonyms);
		verifyKeywordState(expectedGroups, expectedKeywords);
	}
}
