package com.autonomy.abc.keywords;

import com.autonomy.abc.base.HybridIsoTestBase;
import com.autonomy.abc.fixtures.KeywordTearDownStrategy;
import com.autonomy.abc.selenium.analytics.DashboardBase;
import com.autonomy.abc.selenium.keywords.*;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.menu.NotificationsDropDown;
import com.autonomy.abc.selenium.promotions.PromotionsPage;
import com.autonomy.abc.selenium.search.SearchPage;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import com.hp.autonomy.frontend.selenium.element.GritterNotice;
import com.hp.autonomy.frontend.selenium.framework.categories.SlowTest;
import com.hp.autonomy.frontend.selenium.framework.logging.ActiveBug;
import com.hp.autonomy.frontend.selenium.framework.logging.RelatedTo;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import com.hp.autonomy.frontend.selenium.util.DriverUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.util.*;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.CommonMatchers.containsItems;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.urlContains;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.hasTextThat;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

public class KeywordsPageITCase extends HybridIsoTestBase {
	private KeywordsPage keywordsPage;
	private SearchPage searchPage;
	private NotificationsDropDown notifications;
	private KeywordService keywordService;

	public KeywordsPageITCase(final TestConfig config) {
		super(config);
	}

	@Before
	public void setUp() throws MalformedURLException {
		keywordService = getApplication().keywordService();
		keywordsPage = keywordService.deleteAll(KeywordFilter.ALL);
    }

	@After
	public void tearDown() {
		new KeywordTearDownStrategy().tearDown(this);
	}

	@Test
	public void testKeywordsFilter() throws InterruptedException {
		List<String> synonyms = Arrays.asList("dog", "hound", "canine");
		String blacklist = "illegal";

		searchPage = keywordService.addSynonymGroup(synonyms);
		verifyThat("search title contains one of the synonym group", searchPage.getHeadingSearchTerm(), isIn(synonyms));

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
	@Category(SlowTest.class)
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

		keywordsPage = keywordService.goToKeywords();
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
	@Test
	@ResolvedBug("CSA-1447")
	@ActiveBug("CSA-1822")
	public void testNotificationForCreatedBlacklistedTermAndSynonymGroup() throws InterruptedException {
		List<String> notificationContents = new ArrayList<>();

		keywordService.addBlacklistTerms("orange");
		notificationContents.add("Added \"orange\" to the blacklist");

		getApplication().switchTo(PromotionsPage.class);
		verifyNotifications(notificationContents);

		keywordService.addSynonymGroup("piano keyboard pianoforte");
		notificationContents.add("Created a new synonym group containing: keyboard, piano, pianoforte");

		verifyNotifications(notificationContents);

		keywordsPage = keywordService.goToKeywords();
		Waits.loadOrFadeWait();
		keywordsPage.deleteSynonym("keyboard");
		notificationContents.add("Removed \"keyboard\" from a synonym group");

		getApplication().switchTo(PromotionsPage.class);
		verifyNotifications(notificationContents);

		keywordsPage = keywordService.goToKeywords();
		keywordsPage.filterView(KeywordFilter.BLACKLIST);
		keywordsPage.selectLanguage(Language.ENGLISH);
		keywordsPage.deleteBlacklistedTerm("orange");
		notificationContents.add("Removed \"orange\" from the blacklist");

		getApplication().switchTo(DashboardBase.class);
		verifyNotifications(notificationContents);
	}

	private void verifyNotifications(List<String> contents) {
		getElementFactory().getTopNavBar().notificationsDropdown();
		notifications = getElementFactory().getTopNavBar().getNotifications();

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

		getApplication().switchTo(PromotionsPage.class);

		Waits.waitForGritterToClear();

		getElementFactory().getTopNavBar().notificationsDropdown();
		notifications = getElementFactory().getTopNavBar().getNotifications();
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

	@RelatedTo({"CSA-1724", "CSA-1893"})
	private void verifySearchKeywords(final List<String> synonyms) {
		verifyThat("One of the synonyms is included in title", searchPage.getHeadingSearchTerm(), isIn(synonyms));
		for(String searchedFor : searchPage.youSearchedFor()) {
			verifyThat("All searched for terms are within synonym group", searchedFor, isIn(synonyms));
		}
		// TODO: re-enable query analysis
//		verifyThat("synonyms appear in query analysis", searchPage.getSynonymGroupSynonyms(synonyms.get(0)), containsItems(synonyms));
		verifyThat(searchPage.countKeywords(), is(synonyms.size()));
	}

	@Test
	public void testKeywordsCreationAndDeletionOnSecondWindow() throws InterruptedException {
		keywordService.addSynonymGroup(Language.URDU, "double duo two pair couple");
		keywordsPage = keywordService.goToKeywords();

		keywordsPage.filterView(KeywordFilter.SYNONYMS);
		keywordsPage.selectLanguage(Language.URDU);
		Waits.loadOrFadeWait();

		Window mainWindow = getWindow();
		Window secondWindow = getMainSession().openWindow(mainWindow.getUrl());

		final KeywordsPage secondKeywordsPage = getElementFactory().getKeywordsPage();
		assertThat(secondKeywordsPage.countSynonymLists(), is(1));
		assertThat(secondKeywordsPage.countKeywords(), is(5));

		mainWindow.activate();
		keywordsPage = getElementFactory().getKeywordsPage();
		Waits.loadOrFadeWait();
		keywordsPage.deleteSynonym("couple");

		secondWindow.activate();
		assertThat(secondKeywordsPage.countSynonymLists(), is(1));
		assertThat(secondKeywordsPage.countKeywords(), is(4));

		mainWindow.activate();
		keywordsPage = getElementFactory().getKeywordsPage();
		Waits.loadOrFadeWait();
		keywordsPage.deleteSynonym("pair");

		secondWindow.activate();
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

		keywordsPage.getSynonymIcon("ying").click();
		if (keywordsPage.getSynonymIcon("ying").getAttribute("class").contains("fa-spin")) {
			assertThat("Spinner not present on last synonym", keywordsPage.getSynonymIcon("yang").getAttribute("class"),containsString("fa-spin"));
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
	@ResolvedBug("CCUK-3243")
	public void testQuickSynonymDelete() throws InterruptedException {
		keywordService.addSynonymGroup(Language.GERMAN, "string strong strang streng strung");
		keywordService.goToKeywords();

		keywordsPage.filterView(KeywordFilter.SYNONYMS);
		keywordsPage.selectLanguage(Language.GERMAN);
		Waits.loadOrFadeWait();

		try {
			DriverUtil.scrollIntoViewAndClick(getDriver(), keywordsPage.getSynonymIcon("strong"));
			DriverUtil.scrollIntoViewAndClick(getDriver(), keywordsPage.getSynonymIcon("string"));
		} catch (final WebDriverException w) {
			throw new AssertionError("Unable to delete a synonym quickly", w);
		}
		Thread.sleep(15000);
		assertThat("Incorrect number of synonyms", keywordsPage.countSynonymLists(), is(1));
		assertThat(keywordsPage.countKeywords(), is(3));
	}

	@Test
	@ResolvedBug("CCUK-3245")
	public void testAddingForbiddenKeywordsFromUrl() {
		String blacklistUrl = getAppUrl() + "/keywords/create/blacklisted/English/";
		String synonymsUrl = getAppUrl() + "/keywords/create/synonyms/English/";
		if (isOnPrem()) {
			blacklistUrl = getAppUrl() + "keywords/create/blacklisted/englishUTF8/";
			synonymsUrl = getAppUrl() + "keywords/create/synonyms/englishUTF8/";
		}
		//TODO check that OR has been added in lower case?
		for (final String forbidden : Arrays.asList("(", "\"", "OR")) {
			getDriver().get(blacklistUrl + forbidden);
			Waits.loadOrFadeWait();
			CreateNewKeywordsPage createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
			assertThat(forbidden + " is a forbidden keyword and should not be included in the prospective blacklist list", createKeywordsPage.getTriggerForm().getTriggersAsStrings(),not(hasItem(forbidden)));

			getDriver().get(synonymsUrl + forbidden);
			Waits.loadOrFadeWait();
			createKeywordsPage = getElementFactory().getCreateNewKeywordsPage();
			assertThat(forbidden + " is a forbidden keyword and should not be included in the prospective synonyms list", createKeywordsPage.getTriggerForm().getTriggersAsStrings(),not(hasItem(forbidden)));
		}
	}

	@Test
	public void testSynonymsDisplayedInAlphabeticalOrder() throws InterruptedException {
		for (final String synonyms : Arrays.asList("aa ba ca da", "ab bb cb db", "dc cc bc ac", "ca ba da aa")) {
			searchPage = keywordService.addSynonymGroup(Language.ENGLISH, synonyms);
			final List<String> keywords = searchPage.getFirstSynonymInGroup();

			for (int i = 0; i < keywords.size() - 1; i++) {
				assertThat(keywords.get(i).compareTo(keywords.get(i + 1)) <= 0, is(true));
			}
			Waits.loadOrFadeWait();
			keywordsPage = keywordService.goToKeywords();
		}

		Waits.loadOrFadeWait();
		keywordsPage = getElementFactory().getKeywordsPage();
		keywordsPage.filterView(KeywordFilter.SYNONYMS);
		keywordsPage.searchFilterTextBox().sendKeys("cc");
		final List<String> keywords = keywordsPage.getFirstSynonymsList();

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
			keywordsPage.getSynonymIcon(synonym).click();

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
		getElementFactory().getTopNavBar().notificationsDropdown();
		notifications = getElementFactory().getTopNavBar().getNotifications();
		assertThat(notifications.notificationNumber(1).getText(), is("Created a new synonym group containing: " + synonyms[0].toLowerCase() + ", " + synonyms[1].toLowerCase() + ", " + synonyms[2].toLowerCase()));
	}

	@Test
	public void testBlacklistNotificationText() throws InterruptedException {
		String blacklistOne = "Aardvark";
		String blacklistTwo = "Aardwolf";

		keywordService.addBlacklistTerms(blacklistOne, blacklistTwo);
		getElementFactory().getTopNavBar().notificationsDropdown();
		notifications = getElementFactory().getTopNavBar().getNotifications();

		assertThat(notifications.notificationNumber(1).getText(), anyOf(is("Added \"" + blacklistOne.toLowerCase() + "\" to the blacklist"), is("Added \"" + blacklistTwo.toLowerCase() + "\" to the blacklist")));
		assertThat(notifications.notificationNumber(2).getText(), anyOf(is("Added \"" + blacklistOne.toLowerCase() + "\" to the blacklist"), is("Added \"" + blacklistTwo.toLowerCase() + "\" to the blacklist")));
		assertThat(notifications.notificationNumber(1).getText(), not(notifications.notificationNumber(2).getText()));
	}

	@Test
	@ActiveBug("CSA-1882")
	public void testClickingOnNotifications() throws InterruptedException {
		keywordService.addSynonymGroup("a b c d");

		getApplication().switchTo(PromotionsPage.class);
		getElementFactory().getPromotionsPage();
		getElementFactory().getTopNavBar().notificationsDropdown();
		notifications = getElementFactory().getTopNavBar().getNotifications();
		verifyThat(notifications.notificationNumber(1), hasTextThat(startsWith("Created a new synonym group containing: ")));

		verifyClickNotification();
		if(!getWindow().getUrl().contains("keywords")){
			keywordsPage = keywordService.goToKeywords();
		}

		keywordService.addBlacklistTerms("e");

		getApplication().switchTo(DashboardBase.class);
		getElementFactory().getTopNavBar().notificationsDropdown();
		notifications = getElementFactory().getTopNavBar().getNotifications();
		verifyThat(notifications.notificationNumber(1).getText(), is("Added \"e\" to the blacklist"));
		verifyClickNotification();
	}

	/**
	 * The assumed behaviour for where a blacklisted item is also within the synonym group is as follows:
	 *
	 * 		- Searching for the blacklisted term will bring up no results
	 * 		- Searching for the other terms within the synonym group will bring up results for ALL terms within the synonym group INCLUDING the blacklisted term
	 *
	 */
	@Test
	public void testOverlappingBlacklistSynonym() {
		String blacklist = "blacklist";
		String synonym = "synonym";
		String[] synonymGroup = new String[]{blacklist, synonym};

		keywordService.addSynonymGroup(synonymGroup);
		keywordService.addBlacklistTerms(blacklist);

		getElementFactory().getTopNavBar().search(blacklist);

		searchPage = getElementFactory().getSearchPage();

		//Make sure no results show up for blacklisted terms
		assertThat(searchPage.visibleDocumentsCount(), is(0));

		getElementFactory().getTopNavBar().search(synonym);

		searchPage = getElementFactory().getSearchPage();

		//Make sure some results show up for terms within the synonym group
		assertThat(searchPage.visibleDocumentsCount(), not(0));
	}

	@Test
	@ResolvedBug("CSA-1440")
	public void testNavigatingAwayBeforeKeywordAdded() throws InterruptedException {
		keywordService.addKeywords(KeywordWizardType.BLACKLIST, Language.ENGLISH, Collections.singletonList("Jeff"));

		getApplication().switchTo(PromotionsPage.class);

		new WebDriverWait(getDriver(),30).until(GritterNotice.notificationContaining("blacklist"));

		assertThat(getWindow(), urlContains("promotions"));
	}

	@Test
	@ResolvedBug("CSA-1686")
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
