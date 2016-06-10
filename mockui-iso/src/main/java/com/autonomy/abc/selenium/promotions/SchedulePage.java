package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.application.SOPageBase;
import com.hp.autonomy.frontend.selenium.element.DatePicker;
import com.hp.autonomy.frontend.selenium.element.Dropdown;
import com.hp.autonomy.frontend.selenium.util.ElementUtil;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.time.DateUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SchedulePage extends SOPageBase {

    private SchedulePage(final WebDriver driver) {
        super(driver.findElement(By.cssSelector(".pd-wizard")), driver);
    }

	//general
	private WebElement dataOption(String optionName){
		return findElement(By.cssSelector("[data-option='"+optionName+"']"));
	}

	public boolean optionSelected(WebElement option){
		return ElementUtil.hasClass("progressive-disclosure-selection",option);
	}

	public String dateText(WebElement dateTextBox){
		return dateTextBox.getAttribute("value");
	}

	public String date(WebElement dateTextBox){
		return dateText(dateTextBox).split(" ")[0];
	}

	public String time(WebElement dateTextBox){
		return dateText(dateTextBox).split(" ")[1];
	}

	public WebElement continueButton() {
		return findElement(By.cssSelector(".next-step"));
	}

	public WebElement cancelButton() {
		return findElement(By.cssSelector(".cancel-wizard"));
	}

	public WebElement finishButton() {
		return findElement(By.cssSelector(".finish-step"));
	}

	public boolean buttonDisabled(WebElement button){
		if (ElementUtil.isDisabled(button)){
			return true;
		}
		return false;
	}

	//#1 Enable schedule
	public WebElement alwaysActive(){
		return dataOption("ALWAYSACTIVE");
	}

	public WebElement schedule(){
		return dataOption("SCHEDULE");
	}

	//#2 Schedule duration
	public String startDate(){return date(startDateTextBox());}

	public String endDate(){return date(endDateTextBox());}

	public WebElement startDateTextBox() {
		return findElement(By.cssSelector(".promotion-schedule-start [type='text']"));
	}
	public WebElement endDateTextBox() {
		return findElement(By.cssSelector(".promotion-schedule-end [type='text']"));
	}

	public WebElement startDateCalendar() {
		return ElementUtil.getParent(findElement(By.cssSelector(".promotion-schedule-start .hp-icon")));
	}

	public WebElement endDateCalendar() {
		return ElementUtil.getParent(findElement(By.cssSelector(".promotion-schedule-end .hp-icon")));
	}

	//#3 Schedule frequency
	public WebElement doNotRepeat() {
		return dataOption("ONEOFF");
	}

	public WebElement repeatWithFrequencyBelow() {
		return dataOption("FREQUENCY");
	}

	public enum Frequency {
		DAILY("Daily"),
		WEEKLY("Weekly"),
		MONTHLY("Monthly"),
		YEARLY("Yearly");

		private final String title;

		Frequency(final String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}
	}

	private Dropdown dropdown(){return new Dropdown(findElement(By.cssSelector(".promotion-schedule-frequency")),getDriver());}

	public void selectFrequency(final Frequency frequency) {
		dropdown().select(frequency.getTitle());
	}

	public String readFrequency() {
		return dropdown().getValue();
	}

	public List<String> getAvailableFrequencies(){
		dropdown().open();
		return ElementUtil.getTexts(findElements(By.cssSelector(".promotion-schedule-frequency-item[style='display: inline;']")));
	}

	//#4 Schedule recurrence
	public WebElement never() {
		return dataOption("NEVER");
	}

	public WebElement runThisPromotionScheduleUntilTheDateBelow() {
		return dataOption("UNTIL");
	}

	public WebElement finalDateTextBox() {
		return findElement(By.cssSelector(".promotion-end-date [type='text']"));
	}

	public String finalDate(){return date(finalDateTextBox());}

	public WebElement finalDateCalendar() {
		return ElementUtil.getParent(findElement(By.cssSelector(".promotion-end-date .hp-icon")));
	}

	//GENERAL DATE CRAP
	public Date getTodayDate() {
		return new Date();
	}

	public String todayDateString(){return dateAsString(getTodayDate());}

	public String dateAndTimeAsString(final Date date) {
		return (new SimpleDateFormat("dd/MM/yyyy HH:mm")).format(date);
	}

	public int getDay(final int plusDays) {
		return Integer.parseInt((new SimpleDateFormat("dd")).format(DateUtils.addDays(getTodayDate(), plusDays)));
	}

	public String getMonth(final int plusDays) {
		return (new SimpleDateFormat("MMMMMMMMM")).format(DateUtils.addDays(getTodayDate(), plusDays));
	}

	public String dateAsString(final Date date) {
		return (new SimpleDateFormat("dd/MM/yyyy")).format(date);
	}

	public String parseDateObjectToPromotions(final String date) {
		final SimpleDateFormat wrongDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
		final SimpleDateFormat rightDate = new SimpleDateFormat("dd MMMMMMMMM yyyy HH:mm");

		try {
			return rightDate.format(wrongDate.parse(date));
		} catch (ParseException e) {//}
			return "Date didn't parse correctly!";
		}

	}
	//STUFF LIKE THIS SHOULD BE IN A SCHEDULE PROMOTION WIZARD OR SERIVCE
	public void navigateToScheduleDuration(){
		schedule().click();
		continueButton().click();
		Waits.loadOrFadeWait();
	}

	public void schedulePromotion(final Date startDate, final Date endDate, final Frequency frequency, final Date finalDate) {
		navigateToScheduleRecurrence(startDate,endDate,frequency);

		finalDateCalendar().click();
		Waits.loadOrFadeWait();
		new DatePicker(this,getDriver()).calendarDateSelect(finalDate);
		//finalDateCalendar().click();
		finishButton().click();
		Waits.loadOrFadeWait();
	}
	private void navigateToScheduleRecurrence(final Date startDate, final Date endDate, final Frequency frequency){
		Waits.loadOrFadeWait();
		schedule().click();
		continueButton().click();
		Waits.loadOrFadeWait();
		startDateCalendar().click();
		Waits.loadOrFadeWait();
		final DatePicker datePicker = new DatePicker(this, getDriver());
		datePicker.calendarDateSelect(startDate);
		startDateCalendar().click();
		endDateCalendar().click();
		loadOrFadeWait();
		datePicker.calendarDateSelect(endDate);
		endDateCalendar().click();
		continueButton().click();
		Waits.loadOrFadeWait();
		repeatWithFrequencyBelow().click();
		selectFrequency(frequency);
		continueButton().click();
		Waits.loadOrFadeWait();

	}

	public void schedulePromotion(final Date startDate, final Date endDate, final Frequency frequency) {
		navigateToScheduleRecurrence(startDate,endDate,frequency);
		never().click();
		finishButton().click();
		Waits.loadOrFadeWait();
	}

	//should return date picker
	public void scheduleDurationSelector(WebElement calendarButton, Date date){
		DatePicker datePicker = openDatePicker(calendarButton);
		datePicker.calendarDateSelect(date);
	}

	public DatePicker openDatePicker(WebElement calendarButton){
		calendarButton.click();
		return new DatePicker(this.$el(),getDriver());
	}

	public void resetDateToToday(WebElement calendarButton){
		//should check if already open
		DatePicker datePicker = new DatePicker(this.$el(),getDriver());
		datePicker.resetDateToToday();
	}


	public void setStartDate(int daysFromNow){
		scheduleDurationSelector(startDateCalendar(),DateUtils.addDays(getTodayDate(), daysFromNow));
		//what is the point?!
		startDateCalendar().click();
	}
	public void setEndDate(int daysFromNow){
		scheduleDurationSelector(endDateCalendar(),DateUtils.addDays(getTodayDate(),daysFromNow));
		endDateCalendar().click();
	}



	public void navigateWizardAndSetEndDate(final Date endDate) {
		Waits.loadOrFadeWait();
		schedule().click();
		continueButton().click();
		Waits.loadOrFadeWait();
		endDateCalendar().click();
		final DatePicker datePicker = new DatePicker(this.$el(), getDriver());
		datePicker.calendarDateSelect(endDate);
		endDateCalendar().click();
		continueButton().click();
		Waits.loadOrFadeWait();
		repeatWithFrequencyBelow().click();
	}

	public enum WizardStep {
		ENABLE_SCHEDULE("enableSchedule"),
		START_END("scheduleStartEnd"),
		FREQUENCY("scheduleFrequency"),
		FINAL("scheduleEndRecurrence");

		private final String title;

		WizardStep(final String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}
	}

    @Override
    public void waitForLoad() {
        waitForLoad(getDriver());
    }

    public static void waitForLoad(final WebDriver driver) {
        new WebDriverWait(driver, 20).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".pd-wizard .current-step-pill")));
    }

	public List<String> helpMessages(){
		return ElementUtil.getTexts(findElements(By.cssSelector(".help-block")));
	}

	public static class Factory extends SOPageFactory<SchedulePage> {
		public Factory() {
			super(SchedulePage.class);
		}

		public SchedulePage create(WebDriver context) {
			SchedulePage.waitForLoad(context);
			return new SchedulePage(context);
		}
	}
}
