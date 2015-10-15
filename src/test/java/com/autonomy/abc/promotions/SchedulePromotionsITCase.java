package com.autonomy.abc.promotions;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.element.DatePicker;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.promotions.PromotionsPage;
import com.autonomy.abc.selenium.page.promotions.SchedulePage;
import com.autonomy.abc.selenium.page.search.SearchPage;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SchedulePromotionsITCase extends ABCTestBase {

	public SchedulePromotionsITCase(final TestConfig config, final String browser, final ApplicationType appType, final Platform platform) {
		super(config, browser, appType, platform);
	}
	private TopNavBar topNavBar;
	private PromotionsPage promotionsPage;
	private SearchPage searchPage;
	private SchedulePage schedulePage;
	private DatePicker datePicker;
	private final Pattern pattern = Pattern.compile("\\s+");

	@Before
	public void setUp() throws MalformedURLException {
		topNavBar = body.getTopNavBar();
		promotionsPage = getElementFactory().getPromotionsPage();
		promotionsPage.deleteAllPromotions();
	}

	@Test
	public void testSchedulePromotionForTomorrow() throws ParseException {
		promotionsPage.setUpANewMultiDocPromotion("English", "wizard", "Sponsored", "wand magic spells", 4, getConfig().getType().getName());
		promotionsPage.schedulePromotion();

		try {
			schedulePage = (SchedulePage) getElementFactory().getSchedulePage();
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
		searchPage = getElementFactory().getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));
		assertThat("promotions aren't scheduled to be shown now", !searchPage.isPromotionsBoxVisible());

		navBar.switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage.getPromotionLinkWithTitleContaining("magic").click();
		promotionsPage.schedulePromotion();
		promotionsPage.loadOrFadeWait();
		schedulePage = (SchedulePage) getElementFactory().getSchedulePage();
		schedulePage.alwaysActive().click();
		schedulePage.finishButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
		schedulePage.loadOrFadeWait();
		new WebDriverWait(getDriver(), 8).until(ExpectedConditions.visibilityOf(promotionsPage.backButton()));
		assert(promotionsPage.getText().contains("The promotion is always active"));

		topNavBar.search("magic");
		topNavBar.loadOrFadeWait();
		searchPage = getElementFactory().getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));
		assertThat("promotions are scheduled to be shown now", searchPage.isPromotionsBoxVisible());
	}

	@Test
	public void testScheduleStartBeforeEnd() throws ParseException {
		promotionsPage.setUpANewMultiDocPromotion("English", "cone", "Hotwire", "\"ice cream\" chips", 4, getConfig().getType().getName());
		promotionsPage.schedulePromotion();

		try {
			schedulePage = (SchedulePage) getElementFactory().getSchedulePage();
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
		searchPage = getElementFactory().getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));
		assertThat("promotions aren't scheduled to be shown now", !searchPage.isPromotionsBoxVisible());

		navBar.switchPage(NavBarTabId.PROMOTIONS);
		promotionsPage.getPromotionLinkWithTitleContaining("chips").click();
		promotionsPage.schedulePromotion();
		schedulePage = (SchedulePage) getElementFactory().getSchedulePage();
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
		searchPage = getElementFactory().getSearchPage();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.docLogo()));
		assertThat("promotions are scheduled to be shown now but are not visible", searchPage.isPromotionsBoxVisible());
	}

	@Test
	public void testResetTimeAndDate() {
		promotionsPage.setUpANewMultiDocPromotion("Korean", "한국", "Hotwire", "Korea", 4, getConfig().getType().getName());
		promotionsPage.schedulePromotion();
		try {
			schedulePage = (SchedulePage) getElementFactory().getSchedulePage();
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
		promotionsPage.setUpANewMultiDocPromotion("Korean", "한국", "Hotwire", "Korea", 4, getConfig().getType().getName());
		promotionsPage.schedulePromotion();
		try {
			schedulePage = (SchedulePage) getElementFactory().getSchedulePage();
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
		promotionsPage.setUpANewMultiDocPromotion("Kazakh", "Қазақстан", "Sponsored", "Kaz", 5, getConfig().getType().getName());
		promotionsPage.schedulePromotion();
		try {
			schedulePage = (SchedulePage) getElementFactory().getSchedulePage();
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
		promotionsPage.setUpANewMultiDocPromotion("Korean", "한국", "Hotwire", "Korea", 4, getConfig().getType().getName());
		promotionsPage.schedulePromotion();
		try {
			schedulePage = (SchedulePage) getElementFactory().getSchedulePage();
		} catch (final NoSuchElementException e) {
			fail("Schedule Page has not loaded");
		}

		final Date startDate = DateUtils.addDays(schedulePage.getTodayDate(), 4);
		final Date endDate = DateUtils.addDays(schedulePage.getTodayDate(), 8);
		final Date finalDate = DateUtils.addMonths(schedulePage.getTodayDate(), 6);
		schedulePage.schedulePromotion(startDate, endDate, SchedulePage.Frequency.MONTHLY, finalDate);

		promotionsPage.schedulePromotion();
		promotionsPage.loadOrFadeWait();
		schedulePage = (SchedulePage) getElementFactory().getSchedulePage();
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
		promotionsPage.setUpANewPromotion("Georgian", "საქართველო", "Hotwire", "Georgia", getConfig().getType().getName());

		promotionsPage.schedulePromotion();
		try {
			schedulePage = (SchedulePage) getElementFactory().getSchedulePage();
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
		schedulePage = (SchedulePage) getElementFactory().getSchedulePage();
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
		schedulePage = (SchedulePage) getElementFactory().getSchedulePage();
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
		schedulePage = (SchedulePage) getElementFactory().getSchedulePage();
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
		schedulePage = (SchedulePage) getElementFactory().getSchedulePage();
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
}
