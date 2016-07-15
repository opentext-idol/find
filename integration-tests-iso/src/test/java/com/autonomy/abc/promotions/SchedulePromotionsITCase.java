package com.autonomy.abc.promotions;

import com.autonomy.abc.base.IdolIsoTestBase;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.SearchPage;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.element.DatePicker;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.urlContains;
import static java.util.Collections.sort;
import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Matchers.displayed;

public class SchedulePromotionsITCase extends IdolIsoTestBase {

	public SchedulePromotionsITCase(final TestConfig config) {
		super(config);
	}

	private SearchPage searchPage;
	private SchedulePage schedulePage;
    private PromotionService promotionService;
	private SchedulePromotionService schedulePromotionService;

	@Before
	public void setUp() {
		schedulePromotionService = getApplication().schedulePromotionService();
        promotionService = getApplication().promotionService();
		promotionService.deleteAll();
	}

	private void setUpPromotion(final Promotion promotion, final Query search, final int numberOfDocs){
		promotionService.setUpPromotion(promotion,search,numberOfDocs);
		promotionService.goToDetails(promotion);
		schedulePage = schedulePromotionService.goToSchedule();
	}

	private void setUpKoreaSpotlight(){
		final SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "Korea".toLowerCase());  //ON PREM ONLY ALLOWS LOWER CASE SEARCH TRIGGERS
		setUpPromotion(spotlight, new Query("한국").withFilter(new LanguageFilter(Language.KOREAN)), 4);
		schedulePage.schedule().click();
		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
	}

	@Test
	public void testPromotionShowingAndFollowingSchedule() {
		final SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "\"ice cream\" chips");
		setUpPromotion(spotlight, new Query("wizard"), 4);

		final Date start = schedulePage.todayIncrementedByDays(4);
		final Date end = schedulePage.todayIncrementedByDays(10);

		promoteAndCheckSummary(start, end, 6);
		verifyThat("Promotions aren't scheduled to be shown now", searchPage.isPromotionsBoxVisible(), is(false));

		promotionService.goToDetails("chips");
		getElementFactory().getPromotionsDetailPage().schedulePromotion();
		schedulePage = getElementFactory().getSchedulePage();
		final Date today = schedulePage.getTodayDate();

		promoteAndCheckSummary(today, end, 10);
		verifyThat("Promotions scheduled to be shown now and visible", searchPage.isPromotionsBoxVisible(), is(true));
	}

	private void promoteAndCheckSummary(final Date start, final Date end, final int duration){
		schedulePromotionService.schedulePromotion(start,end, SchedulePage.Frequency.YEARLY);
		final PromotionsDetailPage promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
		final String startDate =  schedulePage.parseDateObjectToPromotions(start.toString());
		final String endDate =  schedulePage.parseDateObjectToPromotions(end.toString());

		verifyThat("Summary text present: duration", promotionsDetailPage.getText(), containsString("The promotion is scheduled to run starting on " +startDate+ " for the duration of "+duration+" days, ending on " +endDate+ '.'));
		verifyThat("Summary text present: recurrence", promotionsDetailPage.getText(), containsString("This promotion schedule will run yearly forever."));

		getElementFactory().getTopNavBar().search("chips");
		Waits.loadOrFadeWait();
		searchPage = getElementFactory().getSearchPage();
		searchPage.waitForPromotionsLoadIndicatorToDisappear();
		Waits.loadOrFadeWait();
	}

	@Test
	public void testNavigateScheduleWizard(){
		final SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "\"ice cream\" chips");
		setUpPromotion(spotlight, new Query("wizard"), 4);

		verifyThat(getWindow(), urlContains("schedule"));
		verifyThat("Correct wizard text", schedulePage.getText(), containsString("Schedule your promotion"));
		verifyThat("Always active is selected", schedulePage.optionSelected(schedulePage.alwaysActive()));
		verifyThat("Schedule isn't selected", !schedulePage.optionSelected(schedulePage.schedule()));
		verifyThat("Finish button enabled", !schedulePage.buttonDisabled(schedulePage.finishButton()));

		scheduleClickAndCheck();

		schedulePage.alwaysActive().click();
		Waits.loadOrFadeWait();
		verifyThat("Finish button should be enabled", !schedulePage.buttonDisabled(schedulePage.finishButton()));

		scheduleClickAndCheck();

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		verifyThat("Correct wizard text", schedulePage.getText(), containsString("How long should this promotion run?"));

		verifyThat(schedulePage.startDate(), is(schedulePage.todayDateString()));
		final Date correctEndDate = schedulePage.todayIncrementedByDays(1);
		verifyThat(schedulePage.endDate(), is(schedulePage.dateAsString(correctEndDate)));
		verifyThat(schedulePage.time(schedulePage.startDateTextBox()), is("00:00"));
		verifyThat(schedulePage.time(schedulePage.endDateTextBox()), is("00:00"));

		DatePicker datePicker = schedulePromotionService.openDatePicker(schedulePage.startDateCalendar());
		verifyThat(datePicker.getSelectedDayOfMonth(), is(schedulePage.getDay(0)));
		verifyThat(datePicker.getSelectedMonth(), is(schedulePage.getMonth(0)));
		schedulePage.startDateCalendar().click();

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		verifyThat("Correct wizard text", schedulePage.getText(), containsString("Do you want to repeat this promotion schedule?"));

		schedulePage.repeatWithFrequencyBelow().click();
		schedulePage.selectFrequency(SchedulePage.Frequency.YEARLY);
		verifyThat(schedulePage.readFrequency(), is("YEARLY"));

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		verifyThat("Correct wizard text", schedulePage.getText(), containsString("When should this promotion schedule finish?"));

		schedulePage.runThisPromotionScheduleUntilTheDateBelow().click();
		verifyThat(schedulePage.finalDate(), is(schedulePage.dateAsString(correctEndDate)));

		datePicker = schedulePromotionService.openDatePicker(schedulePage.finalDateCalendar());
		verifyThat(datePicker.getSelectedDayOfMonth(), is(schedulePage.getDay(1)));
		verifyThat(datePicker.getSelectedMonth(), is(schedulePage.getMonth(1)));

		final Date newFinalDate = schedulePage.todayIncrementedByDays(502);
		datePicker.calendarDateSelect(newFinalDate);
		verifyThat(schedulePage.date(schedulePage.finalDateTextBox()), is(schedulePage.dateAsString(newFinalDate)));

		schedulePage.finalDateCalendar().click();
		Waits.loadOrFadeWait();
		schedulePage.finishButton().click();

        final IdolPromotionsDetailPage promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
		verifyThat("Returned to promotions detail page", promotionsDetailPage.promotionTitle().getValue(),is("Spotlight for: chips, ice cream"));
	}

	private void scheduleClickAndCheck(){
		schedulePage.schedule().click();
		Waits.loadOrFadeWait();
		verifyThat(schedulePage.continueButton(), displayed());
	}

	@Test
	public void testScheduleStartBeforeEnd() {
		final SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "\"ice cream\" chips");
		setUpPromotion(spotlight, new Query("wizard"), 4);
		schedulePromotionService.navigateToScheduleDuration();

		schedulePromotionService.setStartDate(3);
		schedulePromotionService.setEndDate(2);
		checkDatesNotOkay();

		schedulePromotionService.setEndDate(4);
		checkDatesOkay();

		schedulePromotionService.setStartDate(9);
		checkDatesNotOkay();

		schedulePromotionService.setStartDate(2);
		checkDatesOkay();
	}

	private void checkDatesOkay(){
		verifyThat("No error message",schedulePage.helpMessages(), not(hasItem("End date cannot be before the start date")));
		verifyThat("Continue button should be enabled", !schedulePage.buttonDisabled(schedulePage.continueButton()));
	}
	private void checkDatesNotOkay(){
		verifyThat(schedulePage.helpMessages(), hasItem("End date cannot be before the start date"));
		verifyThat("Continue button should be disabled",schedulePage.buttonDisabled(schedulePage.continueButton()));
	}

	@Test
	public void testResetTimeAndDate() {
		setUpKoreaSpotlight();

		final Date startDate = schedulePage.todayIncrementedByDays(9);
		schedulePromotionService.scheduleDurationSelector(schedulePage.startDateCalendar(),startDate);
		verifyThat(schedulePage.dateAsString(startDate), is(schedulePage.startDate()));

		schedulePromotionService.resetDateToToday(schedulePage.startDateCalendar());
		verifyThat(schedulePage.dateAndTimeAsString(schedulePage.getTodayDate()), is(schedulePage.dateText(schedulePage.startDateTextBox())));
	}

	@Test
	public void testTextInputToCalendar() {
		setUpKoreaSpotlight();

		schedulePage.startDateTextBox().clear();
		verifyThat("Continue button disabled", schedulePage.buttonDisabled(schedulePage.continueButton()));

		schedulePromotionService.scheduleDurationSelector(schedulePage.startDateCalendar(),schedulePage.getTodayDate());
		final String today = schedulePage.dateAsString(schedulePage.getTodayDate());

		setStartDate(schedulePage.getTodayDate()+"Hello!!");
		getElementFactory().getSideNavBar().toggle();
		verifyThat(today, is(schedulePage.startDate()));

		schedulePage.startDateTextBox().sendKeys(Keys.BACK_SPACE);
		getElementFactory().getSideNavBar().toggle();
		verifyThat(today, is(schedulePage.startDate()));

		final List<String> startDates = Arrays.asList("30/02/2019 11:20","10/13/2019 11:20","02/02/2019 24:20","02/02/2019 22:61");

		for(final String date:startDates) {
			setStartDate(date);
			getElementFactory().getSideNavBar().toggle();
			verifyThat(today, is(schedulePage.startDate()));
		}
	}

	private void setStartDate(final String timestamp) {
		for (int i = 0; i < 16; i++) {
            schedulePage.startDateTextBox().sendKeys(Keys.BACK_SPACE);
		}
		schedulePage.startDateTextBox().sendKeys(timestamp);
	}

	@Test
	public void testIncrementDecrementTimeOnCalendar() {
		setUpKoreaSpotlight();
		Waits.loadOrFadeWait();

		final DatePicker datePicker = schedulePromotionService.openDatePicker(schedulePage.endDateCalendar());
		datePicker.togglePicker();
		Waits.loadOrFadeWait();

		//hours
		datePicker.timePickerHour().click();
		datePicker.selectTimePickerHour(5);
		Waits.loadOrFadeWait();
		verifyThat("05", is(datePicker.timePickerHour().getText()));

		datePicker.incrementHours();
		datePicker.incrementHours();
		verifyThat("07", is(datePicker.timePickerHour().getText()));

		for (int i = 1; i <= 10; i++) {
			datePicker.decrementHours();
		}
		verifyThat("21", is(datePicker.timePickerHour().getText()));

		for (int i = 1; i <= 4; i++) {
			datePicker.incrementHours();
		}
		verifyThat("01", is(datePicker.timePickerHour().getText()));

		//minutes
		datePicker.timePickerMinute().click();
		datePicker.selectTimePickerMinute(50);
		Waits.loadOrFadeWait();
		verifyThat("50", is(datePicker.timePickerMinute().getText()));

		datePicker.incrementMinutes();
		datePicker.incrementMinutes();
		verifyThat("52", is(datePicker.timePickerMinute().getText()));

		for (int i = 1; i <= 10; i++) {
			datePicker.incrementMinutes();
		}
		verifyThat("02", is(datePicker.timePickerMinute().getText()));

		for (int i = 1; i <= 5; i++) {
			datePicker.decrementMinutes();
		}
		verifyThat("57", is(datePicker.timePickerMinute().getText()));
	}

	@Test
	public void testPromotionIsPrepopulated() {
		final SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "Korea".toLowerCase());  //ON PREM ONLY ALLOWS LOWER CASE SEARCH TRIGGERS
		setUpPromotion(spotlight, new Query("한국").withFilter(new LanguageFilter(Language.KOREAN)), 4);

		final Date startDate = schedulePage.todayIncrementedByDays(4);
		final Date endDate = schedulePage.todayIncrementedByDays(8);
		final Date finalDate = DateUtils.addMonths(schedulePage.getTodayDate(), 6);
		schedulePromotionService.schedulePromotion(startDate, endDate, SchedulePage.Frequency.MONTHLY, finalDate);

		getElementFactory().getPromotionsDetailPage().schedulePromotion();
		Waits.loadOrFadeWait();
		schedulePage = getElementFactory().getSchedulePage();
		verifyThat("Due to pre-population 'schedule' should be pre-selected", schedulePage.optionSelected(schedulePage.schedule()));

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		verifyThat(schedulePage.dateAsString(startDate), is(schedulePage.startDate()));
		verifyThat(schedulePage.dateAsString(endDate), is(schedulePage.endDate()));

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		verifyThat(schedulePage.readFrequency(),equalToIgnoringCase("MONTHLY"));
		verifyThat("Due to pre-population 'repeat with frequency below' should be pre-selected", schedulePage.optionSelected(schedulePage.repeatWithFrequencyBelow()));

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		verifyThat("Due to pre-population 'run this promotion schedule until the date below' should be pre-selected", schedulePage.optionSelected(schedulePage.runThisPromotionScheduleUntilTheDateBelow()));
		verifyThat(schedulePage.dateAsString(finalDate), is(schedulePage.finalDate()));

		schedulePage.finishButton().click();
		Waits.loadOrFadeWait();
	}

	@Test
	public void testFrequencyPeriodNotLessThanPromotionLengthAndFinalDateNotLessThanEndDate() {
		final SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "Georgia".toLowerCase());  //ON PREM ONLY ALLOWS LOWER CASE SEARCH TRIGGERS
		setUpPromotion(spotlight, new Query("საქართველო").withFilter(new LanguageFilter(Language.GEORGIAN)), 4);

		setEndDateCheckFrequencies(schedulePage.getTodayDate(),Arrays.asList("Yearly","Daily", "Monthly", "Weekly"));
		getElementFactory().getPromotionsDetailPage().schedulePromotion();

		setEndDateCheckFrequencies(schedulePage.todayIncrementedByDays(4),Arrays.asList("Yearly","Monthly", "Weekly"));
		getElementFactory().getPromotionsDetailPage().schedulePromotion();

		setEndDateCheckFrequencies(DateUtils.addWeeks(schedulePage.getTodayDate(), 2),Arrays.asList("Yearly","Monthly"));
		getElementFactory().getPromotionsDetailPage().schedulePromotion();

		setEndDateCheckFrequencies(DateUtils.addMonths(schedulePage.getTodayDate(), 1), Collections.singletonList("Yearly"));
	}

	private void setEndDateCheckFrequencies(final Date endDate, final List<String> correctFrequencyOptions){
		schedulePage = getElementFactory().getSchedulePage();
		schedulePromotionService.navigateWizardAndSetEndDate(endDate);

		final List<String> availableFrequencies = schedulePage.getAvailableFrequencies();
		verifyThat("Correct number frequency options available",availableFrequencies,hasSize(correctFrequencyOptions.size()));

		if(correctFrequencyOptions.size()>1) {
			sort(correctFrequencyOptions);
			sort(availableFrequencies);
		}
		verifyThat("Available frequencies are correct",availableFrequencies.equals(correctFrequencyOptions));

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		schedulePage.runThisPromotionScheduleUntilTheDateBelow().click();
		verifyThat(schedulePage.dateAndTimeAsString(endDate), is(schedulePage.dateText(schedulePage.finalDateTextBox())));

		schedulePage.finishButton().click();
		Waits.loadOrFadeWait();

	}
}
