package com.autonomy.abc.promotions;

import com.autonomy.abc.base.SOTestBase;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.element.DatePicker;
import com.autonomy.abc.selenium.iso.IdolIsoElementFactory;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.SearchPage;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.urlContains;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.openqa.selenium.lift.Matchers.displayed;

public class SchedulePromotionsITCase extends SOTestBase {

	public SchedulePromotionsITCase(final TestConfig config) {
		super(config);
	}

	private SearchPage searchPage;
	private SchedulePage schedulePage;
	private DatePicker datePicker;
	private final Pattern pattern = Pattern.compile("\\s+");
    private PromotionService promotionService;
	private IdolIsoElementFactory elementFactory;

	@Before
	public void setUp() throws MalformedURLException, InterruptedException {
        promotionService = getApplication().promotionService();

		IdolPromotionsPage promotionsPage = (IdolPromotionsPage) promotionService.deleteAll();
		elementFactory = (IdolIsoElementFactory) getElementFactory();
	}

	@Test
	public void testSchedulePromotionForTomorrow() throws ParseException {
		SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.SPONSORED, "wand magic spells");
        promotionService.setUpPromotion(spotlight, "wizard", 4);
		promotionService.goToDetails(spotlight);
		IdolPromotionsDetailPage promotionsDetailPage = elementFactory.getPromotionsDetailPage();
		promotionsDetailPage.schedulePromotion();

		try {
			schedulePage = elementFactory.getSchedulePage();
		} catch (final NoSuchElementException e) {
			fail("Schedule Page has not loaded");
		}

		assertThat(getWindow(), urlContains("schedule"));
		assertThat("Wrong wizard text", schedulePage.getText().contains("Schedule your promotion"));
		assertThat(schedulePage.finishButton(SchedulePage.WizardStep.ENABLE_SCHEDULE), displayed());
		assertThat("Always active isn't selected", schedulePage.alwaysActive().getAttribute("class"), containsString("progressive-disclosure-selection"));
		assertThat("Schedule shouldn't be selected", schedulePage.schedule().getAttribute("class"), not(containsString("progressive-disclosure-selection")));
		assertThat("Finish button should be enabled", ElementUtil.isAttributePresent(schedulePage.finishButton(SchedulePage.WizardStep.ENABLE_SCHEDULE), "disabled"), is(false));

		schedulePage.alwaysActive().click();
		assertThat("Finish button should be enabled", ElementUtil.isAttributePresent(schedulePage.finishButton(SchedulePage.WizardStep.ENABLE_SCHEDULE), "disabled"), is(false));

		schedulePage.schedule().click();
		assertThat(schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE), displayed());

		schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
		Waits.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText(), containsString("How long should this promotion run?"));

		assertThat(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0], is(schedulePage.dateAsString(schedulePage.getTodayDate())));
		assertThat(pattern.split(schedulePage.endDateTextBox().getAttribute("value"))[0], is(schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 1))));
		assertThat(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[1], is("00:00"));
		assertThat(pattern.split(schedulePage.endDateTextBox().getAttribute("value"))[1], is("00:00"));

		schedulePage.startDateTextBoxButton().click();
		datePicker = new DatePicker(schedulePage.$el(),getDriver());
		assertThat(datePicker.getSelectedDayOfMonth(), is(schedulePage.getDay(0)));
		assertThat(datePicker.getSelectedMonth(), is(schedulePage.getMonth(0)));

		datePicker.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 1));
		assertThat(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0], is(schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 1))));
		final String startDate = SchedulePage.parseDateForPromotionsPage(schedulePage.startDateTextBox().getAttribute("value"));
		schedulePage.startDateTextBoxButton().click();
		Waits.loadOrFadeWait();

		schedulePage.endDateTextBoxButton().click();
		datePicker = new DatePicker(schedulePage.$el(),getDriver());
		datePicker.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 5));
		assertThat(pattern.split(schedulePage.endDateTextBox().getAttribute("value"))[0], is(schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 5))));
		final String endDate = SchedulePage.parseDateForPromotionsPage(schedulePage.endDateTextBox().getAttribute("value"));
		schedulePage.endDateTextBoxButton().click();
		Waits.loadOrFadeWait();

		schedulePage.continueButton(SchedulePage.WizardStep.START_END).click();
		Waits.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText(), containsString("Do you want to repeat this promotion schedule?"));

		schedulePage.repeatWithFrequencyBelow().click();
		schedulePage.selectFrequency(SchedulePage.Frequency.MONTHLY);
		assertEquals(schedulePage.readFrequency(), "Monthly");

		schedulePage.continueButton(SchedulePage.WizardStep.FREQUENCY).click();
		Waits.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText(), containsString("When should this promotion schedule finish?"));

		schedulePage.runThisPromotionScheduleUntilTheDateBelow().click();
		assertThat(pattern.split(schedulePage.finalDateTextBox().getAttribute("value"))[0], is(schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 5))));

		schedulePage.finalDateTextBoxButton().click();
		datePicker = new DatePicker(schedulePage.$el(),getDriver());
		assertThat(datePicker.getSelectedDayOfMonth(), is(schedulePage.getDay(5)));
		assertThat(datePicker.getSelectedMonth(), is(schedulePage.getMonth(5)));

		datePicker.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 502));
		assertThat(pattern.split(schedulePage.finalDateTextBox().getAttribute("value"))[0], is(schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 502))));
		Waits.loadOrFadeWait();
		datePicker.timePickerHour().click();
		datePicker.selectTimePickerHour(3);
		datePicker.timePickerMinute().click();
		datePicker.selectTimePickerMinute(42);
		assertThat(pattern.split(schedulePage.finalDateTextBox().getAttribute("value"))[1], is("03:40"));

		datePicker.setMinuteUsingIncrementDecrement(42);
		assertThat(pattern.split(schedulePage.finalDateTextBox().getAttribute("value"))[1], is("03:42"));

		final String finalDate = SchedulePage.parseDateForPromotionsPage(schedulePage.finalDateTextBox().getAttribute("value"));
		schedulePage.finalDateTextBoxButton().click();
		Waits.loadOrFadeWait();

		schedulePage.finishButton(SchedulePage.WizardStep.FINAL).click();
		promotionsDetailPage = elementFactory.getPromotionsDetailPage();
		assertThat("Correct Scheduling text not visible", promotionsDetailPage.getText(), containsString("The promotion is scheduled to run starting on " + startDate + " for the duration of 4 days, ending on " + endDate));
		assertThat("Correct Scheduling text not visible", promotionsDetailPage.getText(), containsString("This promotion schedule will run monthly until " + finalDate));

		getElementFactory().getTopNavBar().search("magic");
		Waits.loadOrFadeWait();
		searchPage = getElementFactory().getSearchPage();
		assertThat("promotions aren't scheduled to be shown now", searchPage.isPromotionsBoxVisible(), is(false));

		promotionService.goToDetails("magic");
		promotionsDetailPage = elementFactory.getPromotionsDetailPage();
		promotionsDetailPage.schedulePromotion();
		Waits.loadOrFadeWait();
		schedulePage = elementFactory.getSchedulePage();
		schedulePage.alwaysActive().click();
		schedulePage.finishButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
		Waits.loadOrFadeWait();
		promotionsDetailPage = elementFactory.getPromotionsDetailPage();
		assert(promotionsDetailPage.getText().contains("The promotion is always active"));

		getElementFactory().getTopNavBar().search("magic");
		Waits.loadOrFadeWait();
		searchPage = getElementFactory().getSearchPage();
		assertThat("promotions are scheduled to be shown now", searchPage.isPromotionsBoxVisible(), is(true));
	}

	@Test
	public void testScheduleStartBeforeEnd() throws ParseException {
		SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "\"ice cream\" chips");
		promotionService.setUpPromotion(spotlight, "wizard", 4);
		promotionService.goToDetails(spotlight);
		elementFactory.getPromotionsDetailPage().schedulePromotion();

		try {
			schedulePage = elementFactory.getSchedulePage();
		} catch (final NoSuchElementException e) {
			fail("Schedule Page has not loaded");
		}
		assertThat(getWindow(), urlContains("schedule"));
		assertThat("Wrong wizard text", schedulePage.getText(), containsString("Schedule your promotion"));
		assertThat(schedulePage.finishButton(SchedulePage.WizardStep.ENABLE_SCHEDULE), displayed());
		assertThat("Finish button should be enabled", ElementUtil.isAttributePresent(schedulePage.finishButton(SchedulePage.WizardStep.ENABLE_SCHEDULE), "disabled"), is(false));

		schedulePage.alwaysActive().click();
		Waits.loadOrFadeWait();
		assertThat("Finish button should be enabled", ElementUtil.isAttributePresent(schedulePage.finishButton(SchedulePage.WizardStep.ENABLE_SCHEDULE), "disabled"), is(false));

		schedulePage.schedule().click();
		Waits.loadOrFadeWait();
		assertThat(schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE), displayed());

		schedulePage.alwaysActive().click();
		Waits.loadOrFadeWait();
		assertThat("Finish button should be enabled", ElementUtil.isAttributePresent(schedulePage.finishButton(SchedulePage.WizardStep.ENABLE_SCHEDULE), "disabled"), is(false));

		schedulePage.schedule().click();
		Waits.loadOrFadeWait();
		assertThat(schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE), displayed());

		schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
		Waits.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText(), containsString("How long should this promotion run?"));

		assertThat(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0], is(schedulePage.dateAsString(schedulePage.getTodayDate())));
		assertThat(pattern.split(schedulePage.endDateTextBox().getAttribute("value"))[0], is(schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 1))));

		schedulePage.startDateTextBoxButton().click();
		datePicker = new DatePicker(schedulePage.$el(),getDriver());
		assertThat(datePicker.getSelectedDayOfMonth(), is(schedulePage.getDay(0)));
		assertThat(datePicker.getSelectedMonth(), is(schedulePage.getMonth(0)));

		datePicker.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 3));
		assertThat(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0], is(schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 3))));
		schedulePage.startDateTextBoxButton().click();

		schedulePage.endDateTextBoxButton().click();
		datePicker = new DatePicker(schedulePage.$el(),getDriver());
		datePicker.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 2));
		assertThat(pattern.split(schedulePage.endDateTextBox().getAttribute("value"))[0], is(schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 2))));
		schedulePage.endDateTextBoxButton().click();

		assert(getDriver().findElement(By.cssSelector(".pd-wizard")).getText().contains("End date cannot be before the start date"));
		assertThat("Continue button should be disabled", ElementUtil.isAttributePresent(schedulePage.continueButton(SchedulePage.WizardStep.START_END), "disabled"), is(true));

		schedulePage.endDateTextBoxButton().click();
		datePicker = new DatePicker(schedulePage.$el(),getDriver());
		datePicker.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 4));
		assertThat(pattern.split(schedulePage.endDateTextBox().getAttribute("value"))[0], is(schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 4))));
		String endDate = schedulePage.endDateTextBox().getAttribute("value");
		schedulePage.endDateTextBoxButton().click();

		assert(!getDriver().findElement(By.cssSelector(".pd-wizard")).getText().contains("End date cannot be before the start date"));
		assertThat("Continue button should be enabled", ElementUtil.isAttributePresent(schedulePage.continueButton(SchedulePage.WizardStep.START_END), "disabled"), is(false));

		schedulePage.startDateTextBoxButton().click();
		datePicker = new DatePicker(schedulePage.$el(),getDriver());
		datePicker.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 9));
		assertThat(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0], is(schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 9))));
		schedulePage.startDateTextBoxButton().click();

		assert(getDriver().findElement(By.cssSelector(".pd-wizard")).getText().contains("End date cannot be before the start date"));
		assertThat("Continue button should be disabled", ElementUtil.isAttributePresent(schedulePage.continueButton(SchedulePage.WizardStep.START_END), "disabled"), is(true));

		schedulePage.startDateTextBoxButton().click();
		datePicker = new DatePicker(schedulePage.$el(),getDriver());
		datePicker.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 2));
		assertThat(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0], is(schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 2))));
		schedulePage.startDateTextBoxButton().click();

		assert(!getDriver().findElement(By.cssSelector(".pd-wizard")).getText().contains("End date cannot be before the start date"));
		assertThat("Continue button should be enabled", ElementUtil.isAttributePresent(schedulePage.continueButton(SchedulePage.WizardStep.START_END), "disabled"), is(false));

		String startDate = schedulePage.startDateTextBox().getAttribute("value");
		schedulePage.continueButton(SchedulePage.WizardStep.START_END).click();
		Waits.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText(), containsString("Do you want to repeat this promotion schedule?"));

		schedulePage.repeatWithFrequencyBelow().click();
		schedulePage.selectFrequency(SchedulePage.Frequency.YEARLY);
		assertThat(schedulePage.readFrequency(), is("Yearly"));

		schedulePage.continueButton(SchedulePage.WizardStep.FREQUENCY).click();
		Waits.loadOrFadeWait();
		schedulePage.never().click();
		schedulePage.finishButton(SchedulePage.WizardStep.FINAL).click();
		PromotionsDetailPage promotionsDetailPage = elementFactory.getPromotionsDetailPage();
		assertThat("Correct promotion summary text not present", promotionsDetailPage.getText(), containsString("The promotion is scheduled to run starting on " + SchedulePage.parseDateForPromotionsPage(startDate) + " for the duration of 2 days, ending on " + SchedulePage.parseDateForPromotionsPage(endDate)));
		assertThat("Correct promotion summary text not present", promotionsDetailPage.getText(), containsString("This promotion schedule will run yearly forever."));

		getElementFactory().getTopNavBar().search("chips");
		Waits.loadOrFadeWait();
		searchPage = getElementFactory().getSearchPage();
		assertThat("promotions aren't scheduled to be shown now", searchPage.isPromotionsBoxVisible(), is(false));

		promotionService.goToDetails("chips");
		elementFactory.getPromotionsDetailPage().schedulePromotion();
		schedulePage = elementFactory.getSchedulePage();
		Waits.loadOrFadeWait();
		assertThat("Schedule should be selected due to prepopulated schedule", schedulePage.schedule().getAttribute("class"), containsString("progressive-disclosure-selection"));

		schedulePage.schedule().click();
		schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
		Waits.loadOrFadeWait();

		assertThat(startDate, is(schedulePage.startDateTextBox().getAttribute("value")));
		assertThat(endDate, is(schedulePage.endDateTextBox().getAttribute("value")));
		schedulePage.startDateTextBoxButton().click();
		datePicker = new DatePicker(schedulePage.$el(),getDriver());
		datePicker.calendarDateSelect(schedulePage.getTodayDate());
		schedulePage.startDateTextBoxButton().click();
		assertThat(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0], is(schedulePage.dateAsString(schedulePage.getTodayDate())));

		startDate = schedulePage.startDateTextBox().getAttribute("value");
		endDate = schedulePage.endDateTextBox().getAttribute("value");

		schedulePage.continueButton(SchedulePage.WizardStep.START_END).click();
		Waits.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText(), containsString("Do you want to repeat this promotion schedule?"));

		schedulePage.selectFrequency(SchedulePage.Frequency.YEARLY);
		assertThat(schedulePage.readFrequency(), is("Yearly"));

		schedulePage.continueButton(SchedulePage.WizardStep.FREQUENCY).click();
		Waits.loadOrFadeWait();
		schedulePage.runThisPromotionScheduleUntilTheDateBelow().click();
		schedulePage.finalDateTextBoxButton().click();
		datePicker = new DatePicker(schedulePage.$el(),getDriver());
		datePicker.calendarDateSelect(DateUtils.addYears(schedulePage.getTodayDate(), 2));
		schedulePage.finalDateTextBoxButton().click();
		final String finalDate = schedulePage.finalDateTextBox().getAttribute("value");

		schedulePage.finishButton(SchedulePage.WizardStep.FINAL).click();
		Waits.loadOrFadeWait();
		promotionsDetailPage = elementFactory.getPromotionsDetailPage();
		assertThat("Correct schedule summary text not visible", promotionsDetailPage.getText(), containsString("The promotion is scheduled to run starting on " + SchedulePage.parseDateForPromotionsPage(startDate) + " for the duration of 4 days, ending on " + SchedulePage.parseDateForPromotionsPage(endDate)));
		assertThat("Correct schedule summary text not visible", promotionsDetailPage.getText(), containsString("This promotion schedule will run yearly until " + SchedulePage.parseDateForPromotionsPage(finalDate)));

		getElementFactory().getTopNavBar().search("chips");
		Waits.loadOrFadeWait();
		searchPage = getElementFactory().getSearchPage();
		assertThat("promotions are scheduled to be shown now but are not visible", searchPage.isPromotionsBoxVisible(), is(true));
	}

	@Test
	public void testResetTimeAndDate() {
		SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "Korea".toLowerCase());  //ON PREM ONLY ALLOWS LOWER CASE SEARCH TRIGGERS
		promotionService.setUpPromotion(spotlight, new Query("한국").withFilter(new LanguageFilter(Language.KOREAN)), 4);
		promotionService.goToDetails(spotlight);
		elementFactory.getPromotionsDetailPage().schedulePromotion();
		try {
			schedulePage = elementFactory.getSchedulePage();
		} catch (final NoSuchElementException e) {
			fail("Schedule Page has not loaded");
		}
		schedulePage.schedule().click();
		schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
		Waits.loadOrFadeWait();
		schedulePage.startDateTextBoxButton().click();
		datePicker = new DatePicker(schedulePage.$el(),getDriver());
		datePicker.calendarDateSelect(DateUtils.addDays(schedulePage.getTodayDate(), 9));
		assertThat(schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 9)), is(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0]));

		datePicker.resetDateToToday();
		assertThat(schedulePage.dateAndTimeAsString(schedulePage.getTodayDate()), is(schedulePage.startDateTextBox().getAttribute("value")));
	}

	@Test
	public void testTextInputToCalendar() {
		SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "Korea".toLowerCase());  //ON PREM ONLY ALLOWS LOWER CASE SEARCH TRIGGERS
		promotionService.setUpPromotion(spotlight, new Query("한국").withFilter(new LanguageFilter(Language.KOREAN)), 4);
		promotionService.goToDetails(spotlight);
		elementFactory.getPromotionsDetailPage().schedulePromotion();
		try {
			schedulePage = elementFactory.getSchedulePage();
		} catch (final NoSuchElementException e) {
			fail("Schedule Page has not loaded");
		}
		schedulePage.schedule().click();
		schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
		Waits.loadOrFadeWait();
		schedulePage.startDateTextBox().clear();
		getElementFactory().getSideNavBar().toggle();
		assertThat("continue button should be disabled", ElementUtil.isAttributePresent(schedulePage.continueButton(SchedulePage.WizardStep.START_END), "disabled"), is(true));

		schedulePage.startDateTextBoxButton().click();
		datePicker = new DatePicker(schedulePage.$el(),getDriver());
		datePicker.calendarDateSelect(schedulePage.getTodayDate());
		schedulePage.startDateTextBoxButton().click();
		schedulePage.startDateTextBox().sendKeys("Hello!!");
		getElementFactory().getSideNavBar().toggle();
		assertThat(schedulePage.dateAsString(schedulePage.getTodayDate()), is(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0]));

		schedulePage.startDateTextBox().sendKeys(Keys.BACK_SPACE);
		getElementFactory().getSideNavBar().toggle();
		assertThat(schedulePage.dateAsString(schedulePage.getTodayDate()), is(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0]));

		setStartDate("30/02/2019 11:20");
		getElementFactory().getSideNavBar().toggle();
		assertThat(schedulePage.dateAsString(schedulePage.getTodayDate()), is(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0]));

        setStartDate("10/13/2019 11:20");
		getElementFactory().getSideNavBar().toggle();
		assertThat(schedulePage.dateAsString(schedulePage.getTodayDate()), is(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0]));

		setStartDate("02/02/2019 24:20");
		getElementFactory().getSideNavBar().toggle();
		assertThat(schedulePage.dateAsString(schedulePage.getTodayDate()), is(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0]));

        setStartDate("02/02/2019 22:61");
		getElementFactory().getSideNavBar().toggle();
		assertThat(schedulePage.dateAsString(schedulePage.getTodayDate()), is(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0]));
	}

	private void setStartDate(String timestamp) {
		for (int i = 0; i < 16; i++) {
            schedulePage.startDateTextBox().sendKeys(Keys.BACK_SPACE);
		}
		schedulePage.startDateTextBox().sendKeys(timestamp);
	}

	@Test
	public void testIncrementDecrementTimeOnCalendar() {
		SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.SPONSORED, "Kaz".toLowerCase());  //ON PREM ONLY ALLOWS LOWER CASE SEARCH TRIGGERS
		promotionService.setUpPromotion(spotlight, new Query("Қазақстан").withFilter(new LanguageFilter(Language.KAZAKH)), 5);
		promotionService.goToDetails(spotlight);
		elementFactory.getPromotionsDetailPage().schedulePromotion();
		try {
			schedulePage = elementFactory.getSchedulePage();
		} catch (final NoSuchElementException e) {
			fail("Schedule Page has not loaded");
		}
		Waits.loadOrFadeWait();
		schedulePage.schedule().click();
		schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
		Waits.loadOrFadeWait();
		schedulePage.endDateTextBoxButton().click();
		datePicker = new DatePicker(schedulePage.$el(),getDriver());
		datePicker.togglePicker();
		Waits.loadOrFadeWait();
		datePicker.timePickerHour().click();
		datePicker.selectTimePickerHour(5);
		Waits.loadOrFadeWait();
		assertThat("05", is(datePicker.timePickerHour().getText()));

		datePicker.incrementHours();
		datePicker.incrementHours();
		assertThat("07", is(datePicker.timePickerHour().getText()));

		for (int i = 1; i <= 10; i++) {
			datePicker.decrementHours();
		}
		assertThat("21", is(datePicker.timePickerHour().getText()));

		for (int i = 1; i <= 4; i++) {
			datePicker.incrementHours();
		}
		assertThat("01", is(datePicker.timePickerHour().getText()));

		datePicker.timePickerMinute().click();
		datePicker.selectTimePickerMinute(50);
		Waits.loadOrFadeWait();
		assertThat("50", is(datePicker.timePickerMinute().getText()));

		datePicker.incrementMinutes();
		datePicker.incrementMinutes();
		assertThat("52", is(datePicker.timePickerMinute().getText()));

		for (int i = 1; i <= 10; i++) {
			datePicker.incrementMinutes();
		}
		assertThat("02", is(datePicker.timePickerMinute().getText()));

		for (int i = 1; i <= 5; i++) {
			datePicker.decrementMinutes();
		}
		assertThat("57", is(datePicker.timePickerMinute().getText()));
	}

	@Test
	public void testPromotionIsPrepopulated() {
		SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "Korea".toLowerCase());  //ON PREM ONLY ALLOWS LOWER CASE SEARCH TRIGGERS
		promotionService.setUpPromotion(spotlight, new Query("한국").withFilter(new LanguageFilter(Language.KOREAN)), 4);
		promotionService.goToDetails(spotlight);
		elementFactory.getPromotionsDetailPage().schedulePromotion();
		try {
			schedulePage = elementFactory.getSchedulePage();
		} catch (final NoSuchElementException e) {
			fail("Schedule Page has not loaded");
		}

		final Date startDate = DateUtils.addDays(schedulePage.getTodayDate(), 4);
		final Date endDate = DateUtils.addDays(schedulePage.getTodayDate(), 8);
		final Date finalDate = DateUtils.addMonths(schedulePage.getTodayDate(), 6);
		schedulePage.schedulePromotion(startDate, endDate, SchedulePage.Frequency.MONTHLY, finalDate);

		elementFactory.getPromotionsDetailPage().schedulePromotion();
		Waits.loadOrFadeWait();
		schedulePage = elementFactory.getSchedulePage();
		assertThat("Due to pre-population 'schedule' should be pre-selected", schedulePage.schedule().getAttribute("class"),containsString("progressive-disclosure-selection"));

		schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
		Waits.loadOrFadeWait();
		assertThat(schedulePage.dateAsString(startDate), is(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0]));
		assertThat(schedulePage.dateAsString(endDate), is(pattern.split(schedulePage.endDateTextBox().getAttribute("value"))[0]));

		schedulePage.continueButton(SchedulePage.WizardStep.START_END).click();
		Waits.loadOrFadeWait();
		assertThat("Monthly", is(schedulePage.readFrequency()));
		assertThat("Due to pre-population 'repeat with frequency below' should be pre-selected", ElementUtil.getFirstChild(schedulePage.repeatWithFrequencyBelow()).getAttribute("class"),containsString("progressive-disclosure-selection"));

		schedulePage.continueButton(SchedulePage.WizardStep.FREQUENCY).click();
		Waits.loadOrFadeWait();
		assertThat("Due to pre-population 'run this promotion schedule until the date below' should be pre-selected", ElementUtil.getFirstChild(schedulePage.runThisPromotionScheduleUntilTheDateBelow()).getAttribute("class"),containsString("progressive-disclosure-selection"));
		assertThat(schedulePage.dateAsString(finalDate), is(pattern.split(schedulePage.finalDateTextBox().getAttribute("value"))[0]));

		schedulePage.finishButton(SchedulePage.WizardStep.FINAL).click();
		Waits.loadOrFadeWait();
	}

	@Test
	public void testFrequencyPeriodNotLessThanPromotionLengthAndFinalDateNotLessThanEndDate() {
		SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "Georgia".toLowerCase());  //ON PREM ONLY ALLOWS LOWER CASE SEARCH TRIGGERS
		promotionService.setUpPromotion(spotlight, new Query("საქართველო").withFilter(new LanguageFilter(Language.GEORGIAN)), 4);
		promotionService.goToDetails(spotlight);
		elementFactory.getPromotionsDetailPage().schedulePromotion();
		try {
			schedulePage = elementFactory.getSchedulePage();
		} catch (final NoSuchElementException e) {
			fail("Schedule Page has not loaded");
		}
		schedulePage.navigateWizardAndSetEndDate(schedulePage.getTodayDate());

		List<String> availableFrequencies = schedulePage.getAvailableFrequencies();
		assertThat("All frequencies should be available", availableFrequencies.containsAll(Arrays.asList("Yearly", "Daily", "Monthly", "Weekly")));

		schedulePage.continueButton(SchedulePage.WizardStep.FREQUENCY).click();
		Waits.loadOrFadeWait();
		schedulePage.runThisPromotionScheduleUntilTheDateBelow().click();
		assertEquals(pattern.split(schedulePage.dateAndTimeAsString(schedulePage.getTodayDate()))[0], pattern.split(schedulePage.finalDateTextBox().getAttribute("value"))[0]);

		schedulePage.finishButton(SchedulePage.WizardStep.FINAL).click();
		Waits.loadOrFadeWait();
		elementFactory.getPromotionsDetailPage().schedulePromotion();
		schedulePage = elementFactory.getSchedulePage();
		schedulePage.navigateWizardAndSetEndDate(DateUtils.addDays(schedulePage.getTodayDate(), 4));

		availableFrequencies = schedulePage.getAvailableFrequencies();
		assertThat("Yearly should be an available option", availableFrequencies.contains("Yearly"));
		assertThat("Daily should not be an option for this schedule", !availableFrequencies.contains("Daily"));
		assertThat("Weekly should be an option for this schedule", availableFrequencies.contains("Weekly"));
		assertThat("Monthly should be an option for this schedule", availableFrequencies.contains("Monthly"));

		schedulePage.continueButton(SchedulePage.WizardStep.FREQUENCY).click();
		Waits.loadOrFadeWait();
		schedulePage.runThisPromotionScheduleUntilTheDateBelow().click();
		assertEquals(pattern.split(schedulePage.dateAndTimeAsString(DateUtils.addDays(schedulePage.getTodayDate(), 4)))[0], pattern.split(schedulePage.finalDateTextBox().getAttribute("value"))[0]);

		schedulePage.finishButton(SchedulePage.WizardStep.FINAL).click();
		Waits.loadOrFadeWait();
		elementFactory.getPromotionsDetailPage().schedulePromotion();
		schedulePage = elementFactory.getSchedulePage();
		schedulePage.navigateWizardAndSetEndDate(DateUtils.addWeeks(schedulePage.getTodayDate(), 2));

		availableFrequencies = schedulePage.getAvailableFrequencies();
		assertThat("Yearly should be an available option", availableFrequencies.contains("Yearly"));
		assertThat("Daily should not be an option for this schedule", !availableFrequencies.contains("Daily"));
		assertThat("Weekly should not be an option for this schedule", !availableFrequencies.contains("Weekly"));
		assertThat("Monthly should be an option for this schedule", availableFrequencies.contains("Monthly"));

		schedulePage.continueButton(SchedulePage.WizardStep.FREQUENCY).click();
		Waits.loadOrFadeWait();
		schedulePage.runThisPromotionScheduleUntilTheDateBelow().click();
		assertEquals(pattern.split(schedulePage.dateAndTimeAsString(DateUtils.addWeeks(schedulePage.getTodayDate(), 2)))[0], pattern.split(schedulePage.finalDateTextBox().getAttribute("value"))[0]);

		schedulePage.finishButton(SchedulePage.WizardStep.FINAL).click();
		Waits.loadOrFadeWait();
		elementFactory.getPromotionsDetailPage().schedulePromotion();
		schedulePage = elementFactory.getSchedulePage();
		schedulePage.navigateWizardAndSetEndDate(DateUtils.addMonths(schedulePage.getTodayDate(), 1));

		availableFrequencies = schedulePage.getAvailableFrequencies();
		assertThat("Yearly should be an available option", availableFrequencies.contains("Yearly"));
		assertThat("Daily should not be an option for this schedule", !availableFrequencies.contains("Daily"));
		assertThat("Weekly should not be an option for this schedule", !availableFrequencies.contains("Weekly"));
		assertThat("Monthly should not be an option for this schedule", !availableFrequencies.contains("Monthly"));

		schedulePage.continueButton(SchedulePage.WizardStep.FREQUENCY).click();
		Waits.loadOrFadeWait();
		schedulePage.runThisPromotionScheduleUntilTheDateBelow().click();
		assertEquals(pattern.split(schedulePage.dateAndTimeAsString(DateUtils.addMonths(schedulePage.getTodayDate(), 1)))[0], pattern.split(schedulePage.finalDateTextBox().getAttribute("value"))[0]);

		schedulePage.finishButton(SchedulePage.WizardStep.FINAL).click();
		Waits.loadOrFadeWait();
		elementFactory.getPromotionsDetailPage().schedulePromotion();
		schedulePage = elementFactory.getSchedulePage();
		Waits.loadOrFadeWait();
		schedulePage.schedule().click();
		schedulePage.continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
		Waits.loadOrFadeWait();
		schedulePage.endDateTextBoxButton().click();
		final DatePicker datePicker = new DatePicker(schedulePage.$el(), getDriver());
		datePicker.calendarDateSelect(DateUtils.addYears(schedulePage.getTodayDate(), 3));
		schedulePage.endDateTextBoxButton().click();
		assertThat(schedulePage.finishButton(SchedulePage.WizardStep.START_END), displayed());
		schedulePage.finishButton(SchedulePage.WizardStep.START_END).click();
	}
}
