package com.autonomy.abc.promotions;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.ApplicationType;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.element.DatePicker;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.promotions.CreateNewDynamicPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.promotions.SchedulePage;
import com.autonomy.abc.selenium.page.search.SearchBase;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class PromotionsPageITCase extends ABCTestBase {

	public PromotionsPageITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}

	private TopNavBar topNavBar;
	private PromotionsPage promotionsPage;
	private SearchPage searchPage;
	private SchedulePage schedulePage;
	private CreateNewDynamicPromotionsPage dynamicPromotionsPage;
	private DatePicker datePicker;
	private final Pattern pattern = Pattern.compile("\\s+");

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
				assertThat("delete icon should be hidden when only one document remaining", promotionsPage.findElements(By.cssSelector(".remove-document-reference")).size() == 0);
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
		assertThat("No/incorrect error message", promotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

		promotionsPage.addSearchTrigger("Ita,ly Spain");
		assertThat("Commas should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 2);
		assertThat("No/incorrect error message", promotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

		promotionsPage.addSearchTrigger("Ireland, Belgium");
		assertThat("Commas should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 2);
		assertThat("No/incorrect error message", promotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

		promotionsPage.addSearchTrigger("UK , Luxembourg");
		assertThat("Commas should not be included in triggers", promotionsPage.getSearchTriggersList().size() == 2);
		assertThat("No/incorrect error message", promotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));

		promotionsPage.addSearchTrigger("Greece Romania");
		assertThat("New triggers not added", promotionsPage.getSearchTriggersList().size() == 4);
		assertThat("Error message still showing", !promotionsPage.getText().contains("Terms may not contain commas. Separate words and phrases with whitespace."));
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
		setUpANewPromotion("English", "dog", "Sponsored", "<script> document.body.innerHTML = '' </script>");
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
		assertEquals("Wrong number of documents in the promotions list", 100, promotionsPage.getPromotedList().size());
	}

	private String setUpANewPromotion(final String language, final String navBarSearchTerm, final String spotlightType, final String searchTriggers) {
		topNavBar.search(navBarSearchTerm);
		searchPage = body.getSearchPage();
		searchPage.selectLanguage(language, getConfig().getType().getName());
		final String promotedDocTitle = searchPage.createAPromotion();
		final CreateNewPromotionsPage createPromotionsPage = body.getCreateNewPromotionsPage();
		createPromotionsPage.addSpotlightPromotion(spotlightType, searchTriggers, getConfig().getType().getName());

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
		searchPage.selectLanguage(language, getConfig().getType().getName());
		final List<String> promotedDocTitles = searchPage.createAMultiDocumentPromotion(numberOfDocs);
		final CreateNewPromotionsPage createPromotionsPage = body.getCreateNewPromotionsPage();
		createPromotionsPage.addSpotlightPromotion(spotlightType, searchTriggers, getConfig().getType().getName());

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
		searchPage.selectLanguage(language, getConfig().getType().getName());
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
		searchPage.selectLanguage(language, getConfig().getType().getName());
		final String searchResultTitle;

		if (searchPage.getText().contains("No results found")) {
			searchResultTitle = null;
		} else {
			searchResultTitle = searchPage.getSearchResult(1).getText();
		}

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
	public void testSchedulePromotionForTomorrow() throws ParseException {
		setUpANewMultiDocPromotion("English", "wizard", "Sponsored", "wand magic spells", 4);
		promotionsPage.schedulePromotion();

		try {
			schedulePage = body.getSchedulePage();
		} catch (final NoSuchElementException e) {
			fail("Schedule Page has not loaded");
		}

		assertThat("Wrong URL", getDriver().getCurrentUrl().contains("schedule"));
		assertThat("Wrong wizard text", schedulePage.getText().contains("Schedule your promotion"));
		assertThat("Finish button not visible", schedulePage.finishButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).isDisplayed());
		assertThat("Always active isn't selected", schedulePage.alwaysActive().getAttribute("class").contains("progressive-disclosure-selection"));
		assertThat("Schedule shouldn't be selected", !schedulePage.schedule().getAttribute("class").contains("progressive-disclosure-selection"));
		assertThat("Finish button should be enabled", !schedulePage.isAttributePresent(schedulePage.finishButton(SchedulePage.WizardStep.ENABLE_SCHEDULE), "disabled"));

		schedulePage.alwaysActive().click();
		assertThat("Finish button should be enabled", !schedulePage.isAttributePresent(schedulePage.finishButton(SchedulePage.WizardStep.ENABLE_SCHEDULE), "disabled"));

		schedulePage.schedule().click();
		assertThat("Continue button should be present", schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).isDisplayed());

		schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
		schedulePage.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText().contains("How long should this promotion run?"));

		assertEquals(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0], schedulePage.dateAsString(schedulePage.getTodayDate()));
		assertEquals(pattern.split(schedulePage.endDateTextBox().getAttribute("value"))[0], schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 1)));
		assertEquals(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[1], "00:00");
		assertEquals(pattern.split(schedulePage.endDateTextBox().getAttribute("value"))[1], "00:00");

		schedulePage.startDateTextBoxButton().click();
		datePicker = body.getDatePicker();
		assertEquals(datePicker.getSelectedDayOfMonth(), schedulePage.getDay(0));
		assertEquals(datePicker.getSelectedMonth(), schedulePage.getMonth(0));

		datePicker.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 1));
		assertEquals(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0], schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 1)));
		final String startDate = SchedulePage.parseDateForPromotionsPage(schedulePage.startDateTextBox().getAttribute("value"));
		schedulePage.startDateTextBoxButton().click();
		schedulePage.loadOrFadeWait();

		schedulePage.endDateTextBoxButton().click();
		datePicker = body.getDatePicker();
		datePicker.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 5));
		assertEquals(pattern.split(schedulePage.endDateTextBox().getAttribute("value"))[0], schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 5)));
		final String endDate = SchedulePage.parseDateForPromotionsPage(schedulePage.endDateTextBox().getAttribute("value"));
		schedulePage.endDateTextBoxButton().click();
		schedulePage.loadOrFadeWait();

		schedulePage.continueButton(SchedulePage.WizardStep.START_END).click();
		schedulePage.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText().contains("Do you want to repeat this promotion schedule?"));

		schedulePage.repeatWithFrequencyBelow().click();
		schedulePage.selectFrequency(SchedulePage.Frequency.MONTHLY);
		assertEquals(schedulePage.readFrequency(), "Monthly");

		schedulePage.continueButton(SchedulePage.WizardStep.FREQUENCY).click();
		schedulePage.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText().contains("When should this promotion schedule finish?"));

		schedulePage.runThisPromotionScheduleUntilTheDateBelow().click();
		assertEquals(pattern.split(schedulePage.finalDateTextBox().getAttribute("value"))[0], schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 5)));

		schedulePage.finalDateTextBoxButton().click();
		datePicker = body.getDatePicker();
		assertEquals(datePicker.getSelectedDayOfMonth(), schedulePage.getDay(5));
		assertEquals(datePicker.getSelectedMonth(), schedulePage.getMonth(5));

		datePicker.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 502));
		assertEquals(pattern.split(schedulePage.finalDateTextBox().getAttribute("value"))[0], schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 502)));
		schedulePage.loadOrFadeWait();
		datePicker.timePickerHour().click();
		datePicker.selectTimePickerHour(3);
		datePicker.timePickerMinute().click();
		datePicker.selectTimePickerMinute(42);
		assertEquals(pattern.split(schedulePage.finalDateTextBox().getAttribute("value"))[1], "03:40");

		datePicker.setMinuteUsingIncrementDecrement(42);
		assertEquals(pattern.split(schedulePage.finalDateTextBox().getAttribute("value"))[1], "03:42");

		final String finalDate = SchedulePage.parseDateForPromotionsPage(schedulePage.finalDateTextBox().getAttribute("value"));
		schedulePage.finalDateTextBoxButton().click();
		schedulePage.loadOrFadeWait();

		schedulePage.finishButton(SchedulePage.WizardStep.FINAL).click();
		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(promotionsPage.backButton()));
		assertThat("Correct Scheduling text not visible", promotionsPage.getText().contains("The promotion is scheduled to run starting on " + startDate + " for the duration of 4 days, ending on " + endDate));
		assertThat("Correct Scheduling text not visible", promotionsPage.getText().contains("This promotion schedule will run monthly until " + finalDate));

		topNavBar.search("magic");
		topNavBar.loadOrFadeWait();
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));
		assertThat("promotions aren't scheduled to be shown now", !searchPage.isPromotionsBoxVisible());

		navBar.switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage.getPromotionLinkWithTitleContaining("magic").click();
		promotionsPage.schedulePromotion();
		promotionsPage.loadOrFadeWait();
		schedulePage = body.getSchedulePage();
		schedulePage.alwaysActive().click();
		schedulePage.finishButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
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
	public void testScheduleStartBeforeEnd() throws ParseException {
		setUpANewMultiDocPromotion("English", "cone", "Hotwire", "\"ice cream\" chips", 4);
		promotionsPage.schedulePromotion();

		try {
			schedulePage = body.getSchedulePage();
		} catch (final NoSuchElementException e) {
			fail("Schedule Page has not loaded");
		}
		assertThat("Wrong URL", getDriver().getCurrentUrl().contains("schedule"));
		assertThat("Wrong wizard text", schedulePage.getText().contains("Schedule your promotion"));
		assertThat("Finish button not visible", schedulePage.finishButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).isDisplayed());
		assertThat("Finish button should be enabled", !schedulePage.isAttributePresent(schedulePage.finishButton(SchedulePage.WizardStep.ENABLE_SCHEDULE), "disabled"));

		schedulePage.alwaysActive().click();
		assertThat("Finish button should be enabled", !schedulePage.isAttributePresent(schedulePage.finishButton(SchedulePage.WizardStep.ENABLE_SCHEDULE), "disabled"));

		schedulePage.schedule().click();
		assertThat("Continue button should be present", schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).isDisplayed());

		schedulePage.alwaysActive().click();
		assertThat("Finish button should be enabled", !schedulePage.isAttributePresent(schedulePage.finishButton(SchedulePage.WizardStep.ENABLE_SCHEDULE), "disabled"));

		schedulePage.schedule().click();
		assertThat("Continue button should be present", schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).isDisplayed());

		schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
		schedulePage.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText().contains("How long should this promotion run?"));

		assertEquals(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0], schedulePage.dateAsString(schedulePage.getTodayDate()));
		assertEquals(pattern.split(schedulePage.endDateTextBox().getAttribute("value"))[0], schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 1)));

		schedulePage.startDateTextBoxButton().click();
		datePicker = body.getDatePicker();
		assertEquals(datePicker.getSelectedDayOfMonth(), schedulePage.getDay(0));
		assertEquals(datePicker.getSelectedMonth(), schedulePage.getMonth(0));

		datePicker.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 3));
		assertEquals(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0], schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 3)));
		schedulePage.startDateTextBoxButton().click();

		schedulePage.endDateTextBoxButton().click();
		datePicker = body.getDatePicker();
		datePicker.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 2));
		assertEquals(pattern.split(schedulePage.endDateTextBox().getAttribute("value"))[0], schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 2)));
		schedulePage.endDateTextBoxButton().click();

		assert(getDriver().findElement(By.cssSelector(".pd-wizard")).getText().contains("End date cannot be before the start date"));
		assertThat("Continue button should be disabled", schedulePage.isAttributePresent(schedulePage.continueButton(SchedulePage.WizardStep.START_END), "disabled"));

		schedulePage.endDateTextBoxButton().click();
		datePicker = body.getDatePicker();
		datePicker.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 4));
		assertEquals(pattern.split(schedulePage.endDateTextBox().getAttribute("value"))[0], schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 4)));
		String endDate = schedulePage.endDateTextBox().getAttribute("value");
		schedulePage.endDateTextBoxButton().click();

		assert(!getDriver().findElement(By.cssSelector(".pd-wizard")).getText().contains("End date cannot be before the start date"));
		assertThat("Continue button should be enabled", !schedulePage.isAttributePresent(schedulePage.continueButton(SchedulePage.WizardStep.START_END), "disabled"));

		schedulePage.startDateTextBoxButton().click();
		datePicker = body.getDatePicker();
		datePicker.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 9));
		assertEquals(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0], schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 9)));
		schedulePage.startDateTextBoxButton().click();

		assert(getDriver().findElement(By.cssSelector(".pd-wizard")).getText().contains("End date cannot be before the start date"));
		assertThat("Continue button should be disabled", schedulePage.isAttributePresent(schedulePage.continueButton(SchedulePage.WizardStep.START_END), "disabled"));

		schedulePage.startDateTextBoxButton().click();
		datePicker = body.getDatePicker();
		datePicker.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 2));
		assertEquals(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0], schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 2)));
		schedulePage.startDateTextBoxButton().click();

		assert(!getDriver().findElement(By.cssSelector(".pd-wizard")).getText().contains("End date cannot be before the start date"));
		assertThat("Continue button should be enabled", !schedulePage.isAttributePresent(schedulePage.continueButton(SchedulePage.WizardStep.START_END), "disabled"));

		String startDate = schedulePage.startDateTextBox().getAttribute("value");
		schedulePage.continueButton(SchedulePage.WizardStep.START_END).click();
		schedulePage.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText().contains("Do you want to repeat this promotion schedule?"));

		schedulePage.repeatWithFrequencyBelow().click();
		schedulePage.selectFrequency(SchedulePage.Frequency.YEARLY);
		assertEquals(schedulePage.readFrequency(), "Yearly");

		schedulePage.continueButton(SchedulePage.WizardStep.FREQUENCY).click();
		schedulePage.loadOrFadeWait();
		schedulePage.never().click();
		schedulePage.finishButton(SchedulePage.WizardStep.FINAL).click();
		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(promotionsPage.backButton()));
		assertThat("Correct promotion summary text not present", promotionsPage.getText().contains("The promotion is scheduled to run starting on " + SchedulePage.parseDateForPromotionsPage(startDate) + " for the duration of 2 days, ending on " + SchedulePage.parseDateForPromotionsPage(endDate)));
		assertThat("Correct promotion summary text not present", promotionsPage.getText().contains("This promotion schedule will run yearly forever."));

		topNavBar.search("chips");
		topNavBar.loadOrFadeWait();
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));
		assertThat("promotions aren't scheduled to be shown now", !searchPage.isPromotionsBoxVisible());

		navBar.switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage.getPromotionLinkWithTitleContaining("chips").click();
		promotionsPage.schedulePromotion();
		schedulePage = body.getSchedulePage();
		schedulePage.loadOrFadeWait();
		assertThat("Schedule should be selected due to prepopulated schedule", schedulePage.schedule().getAttribute("class").contains("progressive-disclosure-selection"));

		schedulePage.schedule().click();
		schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
		schedulePage.loadOrFadeWait();

		assertEquals(startDate, schedulePage.startDateTextBox().getAttribute("value"));
		assertEquals(endDate, schedulePage.endDateTextBox().getAttribute("value"));
		schedulePage.startDateTextBoxButton().click();
		datePicker = body.getDatePicker();
		datePicker.calendarDateSelect(schedulePage.getTodayDate());
		schedulePage.startDateTextBoxButton().click();
		assertEquals(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0], schedulePage.dateAsString(schedulePage.getTodayDate()));

		startDate = schedulePage.startDateTextBox().getAttribute("value");
		endDate = schedulePage.endDateTextBox().getAttribute("value");

		schedulePage.continueButton(SchedulePage.WizardStep.START_END).click();
		schedulePage.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText().contains("Do you want to repeat this promotion schedule?"));

		schedulePage.selectFrequency(SchedulePage.Frequency.YEARLY);
		assertEquals(schedulePage.readFrequency(), "Yearly");

		schedulePage.continueButton(SchedulePage.WizardStep.FREQUENCY).click();
		schedulePage.loadOrFadeWait();
		schedulePage.runThisPromotionScheduleUntilTheDateBelow().click();
		schedulePage.finalDateTextBoxButton().click();
		datePicker = body.getDatePicker();
		datePicker.calendarDateSelect(DateUtils.addYears(schedulePage.getTodayDate(), 2));
		schedulePage.finalDateTextBoxButton().click();
		final String finalDate = schedulePage.finalDateTextBox().getAttribute("value");

		schedulePage.finishButton(SchedulePage.WizardStep.FINAL).click();
		schedulePage.loadOrFadeWait();
		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(promotionsPage.backButton()));
		assertThat("Correct schedule summary text not visible", promotionsPage.getText().contains("The promotion is scheduled to run starting on " + SchedulePage.parseDateForPromotionsPage(startDate) + " for the duration of 4 days, ending on " + SchedulePage.parseDateForPromotionsPage(endDate)));
		assertThat("Correct schedule summary text not visible", promotionsPage.getText().contains("This promotion schedule will run yearly until " + SchedulePage.parseDateForPromotionsPage(finalDate)));

		topNavBar.search("chips");
		topNavBar.loadOrFadeWait();
		searchPage = body.getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));
		assertThat("promotions are scheduled to be shown now but are not visible", searchPage.isPromotionsBoxVisible());
	}

	@Test
	public void testResetTimeAndDate() {
		setUpANewMultiDocPromotion("Korean", "한국", "Hotwire", "Korea", 4);
		promotionsPage.schedulePromotion();
		try {
			schedulePage = body.getSchedulePage();
		} catch (final NoSuchElementException e) {
			fail("Schedule Page has not loaded");
		}
		schedulePage.schedule().click();
		schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
		schedulePage.loadOrFadeWait();
		schedulePage.startDateTextBoxButton().click();
		datePicker = body.getDatePicker();
		datePicker.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 9));
		assertEquals(schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 9)), pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0]);

		datePicker.resetDateToToday();
		assertEquals(schedulePage.dateAndTimeAsString(schedulePage.getTodayDate()), schedulePage.startDateTextBox().getAttribute("value"));
	}

	@Test
	public void testTextInputToCalendar() {
		setUpANewMultiDocPromotion("Korean", "한국", "Hotwire", "Korea", 4);
		promotionsPage.schedulePromotion();
		try {
			schedulePage = body.getSchedulePage();
		} catch (final NoSuchElementException e) {
			fail("Schedule Page has not loaded");
		}
		schedulePage.schedule().click();
		schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
		schedulePage.loadOrFadeWait();
		schedulePage.startDateTextBox().clear();
		topNavBar.sideBarToggle();
		assertThat("continue button should be disabled", schedulePage.isAttributePresent(schedulePage.continueButton(SchedulePage.WizardStep.START_END), "disabled"));

		schedulePage.startDateTextBoxButton().click();
		datePicker = body.getDatePicker();
		datePicker.calendarDateSelect(schedulePage.getTodayDate());
		schedulePage.startDateTextBoxButton().click();
		schedulePage.startDateTextBox().sendKeys("Hello!!");
		topNavBar.sideBarToggle();
		assertEquals(schedulePage.dateAsString(schedulePage.getTodayDate()), pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0]);

		schedulePage.startDateTextBox().sendKeys(Keys.BACK_SPACE);
		topNavBar.sideBarToggle();
		assertEquals(schedulePage.dateAsString(schedulePage.getTodayDate()), pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0]);

		schedulePage.sendBackspaceToWebElement(schedulePage.startDateTextBox(), 16);
		schedulePage.startDateTextBox().sendKeys("30/02/2019 11:20");
		topNavBar.sideBarToggle();
		assertEquals(schedulePage.dateAsString(schedulePage.getTodayDate()), pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0]);

		schedulePage.sendBackspaceToWebElement(schedulePage.startDateTextBox(), 16);
		schedulePage.startDateTextBox().sendKeys("10/13/2019 11:20");
		topNavBar.sideBarToggle();
		assertEquals(schedulePage.dateAsString(schedulePage.getTodayDate()), pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0]);

		schedulePage.sendBackspaceToWebElement(schedulePage.startDateTextBox(), 16);
		schedulePage.startDateTextBox().sendKeys("02/02/2019 24:20");
		topNavBar.sideBarToggle();
		assertEquals(schedulePage.dateAsString(schedulePage.getTodayDate()), pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0]);

		schedulePage.sendBackspaceToWebElement(schedulePage.startDateTextBox(), 16);
		schedulePage.startDateTextBox().sendKeys("02/02/2019 22:61");
		topNavBar.sideBarToggle();
		assertEquals(schedulePage.dateAsString(schedulePage.getTodayDate()), pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0]);
	}

	@Test
	public void testIncrementDecrementTimeOnCalendar() {
		setUpANewMultiDocPromotion("Kazakh", "Қазақстан", "Sponsored", "Kaz", 5);
		promotionsPage.schedulePromotion();
		try {
			schedulePage = body.getSchedulePage();
		} catch (final NoSuchElementException e) {
			fail("Schedule Page has not loaded");
		}
		schedulePage.loadOrFadeWait();
		schedulePage.schedule().click();
		schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
		schedulePage.loadOrFadeWait();
		schedulePage.endDateTextBoxButton().click();
		datePicker = body.getDatePicker();
		datePicker.togglePicker();
		datePicker.loadOrFadeWait();
		datePicker.timePickerHour().click();
		datePicker.selectTimePickerHour(5);
		datePicker.loadOrFadeWait();
		assertEquals("05", datePicker.timePickerHour().getText());

		datePicker.incrementHours();
		datePicker.incrementHours();
		assertEquals("07", datePicker.timePickerHour().getText());

		for (int i = 1; i <= 10; i++) {
			datePicker.decrementHours();
		}
		assertEquals("21", datePicker.timePickerHour().getText());

		for (int i = 1; i <= 4; i++) {
			datePicker.incrementHours();
		}
		assertEquals("01", datePicker.timePickerHour().getText());

		datePicker.timePickerMinute().click();
		datePicker.selectTimePickerMinute(50);
		datePicker.loadOrFadeWait();
		assertEquals("50", datePicker.timePickerMinute().getText());

		datePicker.incrementMinutes();
		datePicker.incrementMinutes();
		assertEquals("52", datePicker.timePickerMinute().getText());

		for (int i = 1; i <= 10; i++) {
			datePicker.incrementMinutes();
		}
		assertEquals("02", datePicker.timePickerMinute().getText());

		for (int i = 1; i <= 5; i++) {
			datePicker.decrementMinutes();
		}
		assertEquals("57", datePicker.timePickerMinute().getText());
	}

	@Test
	public void testPromotionIsPrepopulated() {
		setUpANewMultiDocPromotion("Korean", "한국", "Hotwire", "Korea", 4);
		promotionsPage.schedulePromotion();
		try {
			schedulePage = body.getSchedulePage();
		} catch (final NoSuchElementException e) {
			fail("Schedule Page has not loaded");
		}

		final Date startDate = DateUtils.addDays(schedulePage.getTodayDate(), 4);
		final Date endDate = DateUtils.addDays(schedulePage.getTodayDate(), 8);
		final Date finalDate = DateUtils.addMonths(schedulePage.getTodayDate(), 6);
		schedulePage.schedulePromotion(startDate, endDate, SchedulePage.Frequency.MONTHLY, finalDate);

		promotionsPage.schedulePromotion();
		promotionsPage.loadOrFadeWait();
		schedulePage = body.getSchedulePage();
		assertThat("Due to pre-population 'schedule' should be pre-selected", schedulePage.schedule().getAttribute("class").contains("progressive-disclosure-selection"));

		schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
		schedulePage.loadOrFadeWait();
		assertEquals(schedulePage.dateAsString(startDate), pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0]);
		assertEquals(schedulePage.dateAsString(endDate), pattern.split(schedulePage.endDateTextBox().getAttribute("value"))[0]);

		schedulePage.continueButton(SchedulePage.WizardStep.START_END).click();
		schedulePage.loadOrFadeWait();
		assertEquals("Monthly", schedulePage.readFrequency());
		assertThat("Due to pre-population 'repeat with frequency below' should be pre-selected", schedulePage.getFirstChild(schedulePage.repeatWithFrequencyBelow()).getAttribute("class").contains("progressive-disclosure-selection"));

		schedulePage.continueButton(SchedulePage.WizardStep.FREQUENCY).click();
		schedulePage.loadOrFadeWait();
		assertThat("Due to pre-population 'run this promotion schedule until the date below' should be pre-selected", schedulePage.getFirstChild(schedulePage.runThisPromotionScheduleUntilTheDateBelow()).getAttribute("class").contains("progressive-disclosure-selection"));
		assertEquals(schedulePage.dateAsString( finalDate), pattern.split(schedulePage.finalDateTextBox().getAttribute("value"))[0]);

		schedulePage.finishButton(SchedulePage.WizardStep.FINAL).click();
		promotionsPage.loadOrFadeWait();
	}

	@Test
	public void testFrequencyPeriodNotLessThanPromotionLengthAndFinalDateNotLessThanEndDate() {
		setUpANewPromotion("Georgian", "საქართველო", "Hotwire", "Georgia");

		promotionsPage.schedulePromotion();
		try {
			schedulePage = body.getSchedulePage();
		} catch (final NoSuchElementException e) {
			fail("Schedule Page has not loaded");
		}
		schedulePage.navigateWizardAndSetEndDate(schedulePage.getTodayDate());

		List<String> availableFrequencies = schedulePage.getAvailableFrequencies();
		assertThat("All frequencies should be available", availableFrequencies.containsAll(Arrays.asList("Yearly", "Daily", "Monthly", "Weekly")));

		schedulePage.continueButton(SchedulePage.WizardStep.FREQUENCY).click();
		schedulePage.loadOrFadeWait();
		schedulePage.runThisPromotionScheduleUntilTheDateBelow().click();
		assertEquals(pattern.split(schedulePage.dateAndTimeAsString(schedulePage.getTodayDate()))[0], pattern.split(schedulePage.finalDateTextBox().getAttribute("value"))[0]);

		schedulePage.finishButton(SchedulePage.WizardStep.FINAL).click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.schedulePromotion();
		schedulePage = body.getSchedulePage();
		schedulePage.navigateWizardAndSetEndDate(DateUtils.addDays(schedulePage.getTodayDate(), 4));

		availableFrequencies = schedulePage.getAvailableFrequencies();
		assertThat("Yearly should be an available option", availableFrequencies.contains("Yearly"));
		assertThat("Daily should not be an option for this schedule", !availableFrequencies.contains("Daily"));
		assertThat("Weekly should be an option for this schedule", availableFrequencies.contains("Weekly"));
		assertThat("Monthly should be an option for this schedule", availableFrequencies.contains("Monthly"));

		schedulePage.continueButton(SchedulePage.WizardStep.FREQUENCY).click();
		schedulePage.loadOrFadeWait();
		schedulePage.runThisPromotionScheduleUntilTheDateBelow().click();
		assertEquals(pattern.split(schedulePage.dateAndTimeAsString(DateUtils.addDays(schedulePage.getTodayDate(), 4)))[0], pattern.split(schedulePage.finalDateTextBox().getAttribute("value"))[0]);

		schedulePage.finishButton(SchedulePage.WizardStep.FINAL).click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.schedulePromotion();
		schedulePage = body.getSchedulePage();
		schedulePage.navigateWizardAndSetEndDate(DateUtils.addWeeks(schedulePage.getTodayDate(), 2));

		availableFrequencies = schedulePage.getAvailableFrequencies();
		assertThat("Yearly should be an available option", availableFrequencies.contains("Yearly"));
		assertThat("Daily should not be an option for this schedule", !availableFrequencies.contains("Daily"));
		assertThat("Weekly should not be an option for this schedule", !availableFrequencies.contains("Weekly"));
		assertThat("Monthly should be an option for this schedule", availableFrequencies.contains("Monthly"));

		schedulePage.continueButton(SchedulePage.WizardStep.FREQUENCY).click();
		schedulePage.loadOrFadeWait();
		schedulePage.runThisPromotionScheduleUntilTheDateBelow().click();
		assertEquals(pattern.split(schedulePage.dateAndTimeAsString(DateUtils.addWeeks(schedulePage.getTodayDate(), 2)))[0], pattern.split(schedulePage.finalDateTextBox().getAttribute("value"))[0]);

		schedulePage.finishButton(SchedulePage.WizardStep.FINAL).click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.schedulePromotion();
		schedulePage = body.getSchedulePage();
		schedulePage.navigateWizardAndSetEndDate(DateUtils.addMonths(schedulePage.getTodayDate(), 1));

		availableFrequencies = schedulePage.getAvailableFrequencies();
		assertThat("Yearly should be an available option", availableFrequencies.contains("Yearly"));
		assertThat("Daily should not be an option for this schedule", !availableFrequencies.contains("Daily"));
		assertThat("Weekly should not be an option for this schedule", !availableFrequencies.contains("Weekly"));
		assertThat("Monthly should not be an option for this schedule", !availableFrequencies.contains("Monthly"));

		schedulePage.continueButton(SchedulePage.WizardStep.FREQUENCY).click();
		schedulePage.loadOrFadeWait();
		schedulePage.runThisPromotionScheduleUntilTheDateBelow().click();
		assertEquals(pattern.split(schedulePage.dateAndTimeAsString(DateUtils.addMonths(schedulePage.getTodayDate(), 1)))[0], pattern.split(schedulePage.finalDateTextBox().getAttribute("value"))[0]);

		schedulePage.finishButton(SchedulePage.WizardStep.FINAL).click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.schedulePromotion();
		schedulePage = body.getSchedulePage();
		schedulePage.loadOrFadeWait();
		schedulePage.schedule().click();
		schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
		schedulePage.loadOrFadeWait();
		schedulePage.endDateTextBoxButton().click();
		final DatePicker datePicker = new DatePicker(schedulePage.$el(), getDriver());
		datePicker.calendarDateSelect(DateUtils.addYears(schedulePage.getTodayDate(), 3));
		schedulePage.endDateTextBoxButton().click();
		assertThat("Finish button should be displayed when schedule period greater than one year", schedulePage.finishButton(SchedulePage.WizardStep.START_END).isDisplayed());
		schedulePage.finishButton(SchedulePage.WizardStep.START_END).click();
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
		setUpANewDynamicPromotion("Afrikaans", "hond", "pooch hond wolf", "Hotwire");
		promotionsPage.backButton().click();
		setUpANewDynamicPromotion("Afrikaans", "hond", "lupo wolf", "Sponsored");
		promotionsPage.backButton().click();
		promotionsPage.loadOrFadeWait();
		assertEquals(7, promotionsPage.promotionsList().size());

		List<WebElement> promotions = promotionsPage.promotionsList();
		for (int i = 0; i < promotions.size() - 1; i++) {
			assertTrue("Following promotions not in alphabetical order: " + promotions.get(i).getText().toLowerCase().split("\\n")[1] + ", " + promotions.get(i + 1).getText().toLowerCase().split("\\n")[1], promotions.get(i).getText().toLowerCase().split("\\n")[1].compareTo(promotions.get(i + 1).getText().toLowerCase().split("\\n")[1]) <= 0);
		}

		promotionsPage.getPromotionLinkWithTitleContaining(promotions.get(3).getText().split("\\n")[1]).click();
		promotionsPage.createNewTitle("aaa");
		promotionsPage.loadOrFadeWait();
		promotionsPage.backButton().click();
		promotionsPage.loadOrFadeWait();

		final List<WebElement> promotionsAgain = promotionsPage.promotionsList();
		for (int i = 0; i < promotionsAgain.size() - 1; i++) {
			assertTrue("Following promotions not in alphabetical order: " + promotionsAgain.get(i).getText().toLowerCase().split("\\n")[1] + ", " + promotionsAgain.get(i + 1).getText().toLowerCase().split("\\n")[1], promotionsAgain.get(i).getText().split("\\n")[1].toLowerCase().compareTo(promotionsAgain.get(i + 1).getText().split("\\n")[1].toLowerCase()) <= 0);
		}

		promotionsPage.getPromotionLinkWithTitleContaining(promotions.get(3).getText().split("\\n")[1]).click();
		promotionsPage.createNewTitle(promotions.get(3).getText());
		promotionsPage.loadOrFadeWait();
		promotionsPage.backButton().click();
		promotionsPage.loadOrFadeWait();

		promotionsPage.promotionsSearchFilter().sendKeys("dog");
		assertEquals(1, promotionsPage.promotionsList().size());

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("wolf");
		assertEquals(2, promotionsPage.promotionsList().size());

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("pooch");
		assertEquals(3, promotionsPage.promotionsList().size());
		promotions = promotionsPage.promotionsList();
		for (int i = 0; i < promotions.size() - 1; i++) {
			assertTrue("Following promotions not in alphabetical order: " + promotions.get(i).getText().split("\\n")[1] + ", " + promotions.get(i + 1).getText().split("\\n")[1], promotions.get(i).getText().split("\\n")[1].toLowerCase().compareTo(promotions.get(i + 1).getText().split("\\n")[1].toLowerCase()) <= 0);
		}

		promotionsPage.getPromotionLinkWithTitleContaining("hound").click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.createNewTitle("hound");
		assertThat("title has not changed", promotionsPage.getPromotionTitle().equals("hound"));
		promotionsPage.backButton().click();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("pooch");
		assertEquals(3, promotionsPage.promotionsList().size());

		promotionsPage.getPromotionLinkWithTitleContaining("hound").click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.removeSearchTrigger("pooch");
		assertThat("trigger not removed", !promotionsPage.getSearchTriggersList().contains("pooch"));
		promotionsPage.backButton().click();

		promotionsPage.clearPromotionsSearchFilter();
		promotionsPage.promotionsSearchFilter().sendKeys("pooch");
		assertEquals(2, promotionsPage.promotionsList().size());

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
		promotionsPage.selectPromotionsCategoryFilter("Dynamic Spotlight");
		promotionsPage.promotionsSearchFilter().sendKeys("Rhodesian");
		assertEquals("Filter should have returned one document", 1, promotionsPage.promotionsList().size());

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
		setUpANewMultiDocPinToPositionPromotion("Swahili", "mbwa", "swahili woof", 3);
		assertEquals("Swahili", promotionsPage.getLanguage());

		promotionsPage.backButton().click();
		setUpANewDynamicPromotion("Afrikaans", "pooch", "hond wolf", "Hotwire");
		assertEquals("Afrikaans", promotionsPage.getLanguage());
	}

	@Test
	public void testEditDynamicQuery() throws InterruptedException {
		topNavBar.search("kitty");
		searchPage = body.getSearchPage();
		searchPage.selectLanguage("French", getConfig().getType().getName());
		final String firstSearchResult = searchPage.getSearchResult(1).getText();
		final String secondSearchResult = setUpANewDynamicPromotion("French", "chat", "Meow", "Top Promotions");
		promotionsPage.addSearchTrigger("purrr");
		assertFalse("Inconsistent changing of case by create promotions wizard", promotionsPage.getSearchTriggersList().contains("meow"));
		promotionsPage.removeSearchTrigger("Meow");
		topNavBar.search("purrr");
		searchPage.selectLanguage("French", getConfig().getType().getName());
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
		searchPage.selectLanguage("French", getConfig().getType().getName());
		assertEquals(firstSearchResult, searchPage.promotionsSummaryList(false).get(0));

		getDriver().navigate().refresh();
		searchPage = body.getSearchPage();
		searchPage.loadOrFadeWait();
		assertEquals(firstSearchResult, searchPage.promotionsSummaryList(false).get(0));
	}

	@Test
	public void testPromotionCreationAndDeletionOnSecondWindow() {
		setUpANewPromotion("French", "chien", "Hotwire", "woof bark");

		final String url = getDriver().getCurrentUrl();
		final List<String> browserHandles = promotionsPage.createAndListWindowHandles();

		getDriver().switchTo().window(browserHandles.get(1));
		getDriver().get(url);
		final AppBody secondBody = new AppBody(getDriver());
		final PromotionsPage secondPromotionsPage = secondBody.getPromotionsPage();
		secondPromotionsPage.loadOrFadeWait();
		assertThat("Haven't navigated to promotions menu", secondPromotionsPage.newPromotionButton().isDisplayed());

		getDriver().switchTo().window(browserHandles.get(0));
		promotionsPage = body.getPromotionsPage();
		promotionsPage.loadOrFadeWait();
		setUpANewDynamicPromotion("Swahili", "rafiki", "friend", "Sponsored");

		getDriver().switchTo().window(browserHandles.get(1));
		assertEquals(2, secondPromotionsPage.promotionsList().size());

		getDriver().switchTo().window(browserHandles.get(0));
		promotionsPage.deletePromotion();

		getDriver().switchTo().window(browserHandles.get(1));
		assertEquals(1, secondPromotionsPage.promotionsList().size());

		secondPromotionsPage.getPromotionLinkWithTitleContaining("woof").click();
		secondPromotionsPage.deletePromotion();

		getDriver().switchTo().window(browserHandles.get(0));
		assertThat("Promotion not deleted", promotionsPage.getText().contains("There are no promotions..."));
	}

	@Test
	public void testPromotionFieldTextRestriction() {
		setUpANewPromotion("English","hot", "Hotwire", "hot");

		promotionsPage.addFieldText("MATCH{hot}:DRECONTENT");

		topNavBar.search("hot");
		searchPage.selectLanguage("English", getConfig().getType().getName());
		assertThat("Promoted Document should be visible", searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("hot pot");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("hots");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		navBar.switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage.getPromotionLinkWithTitleContaining("hot").click();
		promotionsPage.loadOrFadeWait();

		promotionsPage.fieldTextRemoveButton().click();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(promotionsPage.fieldTextAddButton()));

		topNavBar.search("hot");
		searchPage.loadOrFadeWait();
		assertThat("Promoted Document should be visible", searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("hot chocolate");
		assertThat("Promoted Document should be visible", searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("hots");
		assertThat("Promoted Document should be visible", searchPage.promotionsSummary().isDisplayed());

		navBar.switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage.getPromotionLinkWithTitleContaining("hot").click();
		promotionsPage.loadOrFadeWait();

		promotionsPage.fieldTextAddButton().click();
		promotionsPage.fieldTextInputBox().sendKeys("<h1>hi</h1>");
		promotionsPage.fieldTextTickConfirmButton().click();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(promotionsPage.fieldTextRemoveButton()));
		assertEquals("<h1>hi</h1>", promotionsPage.fieldTextValue());

		promotionsPage.fieldTextEditButton().click();
		promotionsPage.fieldTextInputBox().clear();
		promotionsPage.fieldTextInputBox().sendKeys("MATCH{hot dog}:DRECONTENT");
		promotionsPage.fieldTextTickConfirmButton().click();
		promotionsPage.loadOrFadeWait();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(promotionsPage.fieldTextRemoveButton()));

		topNavBar.search("hot dog");
		searchPage.loadOrFadeWait();
		assertThat("Promoted Document should be visible", searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("hot chocolate");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("hot");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("dog");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("hot dogs");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());
	}

	@Test
	public void testPromotionFieldTextOrRestriction() {
		setUpANewPromotion("English","road", "Hotwire", "highway street");

		promotionsPage.addFieldText("MATCH{highway}:DRECONTENT OR MATCH{street}:DRECONTENT");

		topNavBar.search("highway street");
		searchPage.selectLanguage("English", getConfig().getType().getName());
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("road");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("ROAD");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("highway");
		assertThat("Promoted documents are not visible", searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("street");
		assertThat("Promoted documents are not visible", searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("highway street");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("street highway");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("street street");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("highwaystreet");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		topNavBar.search("highway AND street");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());
	}

	@Test
	public void testFieldTextSubmitTextOnEnter() {
		setUpANewPromotion("English","road", "Hotwire", "highway street");

		promotionsPage.fieldTextAddButton().click();
		promotionsPage.loadOrFadeWait();
		promotionsPage.fieldTextInputBox().sendKeys("TEST");
		promotionsPage.fieldTextInputBox().sendKeys(Keys.RETURN);
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(promotionsPage.fieldTextRemoveButton()));
		assertTrue("Field text cannot be submitted with an enter key", promotionsPage.fieldTextRemoveButton().isDisplayed());
	}

	@Test
	public void testCreateFieldTextField() {
		setUpANewPromotion("Telugu","మింగ్ వంశము", "Top Promotions", "Ming");

		promotionsPage.addFieldText("MATCH{Richard}:NAME");
		topNavBar.search("Ming");
		assertThat("Promoted Document should not be visible", !searchPage.promotionsSummary().isDisplayed());

		searchPage.expandFilter(SearchBase.Filter.FIELD_TEXT);
		searchPage.fieldTextAddButton().click();
		searchPage.fieldTextInput().sendKeys("MATCH{Richard}:NAME");
		searchPage.fieldTextTickConfirm().click();
		searchPage.loadOrFadeWait();
		assertThat("Promoted Document should be visible", searchPage.promotionsSummary().isDisplayed());
	}

	@Test
	public void testCountSearchResultsWithPinToPositionInjected() {
		setUpANewMultiDocPinToPositionPromotion("French", "Lyon", "boeuf frites orange", 13);
		for (final String query : Arrays.asList("boeuf", "frites", "orange")) {
			topNavBar.search(query);
			searchPage.selectLanguage("French", getConfig().getType().getName());
			final int initialStatedNumberOfResults = searchPage.countSearchResults();
			searchPage.forwardToLastPageButton().click();
			searchPage.loadOrFadeWait();
			final int numberOfPages = searchPage.getCurrentPageNumber();
			final int lastPageDocumentsCount = searchPage.visibleDocumentsCount();
			assertEquals((numberOfPages - 1) * 6 + lastPageDocumentsCount, searchPage.countSearchResults());
			assertEquals(initialStatedNumberOfResults, searchPage.countSearchResults());
		}
	}
}
