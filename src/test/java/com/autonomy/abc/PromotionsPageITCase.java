package com.autonomy.abc;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.*;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class PromotionsPageITCase extends ABCTestBase {

	public PromotionsPageITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
	}

	private TopNavBar topNavBar;
	private PromotionsPage promotionsPage;
	private SearchPage searchPage;
	private SchedulePage schedulePage;
	private CreateNewDynamicPromotionsPage dynamicPromotionsPage;

	@Before
	public void setUp() throws MalformedURLException {
		topNavBar = body.getTopNavBar();
		promotionsPage = body.getPromotionsPage();
		promotionsPage.deleteAllPromotions();
	}

	@Test
	public void testNewPromotionButtonLink() {
		promotionsPage.newPromotionButton().click();
		assertThat("linked to wrong page", getDriver().getCurrentUrl().endsWith("promotions/new"));
		assertThat("linked to wrong page", body.getText().contains("Create New Promotion"));
	}

	@Test
	public void testCorrectDocumentsInPromotion() {
		final List<String> promotedDocTitles = setUpANewMultiDocPromotion("English", "cars", "Sponsored", "wheels", 18);
		final List<String> promotedList = promotionsPage.getPromotedList();

		for (final String title : promotedDocTitles) {
			assertThat("Promoted document title '" + title + "' does not match promoted documents on promotions page", promotedList.contains(title));
		}
	}

	@Test
	public void testDeletePromotedDocuments() {
		final List<String> promotedDocTitles = setUpANewMultiDocPromotion("English", "cars", "Sponsored", "wheels", 5);
		int numberOfDocuments = promotionsPage.getPromotedList().size();

		for (final String title : promotedDocTitles) {
			final String documentSummary = promotionsPage.promotedDocumentSummary(title);
			promotionsPage.deleteDocument(title);
			numberOfDocuments = numberOfDocuments - 1;

			if (numberOfDocuments > 1) {
				assertThat("A document with title '" + title + "' has not been deleted", promotionsPage.getPromotedList().size() == numberOfDocuments);
				assertThat("A document with title '" + title + "' has not been deleted", !promotionsPage.getPromotedList().contains(documentSummary));
			} else {
				assertThat("delete icon should be hidden when only one document remaining", !promotionsPage.findElement(By.cssSelector(".remove-document-reference")).isDisplayed());
				break;
			}
		}
	}

	@Test
	public void testWhitespaceTrigger() {
		setUpANewPromotion("English", "cars", "Sponsored", "wheels");

		promotionsPage.tryClickThenTryParentClick(promotionsPage.triggerAddButton());

		assertThat("Number of triggers does not equal 1", promotionsPage.getSearchTriggersList().size() == 1);

		promotionsPage.addSearchTrigger("trigger");
		assertThat("Number of triggers does not equal 1", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.addSearchTrigger("   ");
		assertThat("Number of triggers does not equal 1", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.addSearchTrigger(" trigger");
		assertThat("Whitespace at beginning should be ignored", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.addSearchTrigger("\t");
		assertThat("Whitespace at beginning should be ignored", promotionsPage.getSearchTriggersList().size() == 2);
	}

	@Test
	public void testQuotesTrigger() throws InterruptedException {
		setUpANewPromotion("English", "cars", "Sponsored", "wheels");

		assertThat("Number of triggers does not equal 1", promotionsPage.getSearchTriggersList().size() == 1);

		promotionsPage.addSearchTrigger("bag");
		assertThat("Number of triggers does not equal 2", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.addSearchTrigger("\"bag");
		assertThat("Number of triggers does not equal 2", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.waitForGritterToClear();
		promotionsPage.addSearchTrigger("bag\"");
		assertThat("Number of triggers does not equal 2", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.addSearchTrigger("\"bag\"");
		assertThat("Number of triggers does not equal 2", promotionsPage.getSearchTriggersList().size() == 2);
	}

	@Test
	public void testCommasTrigger() {
		setUpANewPromotion("English", "cars", "Sponsored", "wheels");

		promotionsPage.addSearchTrigger("France");
		assertThat("Number of triggers does not equal 1", promotionsPage.getSearchTriggersList().size() == 2);

		promotionsPage.addSearchTrigger(",Germany");
		assertThat("Commas should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 2);
		assertThat("No/incorrect error message", promotionsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));

		promotionsPage.addSearchTrigger("Ita,ly Spain");
		assertThat("Commas should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 2);
		assertThat("No/incorrect error message", promotionsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));

		promotionsPage.addSearchTrigger("Ireland, Belgium");
		assertThat("Commas should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 2);
		assertThat("No/incorrect error message", promotionsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));

		promotionsPage.addSearchTrigger("UK , Luxembourg");
		assertThat("Commas should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 2);
		assertThat("No/incorrect error message", promotionsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));

		promotionsPage.addSearchTrigger("Greece Romania");
		assertThat("New triggers not added", promotionsPage.getSearchTriggersList().size() == 4);
		assertThat("Error message still showing", !promotionsPage.getText().contains("Terms may not contain commas or double quotes. Separate words with whitespace."));
	}

	@Test
	public void testHTMLTrigger() {
		setUpANewPromotion("English", "vans", "Sponsored", "shoes");
		final String trigger = "<h1>Hi</h1>";
		promotionsPage.addSearchTrigger(trigger);

		assertThat("Triggers should be HTML escaped", promotionsPage.getSearchTriggersList().contains(trigger));
	}

	@Test
	public void testAddRemoveTriggers() throws InterruptedException {
		setUpANewPromotion("English", "cars", "Sponsored", "wheels");
		promotionsPage.addSearchTrigger("alpha");
		promotionsPage.removeSearchTrigger("wheels");
		assertThat("Number of search terms does not equal 1", promotionsPage.getSearchTriggersList().size() == 1);
		assertThat("Original search trigger 'wheels has not been deleted'", !promotionsPage.getSearchTriggersList().contains("wheels"));

		promotionsPage.addSearchTrigger("beta gamma delta");
		promotionsPage.waitForGritterToClear();
		promotionsPage.removeSearchTrigger("gamma");
		promotionsPage.removeSearchTrigger("alpha");
		promotionsPage.addSearchTrigger("epsilon");
		promotionsPage.removeSearchTrigger("beta");

		assertThat("Number of triggers does not equal 2", promotionsPage.getSearchTriggersList().size() == 2);
		assertThat("Trigger 'delta' not present", promotionsPage.getSearchTriggersList().contains("delta"));
		assertThat("Trigger 'epsilon' not present", promotionsPage.getSearchTriggersList().contains("epsilon"));

		promotionsPage.removeSearchTrigger("epsilon");
		assertThat("It should not be possible to delete the last trigger", promotionsPage.findElements(By.cssSelector(".remove-word")).size() == 0);
	}

	@Test
	public void testBackButton() {
		setUpANewPromotion("English", "bicycle", "Sponsored", "tyres");
		promotionsPage.backButton().click();
		assertThat("Back button does not redirect to main promotions page", getDriver().getCurrentUrl().endsWith("promotions"));
	}

	@Test
	public void testEditPromotionName() throws InterruptedException {
		setUpANewPromotion("English", "cars", "Sponsored", "wheels");
		assertThat("Incorrect promotion title", promotionsPage.getPromotionTitle().equals("Spotlight for: wheels"));

		promotionsPage.createNewTitle("Fuzz");
		assertThat("Incorrect promotion title", promotionsPage.getPromotionTitle().equals("Fuzz"));

		promotionsPage.createNewTitle("<script> alert(\"hi\") </script>");
		assertThat("Incorrect promotion title", promotionsPage.getPromotionTitle().equals("<script> alert(\"hi\") </script>"));
	}

	@Test
	public void testEditPromotionType() {
		setUpANewPromotion("English", "cars", "Sponsored", "wheels");
		assertThat("Incorrect promotion type", promotionsPage.getPromotionType().equals("Sponsored"));

		promotionsPage.changeSpotlightType("Hotwire");
		assertThat("Incorrect promotion type", promotionsPage.getPromotionType().equals("Hotwire"));

		promotionsPage.changeSpotlightType("Top Promotions");
		assertThat("Incorrect promotion type", promotionsPage.getPromotionType().equals("Top Promotions"));

		promotionsPage.changeSpotlightType("Sponsored");
		assertThat("Incorrect promotion type", promotionsPage.getPromotionType().equals("Sponsored"));
	}

	@Test
	public void testDeletePromotions() throws InterruptedException {
		setUpANewPromotion("English", "rabbit", "Sponsored", "bunny");
		promotionsPage.backButton().click();
		setUpANewPromotion("English", "horse", "Sponsored", "pony");
		promotionsPage.backButton().click();
		setUpANewPromotion("English", "dog", "Sponsored", "<script> alert('hi') </script>");
		promotionsPage.backButton().click();

		assertThat("promotion 'bunny' not created", promotionsPage.getPromotionLinkWithTitleContaining("bunny").isDisplayed());
		assertThat("promotion 'pony' not created", promotionsPage.getPromotionLinkWithTitleContaining("pony").isDisplayed());
		assertThat("promotion 'pooch' not created", promotionsPage.getPromotionLinkWithTitleContaining("script").isDisplayed());
		assertThat("promotion 'pooch' not created", promotionsPage.promotionsList().size() == 3);

		promotionsPage.getPromotionLinkWithTitleContaining("bunny").click();
		body.waitForGritterToClear();
		promotionsPage.deletePromotion();

		assertThat("promotion 'pony' has been deleted", promotionsPage.getPromotionLinkWithTitleContaining("pony").isDisplayed());
		assertThat("promotion 'script' has been deleted", promotionsPage.getPromotionLinkWithTitleContaining("script").isDisplayed());
		assertThat("promotion 'bunny' not deleted", promotionsPage.promotionsList().size() == 2);

		promotionsPage.getPromotionLinkWithTitleContaining("script").click();
		promotionsPage.deletePromotion();

		assertThat("promotion 'pony' has been deleted", promotionsPage.getPromotionLinkWithTitleContaining("pony").isDisplayed());
		assertThat("promotion 'script' not deleted", promotionsPage.promotionsList().size() == 1);

		promotionsPage.getPromotionLinkWithTitleContaining("pony").click();
		promotionsPage.deletePromotion();

		assertThat("promotion 'pony' not deleted", promotionsPage.promotionsList().size() == 0);
	}

	@Test
	public void testAddingLotsOfDocsToAPromotion() {
		setUpANewMultiDocPromotion("English", "sith", "Hotwire", "darth sith", 100);
		assertEquals(promotionsPage.getPromotedList().size(), 100);
	}

	private String setUpANewPromotion(final String language, final String navBarSearchTerm, final String spotlightType, final String searchTriggers) {
		topNavBar.search(navBarSearchTerm);
		searchPage = body.getSearchPage();
		searchPage.selectLanguage(language);
		final String promotedDocTitle = searchPage.createAPromotion();
		final CreateNewPromotionsPage createPromotionsPage = body.getCreateNewPromotionsPage();
		createPromotionsPage.addSpotlightPromotion(spotlightType, searchTriggers);

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		navBar.getTab(NavBarTabId.PROMOTIONS).click();
		promotionsPage = body.getPromotionsPage();

		promotionsPage.getPromotionLinkWithTitleContaining(searchTriggers.split(" ")[0]).click();

		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(promotionsPage.addMorePromotedItemsButton()));
		return promotedDocTitle;
	}

	private List <String> setUpANewMultiDocPromotion(final String language, final String navBarSearchTerm, final String spotlightType, final String searchTriggers, final int numberOfDocs) {
		topNavBar.search(navBarSearchTerm);
		topNavBar.loadOrFadeWait();
		searchPage = body.getSearchPage();
		searchPage.selectLanguage(language);
		final List<String> promotedDocTitles = searchPage.createAMultiDocumentPromotion(numberOfDocs);
		final CreateNewPromotionsPage createPromotionsPage = body.getCreateNewPromotionsPage();
		createPromotionsPage.addSpotlightPromotion(spotlightType, searchTriggers);

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		navBar.getTab(NavBarTabId.PROMOTIONS).click();
		promotionsPage = body.getPromotionsPage();
		promotionsPage.getPromotionLinkWithTitleContaining(searchTriggers).click();

		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(promotionsPage.triggerAddButton()));
		return promotedDocTitles;
	}

	private List <String> setUpANewMultiDocPinToPositionPromotion(final String language, final String navBarSearchTerm, final String searchTriggers, final int numberOfDocs) {
		topNavBar.search(navBarSearchTerm);
		searchPage = body.getSearchPage();
		searchPage.selectLanguage(language);
		final List<String> promotedDocTitles = searchPage.createAMultiDocumentPromotion(numberOfDocs);
		final CreateNewPromotionsPage createPromotionsPage = body.getCreateNewPromotionsPage();
		createPromotionsPage.promotionType("PIN_TO_POSITION").click();
		createPromotionsPage.continueButton("type").click();
		createPromotionsPage.loadOrFadeWait();
		createPromotionsPage.continueButton("pinToPosition").click();
		createPromotionsPage.loadOrFadeWait();
		createPromotionsPage.addSearchTrigger(searchTriggers);
		createPromotionsPage.finishButton().click();
		createPromotionsPage.loadOrFadeWait();

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		navBar.getTab(NavBarTabId.PROMOTIONS).click();
		promotionsPage = body.getPromotionsPage();
		promotionsPage.getPromotionLinkWithTitleContaining(searchTriggers).click();

		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(promotionsPage.triggerAddButton()));
		return promotedDocTitles;
	}

	private String setUpANewDynamicPromotion(final String language, final String navBarSearchTerm, final String searchTriggers, final String spotlightType) {
		topNavBar.search(navBarSearchTerm);
		searchPage = body.getSearchPage();
		searchPage.selectLanguage(language);
		final String searchResultTitle = searchPage.getSearchResult(1).getText();
		searchPage.promoteThisQueryButton().click();
		searchPage.loadOrFadeWait();
		dynamicPromotionsPage = body.getCreateNewDynamicPromotionsPage();
		dynamicPromotionsPage.createDynamicPromotion(spotlightType, searchTriggers);
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteTheseDocumentsButton()));
		navBar.getTab(NavBarTabId.PROMOTIONS).click();
		promotionsPage = body.getPromotionsPage();
		promotionsPage.getPromotionLinkWithTitleContaining(searchTriggers).click();
		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(promotionsPage.triggerAddButton()));

		return searchResultTitle;
	}

	@Test
	public void testSchedulePromotionForTomorrow() {
		setUpANewMultiDocPromotion("English", "wizard", "Sponsored", "wand magic spells", 4);
		promotionsPage.schedulePromotion();

		schedulePage = body.getSchedulePage();
		assertThat("Wrong URL", getDriver().getCurrentUrl().contains("schedule"));
		assertThat("Wrong wizard text", schedulePage.getText().contains("Schedule your promotion"));
		assertThat("Finish button not visible", schedulePage.finishButton("enableSchedule").isDisplayed()); //TODO: Enum of datasteps
		assertThat("Finish button should be disabled", schedulePage.isAttributePresent(schedulePage.finishButton("enableSchedule"), "disabled"));

		schedulePage.alwaysActive().click();
		assertThat("Finish button should be enabled", !schedulePage.isAttributePresent(schedulePage.finishButton("enableSchedule"), "disabled"));

		schedulePage.schedule().click();
		assertThat("Continue button should be present", schedulePage.continueButton("enableSchedule").isDisplayed());

		schedulePage.continueButton("enableSchedule").click();
		schedulePage.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText().contains("When should this promotion start and end?"));

		assertEquals(schedulePage.startDateTextBox().getAttribute("value"), schedulePage.dateAsString(schedulePage.getTodayDate()));
		assertEquals(schedulePage.endDateTextBox().getAttribute("value"), schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 1)));

		schedulePage.startDateTextBox().click();
		assertEquals(schedulePage.getSelectedDayOfMonth(), schedulePage.getDay());
		assertEquals(schedulePage.getSelectedMonth(), schedulePage.getMonth());

		schedulePage.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 1));
		assertEquals(schedulePage.startDateTextBox().getAttribute("value"), schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 1)));
		final String startDate = schedulePage.startDateTextBox().getAttribute("value");

		schedulePage.endDateTextBox().click();
		schedulePage.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 5));
		assertEquals(schedulePage.endDateTextBox().getAttribute("value"), schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 5)));
		final String endDate = schedulePage.endDateTextBox().getAttribute("value");

		schedulePage.continueButton("scheduleTimes").click();
		schedulePage.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText().contains("How often should this promotion run?"));

		schedulePage.selectFrequency("Monthly");
		assertEquals(schedulePage.readFrequency(), "Monthly");

		schedulePage.finishButton("scheduleFrequency").click();
		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(promotionsPage.backButton()));
		assert(promotionsPage.getText().contains("The promotion is scheduled to run monthly, starting from " + startDate + " and ending on " + endDate));

		topNavBar.search("magic");
		topNavBar.loadOrFadeWait();
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));
		assertThat("promotions aren't scheduled to be shown now", !searchPage.isPromotionsBoxVisible());

		navBar.switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage.getPromotionLinkWithTitleContaining("magic").click();
		promotionsPage.schedulePromotion();
		schedulePage.alwaysActive().click();
		schedulePage.finishButton("enableSchedule").click();
		schedulePage.loadOrFadeWait();
		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(promotionsPage.backButton()));
		assert(promotionsPage.getText().contains("The promotion is always active"));

		topNavBar.search("magic");
		topNavBar.loadOrFadeWait();
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));
		assertThat("promotions are scheduled to be shown now", searchPage.isPromotionsBoxVisible());
	}

	@Test
	public void testScheduleStartBeforeEnd() {
		setUpANewMultiDocPromotion("English", "cone", "Hotwire", "\"ice cream\" chips", 4);
		promotionsPage.schedulePromotion();

		schedulePage = body.getSchedulePage();
		assertThat("Wrong URL", getDriver().getCurrentUrl().contains("schedule"));
		assertThat("Wrong wizard text", schedulePage.getText().contains("Schedule your promotion"));
		assertThat("Finish button not visible", schedulePage.finishButton("enableSchedule").isDisplayed());
		assertThat("Finish button should be disabled", schedulePage.isAttributePresent(schedulePage.finishButton("enableSchedule"), "disabled"));

		schedulePage.alwaysActive().click();
		assertThat("Finish button should be enabled", !schedulePage.isAttributePresent(schedulePage.finishButton("enableSchedule"), "disabled"));

		schedulePage.schedule().click();
		assertThat("Continue button should be present", schedulePage.continueButton("enableSchedule").isDisplayed());

		schedulePage.alwaysActive().click();
		assertThat("Finish button should be enabled", !schedulePage.isAttributePresent(schedulePage.finishButton("enableSchedule"), "disabled"));

		schedulePage.schedule().click();
		assertThat("Continue button should be present", schedulePage.continueButton("enableSchedule").isDisplayed());

		schedulePage.continueButton("enableSchedule").click();
		schedulePage.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText().contains("When should this promotion start and end?"));

		assertEquals(schedulePage.startDateTextBox().getAttribute("value"), schedulePage.dateAsString(schedulePage.getTodayDate()));
		assertEquals(schedulePage.endDateTextBox().getAttribute("value"), schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 1)));

		schedulePage.startDateTextBox().click();
		assertEquals(schedulePage.getSelectedDayOfMonth(), schedulePage.getDay());
		assertEquals(schedulePage.getSelectedMonth(), schedulePage.getMonth());

		schedulePage.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 3));
		assertEquals(schedulePage.startDateTextBox().getAttribute("value"), schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 3)));

		schedulePage.endDateTextBox().click();
		schedulePage.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 2));
		assertEquals(schedulePage.endDateTextBox().getAttribute("value"), schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 2)));

		assert(getDriver().findElement(By.cssSelector(".wizard")).getText().contains("End date cannot be before the start date"));
		assertThat("Continue button should be disabled", schedulePage.isAttributePresent(schedulePage.continueButton("scheduleTimes"), "disabled"));

		schedulePage.endDateTextBox().click();
		schedulePage.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 4));
		assertEquals(schedulePage.endDateTextBox().getAttribute("value"), schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 4)));
		String endDate = schedulePage.endDateTextBox().getAttribute("value");

		assert(!getDriver().findElement(By.cssSelector(".wizard")).getText().contains("End date cannot be before the start date"));
		assertThat("Continue button should be enabled", !schedulePage.isAttributePresent(schedulePage.continueButton("scheduleTimes"), "disabled"));

		schedulePage.startDateTextBox().click();
		schedulePage.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 9));
		assertEquals(schedulePage.startDateTextBox().getAttribute("value"), schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 9)));

		assert(getDriver().findElement(By.cssSelector(".wizard")).getText().contains("End date cannot be before the start date"));
		assertThat("Continue button should be disabled", schedulePage.isAttributePresent(schedulePage.continueButton("scheduleTimes"), "disabled"));

		schedulePage.startDateTextBox().click();
		schedulePage.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 2));
		assertEquals(schedulePage.startDateTextBox().getAttribute("value"), schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 2)));

		assert(!getDriver().findElement(By.cssSelector(".wizard")).getText().contains("End date cannot be before the start date"));
		assertThat("Continue button should be enabled", !schedulePage.isAttributePresent(schedulePage.continueButton("scheduleTimes"), "disabled"));

		String startDate = schedulePage.startDateTextBox().getAttribute("value");
		schedulePage.continueButton("scheduleTimes").click();
		schedulePage.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText().contains("How often should this promotion run?"));

		schedulePage.selectFrequency("Daily");
		assertEquals(schedulePage.readFrequency(), "Daily");

		schedulePage.finishButton("scheduleFrequency").click();
		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(promotionsPage.backButton()));
		assert(promotionsPage.getText().contains("The promotion is scheduled to run daily, starting from " + startDate + " and ending on " + endDate));

		topNavBar.search("chips");
		topNavBar.loadOrFadeWait();
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));
		assertThat("promotions aren't scheduled to be shown now", !searchPage.isPromotionsBoxVisible());

		navBar.switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage.getPromotionLinkWithTitleContaining("chips").click();
		promotionsPage.schedulePromotion();
		schedulePage.schedule().click();
		schedulePage.continueButton("enableSchedule").click();
		schedulePage.loadOrFadeWait();

		schedulePage.startDateTextBox().click();
		schedulePage.calendarDateSelect(schedulePage.getTodayDate());
		assertEquals(schedulePage.startDateTextBox().getAttribute("value"), schedulePage.dateAsString(schedulePage.getTodayDate()));

		startDate = schedulePage.startDateTextBox().getAttribute("value");
		endDate = schedulePage.endDateTextBox().getAttribute("value");

		schedulePage.continueButton("scheduleTimes").click();
		schedulePage.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText().contains("How often should this promotion run?"));

		schedulePage.selectFrequency("Daily");
		assertEquals(schedulePage.readFrequency(), "Daily");

		schedulePage.finishButton("scheduleFrequency").click();
		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(promotionsPage.backButton()));
		assert(promotionsPage.getText().contains("The promotion is scheduled to run daily, starting from " + startDate + " and ending on " + endDate));

		topNavBar.search("magic");
		topNavBar.loadOrFadeWait();
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));
		assertThat("promotions are scheduled to be shown now", searchPage.isPromotionsBoxVisible());
	}

	@Test
	public void testPromotionFilter() throws InterruptedException {
		setUpANewPromotion("French", "chien", "Hotwire", "woof bark");
		promotionsPage.backButton().click();
		setUpANewPromotion("Arabic", "الكلب", "Top Promotions", "dog chien");
		promotionsPage.backButton().click();
		setUpANewPromotion("English", "dog", "Sponsored", "hound pooch");
		promotionsPage.backButton().click();
		setUpANewMultiDocPinToPositionPromotion("Swahili", "mbwa", "woof swahili", 3);
		promotionsPage.backButton().click();
		setUpANewMultiDocPinToPositionPromotion("Swahili", "mbwa", "pooch swahili", 3);
		promotionsPage.backButton().click();
		setUpANewDynamicPromotion("Afrikaans", "pooch", "hond wolf", "Hotwire");
		promotionsPage.backButton().click();
		setUpANewDynamicPromotion("Afrikaans", "pooch", "lupo wolf", "Sponsored");
		promotionsPage.backButton().click();
		promotionsPage.loadOrFadeWait();
		assertEquals(7, promotionsPage.promotionsList().size());

		promotionsPage.promotionsSearchFilter().sendKeys("dog");
		assertEquals(1, promotionsPage.promotionsList().size());

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("wolf");
		assertEquals(2, promotionsPage.promotionsList().size());

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("pooch");
		assertEquals(4, promotionsPage.promotionsList().size());

		promotionsPage.getPromotionLinkWithTitleContaining("hound").click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.createNewTitle("hound");
		assertThat("title has not changed", promotionsPage.getPromotionTitle().equals("hound"));
		promotionsPage.backButton().click();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("pooch");
		assertEquals(4, promotionsPage.promotionsList().size());

		promotionsPage.getPromotionLinkWithTitleContaining("hound").click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.removeSearchTrigger("pooch");
		assertThat("trigger not removed", !promotionsPage.getSearchTriggersList().contains("pooch"));
		promotionsPage.backButton().click();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("pooch");
		assertEquals(3, promotionsPage.promotionsList().size());

		assertEquals("All Types", promotionsPage.promotionsCategoryFilterValue());

		promotionsPage.selectPromotionsCategoryFilter("Spotlight");
		promotionsPage.clearPromotionsSearchFilter();
		assertEquals("Spotlight", promotionsPage.promotionsCategoryFilterValue());
		assertEquals(3, promotionsPage.promotionsList().size());

		promotionsPage.promotionsSearchFilter().sendKeys("woof");
		assertEquals(1, promotionsPage.promotionsList().size());

		promotionsPage.selectPromotionsCategoryFilter("Pin to Position");
		promotionsPage.clearPromotionsSearchFilter();
		assertEquals("Pin to Position", promotionsPage.promotionsCategoryFilterValue());
		assertEquals(2, promotionsPage.promotionsList().size());

		promotionsPage.promotionsSearchFilter().sendKeys("woof");
		assertEquals(1, promotionsPage.promotionsList().size());

		promotionsPage.clearPromotionsSearchFilter();
		assertEquals(2, promotionsPage.promotionsList().size());

		promotionsPage.selectPromotionsCategoryFilter("Dynamic Spotlight");
		promotionsPage.promotionsSearchFilter().sendKeys("wolf");
		assertEquals(2, promotionsPage.promotionsList().size());

		promotionsPage.getPromotionLinkWithTitleContaining("lupo").click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.removeSearchTrigger("wolf");
		assertThat("trigger not removed", !promotionsPage.getSearchTriggersList().contains("wolf"));
		promotionsPage.backButton().click();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("wolf");
		assertEquals(2, promotionsPage.promotionsList().size());

		promotionsPage.getPromotionLinkWithTitleContaining("lupo").click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.createNewTitle("lupo");
		assertThat("title has not changed", promotionsPage.getPromotionTitle().equals("lupo"));
		promotionsPage.backButton().click();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("wolf");
		assertEquals(1, promotionsPage.promotionsList().size());

		promotionsPage.getPromotionLinkWithTitleContaining("hond").click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.addSearchTrigger("Rhodesian Ridgeback");
		assertThat("trigger not added", promotionsPage.getSearchTriggersList().containsAll(Arrays.asList("Rhodesian", "Ridgeback")));
		promotionsPage.backButton().click();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("Rhodesian");
		assertEquals(1, promotionsPage.promotionsList().size());

		promotionsPage.selectPromotionsCategoryFilter("All Types");
		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("Ridgeback");
		assertEquals(1, promotionsPage.promotionsList().size());
	}

	@Test
	public void testPromotionLanguages() {
		setUpANewPromotion("French", "chien", "Hotwire", "woof bark");
		assertEquals("French", promotionsPage.getLanguage());

		promotionsPage.backButton().click();
		setUpANewMultiDocPinToPositionPromotion("Swahili", "mbwa", "woof swahili", 3);
		assertEquals("Swahili", promotionsPage.getLanguage());

		promotionsPage.backButton().click();
		setUpANewDynamicPromotion("Afrikaans", "pooch", "hond wolf", "Hotwire");
		assertEquals("Afrikaans", promotionsPage.getLanguage());
	}

	@Test
	public void testEditDynamicQuery() throws InterruptedException {
		topNavBar.search("kitty");
		searchPage = body.getSearchPage();
		searchPage.selectLanguage("French");
		final String firstSearchResult = searchPage.getSearchResult(1).getText();
		final String secondSearchResult = setUpANewDynamicPromotion("French", "chat", "Meow", "Top Promotions");
		promotionsPage.addSearchTrigger("purrr");
		promotionsPage.removeSearchTrigger("Meow");
		topNavBar.search("purrr");
		searchPage.selectLanguage("French");
		assertEquals(secondSearchResult, searchPage.promotionsSummaryList(false).get(0));

		navBar.switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage.loadOrFadeWait();
		promotionsPage.selectPromotionsCategoryFilter("All Types");
		promotionsPage.loadOrFadeWait();
		promotionsPage.getPromotionLinkWithTitleContaining("Meow").click();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(promotionsPage.backButton()));
		assertEquals("chat", promotionsPage.getQueryText());

		promotionsPage.editQueryText("kitty");
		assertEquals("kitty", promotionsPage.getQueryText());

		topNavBar.search("purrr");
		searchPage.selectLanguage("French");
		assertEquals(firstSearchResult, searchPage.promotionsSummaryList(false).get(0));

		getDriver().navigate().refresh();
		searchPage = body.getSearchPage();
		searchPage.loadOrFadeWait();
		assertEquals(firstSearchResult, searchPage.promotionsSummaryList(false).get(0));
	}
}
