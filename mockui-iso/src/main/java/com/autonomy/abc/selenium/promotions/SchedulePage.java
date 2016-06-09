package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.application.SOPageBase;
import com.hp.autonomy.frontend.selenium.element.DatePicker;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SchedulePage extends SOPageBase {

    private SchedulePage(final WebDriver driver) {
        super(driver.findElement(By.cssSelector(".pd-wizard")), driver);
    }

	public WebElement alwaysActive() {
		return ElementUtil.getParent(findElement(By.xpath(".//h4[contains(text(), 'Always active')]")));
	}

	public WebElement schedule() {
		return ElementUtil.getParent(findElement(By.xpath(".//h4[contains(text(), 'Schedule')]")));
	}

	public WebElement continueButton() {
		return findElement(By.xpath("//button[contains(text(), 'Continue')]"));
	}

	public WebElement cancelButton() {
		return findElement(By.xpath(".//button[contains(text(), 'Cancel')]"));
	}

	public WebElement finishButton() {
		return findElement(By.xpath("//button[contains(text(), 'Finish')]"));
	}

	public boolean buttonDisabled(WebElement button){
		if (ElementUtil.isDisabled(button)){
			return true;
		}
		return false;
	}

	public boolean optionSelected(WebElement option){
		return ElementUtil.hasClass("progressive-disclosure-selection",option);
	}

	public WebElement startDateTextBox() {
		return findElement(By.cssSelector(".promotion-schedule-start [type='text']"));
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

	public WebElement endDateTextBox() {
		return findElement(By.cssSelector(".promotion-schedule-end [type='text']"));
	}

	public WebElement finalDateTextBox() {
		return findElement(By.cssSelector(".promotion-end-date [type='text']"));
	}

	public void selectFrequency(final Frequency frequency) {
		findElement(By.cssSelector(".promotion-schedule-frequency .dropdown-toggle")).click();
		findElement(By.xpath(".//a[text()='" + frequency.getTitle() + "']")).click();
	}

	//TODO toggle for frequency dropdown

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

	public String readFrequency() {
		return findElement(By.cssSelector(".current-frequency")).getText();
	}

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

	public WebElement startDateCalendar() {
		return ElementUtil.getParent(findElement(By.cssSelector(".promotion-schedule-start .hp-icon")));
	}

	public WebElement endDateCalendar() {
		return ElementUtil.getParent(findElement(By.cssSelector(".promotion-schedule-end .hp-icon")));
	}

	public WebElement finalDateTextBoxButton() {
		return ElementUtil.getParent(findElement(By.cssSelector(".promotion-end-date .hp-icon")));
	}

	public WebElement doNotRepeat() {
		return findElement(By.xpath(".//h4[contains(text(), 'Do not repeat')]/../.."));
	}

	public WebElement repeatWithFrequencyBelow() {
		return findElement(By.xpath(".//h4[contains(text(), 'Repeat with frequency below')]/../.."));
	}

	public WebElement never() {
		return findElement(By.xpath(".//h4[contains(text(), 'Never')]/../.."));
	}

	public WebElement runThisPromotionScheduleUntilTheDateBelow() {
		return findElement(By.xpath(".//h4[contains(text(), 'Run this promotion schedule until the date below')]/../.."));
	}

	public static String parseDateForPromotionsPage(final String date) throws ParseException {
		final SimpleDateFormat numberDate = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		final SimpleDateFormat monthWord = new SimpleDateFormat("dd MMMMMMMMM yyyy HH:mm");

		return monthWord.format(numberDate.parse(date)).replaceFirst("^0", "");
	}

	//STUFF LIKE THIS SHOULD BE IN A SCHEDULE PROMOTION WIZARD OR SERIVCE

	public void navigateToScheduleDuration(){
		schedule().click();
		continueButton().click();
		Waits.loadOrFadeWait();
	}

	public void schedulePromotion(final Date startDate, final Date endDate, final Frequency frequency, final Date finalDate) {
		navigateToScheduleRecurrence(startDate,endDate,frequency);


		finalDateTextBoxButton().click();
		Waits.loadOrFadeWait();
		new DatePicker(this,getDriver()).calendarDateSelect(finalDate);
		//finalDateTextBoxButton().click();
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

	public List<String> getAvailableFrequencies(){
		//TODO replace this
		findElement(By.cssSelector(".promotion-schedule-frequency .dropdown-toggle")).click();
		return ElementUtil.getTexts(findElements(By.cssSelector(".promotion-schedule-frequency-item[style='display: inline;']")));
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
