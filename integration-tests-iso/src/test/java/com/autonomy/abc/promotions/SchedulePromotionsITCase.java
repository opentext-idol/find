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
import org.openqa.selenium.NoSuchElementException;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.urlContains;
import static java.util.Collections.sort;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.openqa.selenium.lift.Matchers.displayed;

//TODO: schedulePage.date(schedulePage.startDateTextBox())
//TODO: data-option -> getting the box better?!

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
	public void testSchedulePromotionForTomorrow() throws ParseException {
		SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.SPONSORED, "wand magic spells");
        setUpPromotion(spotlight, new Query("wizard"), 4);
		//on the schedule page
		schedulePage.navigateToScheduleDuration();
		schedulePage.scheduleDurationSelector(schedulePage.startDateCalendar(),DateUtils.addDays(schedulePage.getTodayDate(),1));


		assertThat(schedulePage.date(schedulePage.startDateTextBox()), is(schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 1))));
		final String startDate = SchedulePage.parseDateForPromotionsPage(schedulePage.dateText(schedulePage.startDateTextBox()));
		schedulePage.startDateCalendar().click();
		Waits.loadOrFadeWait();

		Date newDate = DateUtils.addDays(schedulePage.getTodayDate(), 5);
		schedulePage.scheduleDurationSelector(schedulePage.endDateCalendar(),newDate);
		assertThat(schedulePage.date(schedulePage.endDateTextBox()), is(schedulePage.dateAsString(newDate)));

		final String endDate = SchedulePage.parseDateForPromotionsPage(schedulePage.dateText(schedulePage.endDateTextBox()));
		schedulePage.endDateCalendar().click();
		Waits.loadOrFadeWait();

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText(), containsString("Do you want to repeat this promotion schedule?"));

		schedulePage.repeatWithFrequencyBelow().click();
		schedulePage.selectFrequency(SchedulePage.Frequency.MONTHLY);
		assertEquals(schedulePage.readFrequency(), "MONTHLY");

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText(), containsString("When should this promotion schedule finish?"));

		schedulePage.runThisPromotionScheduleUntilTheDateBelow().click();
		assertThat(schedulePage.date(schedulePage.finalDateTextBox()), is(schedulePage.dateAsString(newDate)));

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

		schedulePage.finishButton().click();
		promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
		assertThat("Correct Scheduling text not visible", promotionsDetailPage.getText(), containsString("The promotion is scheduled to run starting on " + startDate + " for the duration of 4 days, ending on " + endDate));
		assertThat("Correct Scheduling text not visible", promotionsDetailPage.getText(), containsString("This promotion schedule will run monthly until " + finalDate));

		//TODO make a better wait -> sometimes works, sometimes doesn't
		getElementFactory().getTopNavBar().search("magic");
		Waits.loadOrFadeWait();
		searchPage = getElementFactory().getSearchPage();
		Waits.loadOrFadeWait();
		assertThat("promotions aren't scheduled to be shown now", searchPage.isPromotionsBoxVisible(), is(false));

		promotionService.goToDetails("magic");
		promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
		promotionsDetailPage.schedulePromotion();
		Waits.loadOrFadeWait();
		schedulePage = getElementFactory().getSchedulePage();
		schedulePage.alwaysActive().click();
		schedulePage.finishButton().click();
		Waits.loadOrFadeWait();
		promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
		assert(promotionsDetailPage.getText().contains("The promotion is always active"));

		getElementFactory().getTopNavBar().search("magic");
		Waits.loadOrFadeWait();
		searchPage = getElementFactory().getSearchPage();
		assertThat("promotions are scheduled to be shown now", searchPage.isPromotionsBoxVisible(), is(true));
	}


	//NAVIVATION CRAP -> its own test
	@Test
	public void testNavigateScheduleWizard(){
		SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "\"ice cream\" chips");
		setUpPromotion(spotlight, new Query("wizard"), 4);

		assertThat(getWindow(), urlContains("schedule"));
		assertThat("Wrong wizard text", schedulePage.getText(), containsString("Schedule your promotion"));
		assertThat(schedulePage.finishButton(), displayed());
		assertThat("Finish button should be enabled", !schedulePage.buttonDisabled(schedulePage.finishButton()));

		//from testSchedulePromotionForTomorrow
		assertThat("Always active isn't selected", schedulePage.alwaysActive().getAttribute("class"), containsString("progressive-disclosure-selection"));
		assertThat("Schedule shouldn't be selected", schedulePage.schedule().getAttribute("class"), not(containsString("progressive-disclosure-selection")));
		assertThat("Finish button should be enabled", !schedulePage.buttonDisabled(schedulePage.finishButton()));

		schedulePage.alwaysActive().click();
		Waits.loadOrFadeWait();
		assertThat("Finish button should be enabled", !schedulePage.buttonDisabled(schedulePage.finishButton()));

		schedulePage.schedule().click();
		Waits.loadOrFadeWait();
		assertThat(schedulePage.continueButton(), displayed());

		schedulePage.alwaysActive().click();
		Waits.loadOrFadeWait();
		assertThat("Finish button should be enabled", !schedulePage.buttonDisabled(schedulePage.finishButton()));

		schedulePage.schedule().click();
		Waits.loadOrFadeWait();
		assertThat(schedulePage.continueButton(), displayed());

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText(), containsString("How long should this promotion run?"));

		assertThat(schedulePage.date(schedulePage.startDateTextBox()), is(schedulePage.todayDateString()));
		assertThat(schedulePage.date(schedulePage.endDateTextBox()), is(schedulePage.dateAsString(DateUtils.addDays(schedulePage.getTodayDate(), 1))));

		//from testSchedulePromotionForTomorrow
		assertThat(schedulePage.time(schedulePage.startDateTextBox()), is("00:00"));
		assertThat(schedulePage.time(schedulePage.endDateTextBox()), is("00:00"));

		schedulePage.startDateCalendar().click();
		datePicker = new DatePicker(schedulePage.$el(),getDriver());
		assertThat(datePicker.getSelectedDayOfMonth(), is(schedulePage.getDay(0)));
		assertThat(datePicker.getSelectedMonth(), is(schedulePage.getMonth(0)));

		//KEEP NAVIGATING ALL THE WAY TO THE END AND THEN CHECK THIS CRAP

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		assertThat("Correct wizard text", schedulePage.getText(), containsString("Do you want to repeat this promotion schedule?"));

		schedulePage.repeatWithFrequencyBelow().click();
		schedulePage.selectFrequency(SchedulePage.Frequency.YEARLY);
		assertThat(schedulePage.readFrequency(), is("YEARLY"));

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		schedulePage.never().click();
		schedulePage.finishButton().click();
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
	public void testDetailsAndDocumentsCorrectForSchedule() throws ParseException{

		//ALL BAD NEEDS TO USE A WIZARD

		//sets up and schedules promotion
		SpotlightPromotion spotlight = new SpotlightPromotion(Promotion.SpotlightType.HOTWIRE, "\"ice cream\" chips");
		setUpPromotion(spotlight, new Query("wizard"), 4);
		Date start= DateUtils.addDays(schedulePage.getTodayDate(), 4);
		Date end = DateUtils.addDays(schedulePage.getTodayDate(), 10);

		schedulePage.schedulePromotion(start,end,SchedulePage.Frequency.YEARLY);
		promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
		promotionsDetailPage.schedulePromotion();
		schedulePage = getElementFactory().getSchedulePage();
		schedulePage.schedule().click();
		schedulePage.continueButton().click();
		String startDate = schedulePage.dateText(schedulePage.startDateTextBox());
		String endDate = schedulePage.date(schedulePage.endDateTextBox());

		PromotionsDetailPage promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
		assertThat("Correct promotion summary text not present", promotionsDetailPage.getText(), containsString("The promotion is scheduled to run starting on " + schedulePage.parseDateForPromotionsPage(startDate) + " for the duration of 6 days, ending on " +schedulePage.parseDateForPromotionsPage(endDate)));
		assertThat("Correct promotion summary text not present", promotionsDetailPage.getText(), containsString("This promotion schedule will run yearly forever."));


		//TODO better wait
		getElementFactory().getTopNavBar().search("chips");
		Waits.loadOrFadeWait();
		searchPage = getElementFactory().getSearchPage();
		Waits.loadOrFadeWait();
		assertThat("promotions aren't scheduled to be shown now", searchPage.isPromotionsBoxVisible(), is(false));

		promotionService.goToDetails("chips");
		getElementFactory().getPromotionsDetailPage().schedulePromotion();
		schedulePage = getElementFactory().getSchedulePage();
		Waits.loadOrFadeWait();
		assertThat("Schedule should be selected due to prepopulated schedule", schedulePage.schedule().getAttribute("class"), containsString("progressive-disclosure-selection"));

		schedulePage.schedule().click();
		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();

		assertThat(startDate.toString(), is(schedulePage.startDateTextBox().getAttribute("value")));
		assertThat(endDate.toString(), is(schedulePage.endDateTextBox().getAttribute("value")));

		schedulePage.scheduleDurationSelector(schedulePage.startDateCalendar(),schedulePage.getTodayDate());

		schedulePage.startDateCalendar().click();
		assertThat(schedulePage.date(schedulePage.startDateTextBox()), is(schedulePage.dateAsString(schedulePage.getTodayDate())));

		String newStartDate= schedulePage.startDateTextBox().getAttribute("value");
		String newEndDate = schedulePage.endDateTextBox().getAttribute("value");

		//EVERYWHERE BELOW HERE NEEDS newStartDate where startDate etc is
		/*schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		assertThat("Wrong wizard text", schedulePage.getText(), containsString("Do you want to repeat this promotion schedule?"));

		schedulePage.selectFrequency(SchedulePage.Frequency.YEARLY);
		assertThat(schedulePage.readFrequency(), is("YEARLY"));

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		schedulePage.runThisPromotionScheduleUntilTheDateBelow().click();
		schedulePage.finalDateTextBoxButton().click();
		datePicker = new DatePicker(schedulePage.$el(),getDriver());
		datePicker.calendarDateSelect(DateUtils.addYears(schedulePage.getTodayDate(), 2));
		schedulePage.finalDateTextBoxButton().click();
		final String finalDate = schedulePage.finalDateTextBox().getAttribute("value");

		schedulePage.finishButton().click();
		Waits.loadOrFadeWait();
		promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
		assertThat("Correct schedule summary text not visible", promotionsDetailPage.getText(), containsString("The promotion is scheduled to run starting on " + SchedulePage.parseDateForPromotionsPage(newStartDate) + " for the duration of 4 days, ending on " + SchedulePage.parseDateForPromotionsPage(endDate)));
		assertThat("Correct schedule summary text not visible", promotionsDetailPage.getText(), containsString("This promotion schedule will run yearly until " + SchedulePage.parseDateForPromotionsPage(finalDate)));

		getElementFactory().getTopNavBar().search("chips");
		Waits.loadOrFadeWait();
		searchPage = getElementFactory().getSearchPage();
		assertThat("promotions are scheduled to be shown now but are not visible", searchPage.isPromotionsBoxVisible(), is(true));*/
	}


	@Test
	public void testResetTimeAndDate() {
		setUpKoreaSpotlight();

		Date startDate = DateUtils.addDays(schedulePage.getTodayDate(), 9);
		schedulePage.scheduleDurationSelector(schedulePage.startDateCalendar(),startDate);
		assertThat(schedulePage.dateAsString(startDate), is(schedulePage.date(schedulePage.startDateTextBox())));

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
		//gone back to promotionDetailsPage

		//check first page already selected
		getElementFactory().getPromotionsDetailPage().schedulePromotion();
		Waits.loadOrFadeWait();
		schedulePage = getElementFactory().getSchedulePage();
		verifyThat("Due to pre-population 'schedule' should be pre-selected", schedulePage.optionSelected(schedulePage.schedule()));

		//check preserved dates
		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		assertThat(schedulePage.dateAsString(startDate), is(schedulePage.date(schedulePage.startDateTextBox())));
		assertThat(schedulePage.dateAsString(endDate), is(schedulePage.date(schedulePage.endDateTextBox())));

		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		assertThat(schedulePage.readFrequency(),equalToIgnoringCase("MONTHLY"));
		assertThat("Due to pre-population 'repeat with frequency below' should be pre-selected", schedulePage.optionSelected(ElementUtil.getFirstChild(schedulePage.repeatWithFrequencyBelow())));

		//continue
		schedulePage.continueButton().click();
		Waits.loadOrFadeWait();
		assertThat("Due to pre-population 'run this promotion schedule until the date below' should be pre-selected", schedulePage.optionSelected(ElementUtil.getFirstChild(schedulePage.runThisPromotionScheduleUntilTheDateBelow())));
		assertThat(schedulePage.dateAsString(finalDate), is(schedulePage.date(schedulePage.finalDateTextBox())));

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
