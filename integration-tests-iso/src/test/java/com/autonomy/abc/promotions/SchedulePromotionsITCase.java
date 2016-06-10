package com.autonomy.abc.promotions;

import com.autonomy.abc.base.IdolIsoTestBase;
import com.autonomy.abc.selenium.language.Language;
import com.autonomy.abc.selenium.promotions.*;
import com.autonomy.abc.selenium.query.LanguageFilter;
import com.autonomy.abc.selenium.query.Query;
import com.autonomy.abc.selenium.search.SearchPage;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.element.DatePicker;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
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
	private IdolPromotionsDetailPage promotionsDetailPage;
	private DatePicker datePicker;
	private final Pattern pattern = Pattern.compile("\\s+");
    private PromotionService promotionService;

	@Before
	public void setUp() throws MalformedURLException, InterruptedException {
        promotionService = getApplication().promotionService();
		promotionService.deleteAll();
	}

	private void setUpPromotion(Promotion promotion, Query search, int numberOfDocs){
		promotionService.setUpPromotion(promotion,search,numberOfDocs);
		promotionService.goToDetails(promotion);

		IdolPromotionsDetailPage promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
		promotionsDetailPage.schedulePromotion();
		schedulePage = getElementFactory().getSchedulePage();
	}

	private void setUpKoreaSpotlight(){
		SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "Korea".toLowerCase());  //ON PREM ONLY ALLOWS LOWER CASE SEARCH TRIGGERS
		setUpPromotion(spotlight, new Query("한국").withFilter(new LanguageFilter(Language.KOREAN)), 4);
		schedulePage.schedule().click();
		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
	}

	@Test
	public void testPromotionShowingAndFollowingSchedule() {
		SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "\"ice cream\" chips");
		setUpPromotion(spotlight, new Query("wizard"), 4);

		Date start = DateUtils.addDays(schedulePage.getTodayDate(), 4);
		Date end = DateUtils.addDays(schedulePage.getTodayDate(), 10);

		//should not be promoted now
		promoteAndCheckSummary(start, end, 6);
		assertThat("Promotions aren't scheduled to be shown now", searchPage.isPromotionsBoxVisible(), is(false));

		promotionService.goToDetails("chips");
		getElementFactory().getPromotionsDetailPage().schedulePromotion();
		schedulePage = getElementFactory().getSchedulePage();
		Date today = schedulePage.getTodayDate();

		promoteAndCheckSummary(today, end, 10);
		assertThat("Promotions scheduled to be shown now and visible", searchPage.isPromotionsBoxVisible(), is(true));
	}

	private void promoteAndCheckSummary(Date start, Date end,int duration){
		schedulePage.schedulePromotion(start,end, SchedulePage.Frequency.YEARLY);
		PromotionsDetailPage promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
		String startDate =  schedulePage.parseDateObjectToPromotions(start.toString());
		String endDate =  schedulePage.parseDateObjectToPromotions(end.toString());

		assertThat("Summary text present: duration", promotionsDetailPage.getText(), containsString("The promotion is scheduled to run starting on " +startDate+ " for the duration of "+duration+" days, ending on " +endDate+"."));
		assertThat("Summary text present: recurrence", promotionsDetailPage.getText(), containsString("This promotion schedule will run yearly forever."));

		getElementFactory().getTopNavBar().search("chips");
		Waits.loadOrFadeWait();
		searchPage = getElementFactory().getSearchPage();
		searchPage.waitForPromotionsLoadIndicatorToDisappear();
		Waits.loadOrFadeWait();
	}

	@Test
	public void testNavigateScheduleWizard(){
		SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "\"ice cream\" chips");
		setUpPromotion(spotlight, new Query("wizard"), 4);

		assertThat(getWindow(), urlContains("schedule"));
		assertThat("Correct wizard text", schedulePage.getText(), containsString("Schedule your promotion"));
		assertThat("Always active is selected", schedulePage.optionSelected(schedulePage.alwaysActive()));
		assertThat("Schedule isn't selected", !schedulePage.optionSelected(schedulePage.schedule()));
		assertThat("Finish button enabled", !schedulePage.buttonDisabled(schedulePage.finishButton()));

		scheduleClickAndCheck();

		schedulePage.alwaysActive().click();
		Waits.loadOrFadeWait();
		assertThat("Finish button should be enabled", !schedulePage.buttonDisabled(schedulePage.finishButton()));

		scheduleClickAndCheck();

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		assertThat("Correct wizard text", schedulePage.getText(), containsString("How long should this promotion run?"));

		assertThat(schedulePage.startDate(), is(schedulePage.todayDateString()));
		Date correctEndDate = DateUtils.addDays(schedulePage.getTodayDate(), 1);
		assertThat(schedulePage.endDate(), is(schedulePage.dateAsString(correctEndDate)));
		assertThat(schedulePage.time(schedulePage.startDateTextBox()), is("00:00"));
		assertThat(schedulePage.time(schedulePage.endDateTextBox()), is("00:00"));

		DatePicker datePicker = schedulePage.openDatePicker(schedulePage.startDateCalendar());
		assertThat(datePicker.getSelectedDayOfMonth(), is(schedulePage.getDay(0)));
		assertThat(datePicker.getSelectedMonth(), is(schedulePage.getMonth(0)));
		schedulePage.startDateCalendar().click();

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		assertThat("Correct wizard text", schedulePage.getText(), containsString("Do you want to repeat this promotion schedule?"));

		schedulePage.repeatWithFrequencyBelow().click();
		schedulePage.selectFrequency(SchedulePage.Frequency.YEARLY);
		assertThat(schedulePage.readFrequency(), is("YEARLY"));

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		assertThat("Correct wizard text", schedulePage.getText(), containsString("When should this promotion schedule finish?"));

		schedulePage.runThisPromotionScheduleUntilTheDateBelow().click();
		assertThat(schedulePage.finalDate(), is(schedulePage.dateAsString(correctEndDate)));

		datePicker = schedulePage.openDatePicker(schedulePage.finalDateCalendar());
		assertThat(datePicker.getSelectedDayOfMonth(), is(schedulePage.getDay(1)));
		assertThat(datePicker.getSelectedMonth(), is(schedulePage.getMonth(1)));

		Date newFinalDate = DateUtils.addDays(schedulePage.getTodayDate(), 502);
		datePicker.calendarDateSelect(newFinalDate);
		assertThat(schedulePage.date(schedulePage.finalDateTextBox()), is(schedulePage.dateAsString(newFinalDate)));

		schedulePage.finalDateCalendar().click();
		Waits.loadOrFadeWait();
		schedulePage.finishButton().click();

		promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
		assertThat("Returned to promotions detail page",promotionsDetailPage.promotionTitle().getValue(),is("Spotlight for: chips, ice cream"));
	}

	private void scheduleClickAndCheck(){
		schedulePage.schedule().click();
		Waits.loadOrFadeWait();
		assertThat(schedulePage.continueButton(), displayed());
	}

	@Test
	public void testScheduleStartBeforeEnd() {
		SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "\"ice cream\" chips");
		setUpPromotion(spotlight, new Query("wizard"), 4);
		schedulePage.navigateToScheduleDuration();

		schedulePage.setStartDate(3);
		schedulePage.setEndDate(2);
		checkDatesNotOkay();

		schedulePage.setEndDate(4);
		checkDatesOkay();

		schedulePage.setStartDate(9);
		checkDatesNotOkay();

		schedulePage.setStartDate(2);
		checkDatesOkay();
	}

	private void checkDatesOkay(){
		assertThat("No error message",schedulePage.helpMessages(), not(hasItem("End date cannot be before the start date")));
		assertThat("Continue button should be enabled", !schedulePage.buttonDisabled(schedulePage.continueButton()));
	}
	private void checkDatesNotOkay(){
		assertThat(schedulePage.helpMessages(), hasItem("End date cannot be before the start date"));
		assertThat("Continue button should be disabled",schedulePage.buttonDisabled(schedulePage.continueButton()));
	}

	@Test
	public void testResetTimeAndDate() {
		setUpKoreaSpotlight();

		Date startDate = DateUtils.addDays(schedulePage.getTodayDate(), 9);
		schedulePage.scheduleDurationSelector(schedulePage.startDateCalendar(),startDate);
		assertThat(schedulePage.dateAsString(startDate), is(schedulePage.startDate()));

		schedulePage.resetDateToToday(schedulePage.startDateCalendar());
		assertThat(schedulePage.dateAndTimeAsString(schedulePage.getTodayDate()), is(schedulePage.dateText(schedulePage.startDateTextBox())));
	}

	@Test
	public void testTextInputToCalendar() {
		setUpKoreaSpotlight();

		schedulePage.startDateTextBox().clear();
		assertThat("Continue button disabled", schedulePage.buttonDisabled(schedulePage.continueButton()));

		schedulePage.scheduleDurationSelector(schedulePage.startDateCalendar(),schedulePage.getTodayDate());
		String today = schedulePage.dateAsString(schedulePage.getTodayDate());

		setStartDate(schedulePage.getTodayDate()+"Hello!!");
		getElementFactory().getSideNavBar().toggle();
		assertThat(today, is(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0]));

		schedulePage.startDateTextBox().sendKeys(Keys.BACK_SPACE);
		getElementFactory().getSideNavBar().toggle();
		assertThat(today, is(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0]));

		List<String> startDates = Arrays.asList("30/02/2019 11:20","10/13/2019 11:20","02/02/2019 24:20","02/02/2019 22:61");

		for(String date:startDates) {
			setStartDate(date);
			getElementFactory().getSideNavBar().toggle();
			assertThat(today, is(pattern.split(schedulePage.startDateTextBox().getAttribute("value"))[0]));
		}
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
		setUpPromotion(spotlight, new Query("Қазақстан").withFilter(new LanguageFilter(Language.KAZAKH)), 5);
		schedulePage.navigateToScheduleDuration();
		Waits.loadOrFadeWait();

		//opening calendar
		DatePicker datePicker = schedulePage.openDatePicker(schedulePage.endDateCalendar());

		//open correct area
		datePicker.togglePicker();
		//goes to time from date

		//pick hour
		Waits.loadOrFadeWait();
		//clicks on the hour

		//hours
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

		//minutes
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
		setUpPromotion(spotlight, new Query("한국").withFilter(new LanguageFilter(Language.KOREAN)), 4);

		final Date startDate = DateUtils.addDays(schedulePage.getTodayDate(), 4);
		final Date endDate = DateUtils.addDays(schedulePage.getTodayDate(), 8);
		final Date finalDate = DateUtils.addMonths(schedulePage.getTodayDate(), 6);
		schedulePage.schedulePromotion(startDate, endDate, SchedulePage.Frequency.MONTHLY, finalDate);

		getElementFactory().getPromotionsDetailPage().schedulePromotion();
		Waits.loadOrFadeWait();
		schedulePage = getElementFactory().getSchedulePage();
		verifyThat("Due to pre-population 'schedule' should be pre-selected", schedulePage.optionSelected(schedulePage.schedule()));

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		assertThat(schedulePage.dateAsString(startDate), is(schedulePage.startDate()));
		assertThat(schedulePage.dateAsString(endDate), is(schedulePage.endDate()));

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		assertThat(schedulePage.readFrequency(),equalToIgnoringCase("MONTHLY"));
		assertThat("Due to pre-population 'repeat with frequency below' should be pre-selected", schedulePage.optionSelected(ElementUtil.getFirstChild(schedulePage.repeatWithFrequencyBelow())));

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		assertThat("Due to pre-population 'run this promotion schedule until the date below' should be pre-selected", schedulePage.optionSelected(ElementUtil.getFirstChild(schedulePage.runThisPromotionScheduleUntilTheDateBelow())));
		assertThat(schedulePage.dateAsString(finalDate), is(schedulePage.finalDate()));

		schedulePage.finishButton().click();
		Waits.loadOrFadeWait();
	}

	@Test
	public void testFrequencyPeriodNotLessThanPromotionLengthAndFinalDateNotLessThanEndDate() {
		SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "Georgia".toLowerCase());  //ON PREM ONLY ALLOWS LOWER CASE SEARCH TRIGGERS
		setUpPromotion(spotlight, new Query("საქართველო").withFilter(new LanguageFilter(Language.GEORGIAN)), 4);

		setEndDateCheckFrequencies(schedulePage.getTodayDate(),Arrays.asList("Yearly","Daily", "Monthly", "Weekly"));
		getElementFactory().getPromotionsDetailPage().schedulePromotion();

		setEndDateCheckFrequencies(DateUtils.addDays(schedulePage.getTodayDate(), 4),Arrays.asList("Yearly","Monthly", "Weekly"));
		getElementFactory().getPromotionsDetailPage().schedulePromotion();

		setEndDateCheckFrequencies(DateUtils.addWeeks(schedulePage.getTodayDate(), 2),Arrays.asList("Yearly","Monthly"));
		getElementFactory().getPromotionsDetailPage().schedulePromotion();

		setEndDateCheckFrequencies(DateUtils.addMonths(schedulePage.getTodayDate(), 1),Arrays.asList("Yearly"));
	}

	private void setEndDateCheckFrequencies(Date endDate,List<String> correctFrequencyOptions){
		schedulePage = getElementFactory().getSchedulePage();
		schedulePage.navigateWizardAndSetEndDate(endDate);

		List<String> availableFrequencies = schedulePage.getAvailableFrequencies();
		verifyThat("Correct number frequency options available",availableFrequencies,hasSize(correctFrequencyOptions.size()));

		sort(correctFrequencyOptions);
		sort(availableFrequencies);
		verifyThat("Available frequencies are correct",availableFrequencies.equals(correctFrequencyOptions));

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		schedulePage.runThisPromotionScheduleUntilTheDateBelow().click();
		assertThat(schedulePage.dateAndTimeAsString(endDate), is(schedulePage.dateText(schedulePage.finalDateTextBox())));

		schedulePage.finishButton().click();
		Waits.loadOrFadeWait();

	}
}
