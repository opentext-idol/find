package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SchedulePage extends AppElement implements AppPage{


	public SchedulePage(final TopNavBar topNavBar, final WebElement $el) {
		super($el, topNavBar.getDriver());
	}

	@Override
	public void navigateToPage()  {
		getDriver().get("promotions/schedule");
	}

	public WebElement alwaysActive() {
		return getParent(findElement(By.xpath(".//h4[contains(text(), 'Always active')]")));
	}

	public WebElement schedule() {
		return getParent(findElement(By.xpath(".//h4[contains(text(), 'Schedule')]")));
	}

	public WebElement continueButton(final WizardStep dataStep) {
		return findElement(By.cssSelector("[data-step='" + dataStep.getTitle() + "']")).findElement(By.xpath(".//button[contains(text(), 'Continue')]"));
	}

	public WebElement cancelButton(final WizardStep dataStep) {
		return findElement(By.cssSelector("[data-step='" + dataStep.getTitle() + "']")).findElement(By.xpath(".//button[contains(text(), 'Cancel')]"));
	}

	public WebElement finishButton(final WizardStep dataStep) {
		return findElement(By.cssSelector("[data-step='" + dataStep.getTitle() + "']")).findElement(By.xpath(".//button[contains(text(), 'Finish')]"));
	}

	public WebElement startDateTextBox() {
		return findElement(By.cssSelector(".promotion-schedule-start [type='text']"));
	}

	public WebElement endDateTextBox() {
		return findElement(By.cssSelector(".promotion-schedule-end [type='text']"));
	}

	public WebElement finalDateTextBox() {
		return findElement(By.cssSelector(".promotion-end-date [type='text']"));
	}

	public int getSelectedDayOfMonth() {
		return Integer.parseInt(getDriver().findElement(By.cssSelector(".datepicker-days td.active")).getText());
	}

	public String getSelectedMonth() {
		return getDriver().findElement(By.cssSelector(".picker-switch")).getText().split("\\s+")[0];
	}

	public WebElement getDatepickerSwitch(final CalendarView currentCalendarView) {
		return getDriver().findElement(By.cssSelector(".datepicker-" + currentCalendarView.getTitle() + " .picker-switch"));
	}

	public enum CalendarView {
		DAYS("days"),
		MONTHS("months"),
		YEARS("years");

		private final String title;

		CalendarView(final String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}
	}

	public void calendarDateSelect(final Date date) {
		final SimpleDateFormat day = new SimpleDateFormat("dd");
		final SimpleDateFormat month = new SimpleDateFormat("MMM");
		final SimpleDateFormat year = new SimpleDateFormat("yyyy");
		final SimpleDateFormat hour = new SimpleDateFormat("hh");
		final SimpleDateFormat minute = new SimpleDateFormat("mm");

		getDatepickerSwitch(CalendarView.DAYS).click();
		getDatepickerSwitch(CalendarView.MONTHS).click();
		datepickerYearSelect(year.format(date));
		datepickerMonthSelect(month.format(date));
		datepickerDaySelect(day.format(date));
		togglePicker();
		loadOrFadeWait();
		timepickerHour().click();
		selectTimepickerHour(Integer.parseInt(hour.format(date)));
		timepickerMinute().click();
		selectTimepickerMinute(Integer.parseInt(minute.format(date)));
		setMinuteUsingIncrementDecrement(Integer.parseInt(minute.format(date)));
	}

	public void datepickerYearSelect(final String year) {
		getDriver().findElement(By.xpath(".//span[contains(@class, 'year')][text()='" + year + "']")).click();
		loadOrFadeWait();
	}

	public void datepickerMonthSelect(final String month) {
		getDriver().findElement(By.xpath(".//span[contains(@class, 'month')][text()='" + month + "']")).click();
		loadOrFadeWait();
	}

	public void datepickerDaySelect(final String day) {
		final String strippedDay = StringUtils.stripStart(day, "0");
		getDriver().findElement(By.xpath(".//td[@class='day' or @class='day today' or @class='day today weekend' or @class='day active' or @class='day active today' or @class='day weekend'][text()='" + strippedDay + "']")).click();
		loadOrFadeWait();
	}

	public void selectFrequency(final Frequency frequency) {
		findElement(By.cssSelector(".promotion-schedule-frequency .dropdown-toggle")).click();
		findElement(By.xpath(".//a[text()='" + frequency.getTitle() + "']")).click();
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

	public String readFrequency() {
		return findElement(By.cssSelector(".current-frequency")).getText();
	}

	public Date getTodayDate() {
		return new Date();
	}

	public int getDay() {
		return Integer.parseInt((new SimpleDateFormat("dd")).format(getTodayDate()));
	}

	public String getMonth() {
		return (new SimpleDateFormat("MMMMMMMMM")).format(getTodayDate());
	}

	public String dateAsString(final Date date) {
		return (new SimpleDateFormat("dd/MM/yyyy")).format(date);
	}

	public void resetDateToToday() {
		findElement(By.cssSelector(".picker-switch [data-action='today']")).click();
	}

	public void incrementHours() {
		findElement(By.cssSelector("[data-action='incrementHours']")).click();
	}

	public void incrementMinutes() {
		findElement(By.cssSelector("[data-action='incrementMinutes']")).click();
	}

	public void decrementHours() {
		findElement(By.cssSelector("[data-action='decrementHours']")).click();
	}

	public void decrementMinutes() {
		findElement(By.cssSelector("[data-action='decrementMinutes']")).click();
	}

	public void togglePicker() {
		findElement(By.cssSelector("[data-action='togglePicker']")).click();
	}

	public String dateAndTimeAsString(final Date date) {
		return (new SimpleDateFormat("dd/MM/yyyy HH:mm")).format(date);
	}

	public WebElement timepickerHour() {
		return findElement(By.cssSelector(".timepicker-hour"));
	}

	public WebElement timepickerMinute() {
		return findElement(By.cssSelector(".timepicker-minute"));
	}

	public void selectTimepickerHour(final int hour) {
		if (hour > 23 || hour < 0) {
			throw new IllegalArgumentException("Hours must not be greater than 23 or less than 0");
		}
		String hourString = String.valueOf(hour);
		if (hourString.length() == 1) {
			hourString = "0" + hourString;
		}
		findElement(By.cssSelector(".timepicker-hours")).findElement(By.xpath(".//td[contains(text(), '" + hourString + "')]")).click();
	}

	public void selectTimepickerMinute(int minute) {
		if (minute > 59 || minute < 0) {
			throw new IllegalArgumentException("Minutes must not be greater than 59 or less than 0");
		}
		minute = minute / 5 * 5; //rounding to nearest 5
		String minuteString = String.valueOf(minute);
		if (minuteString.length() == 1) {
			minuteString = "0" + minuteString;
		}
		findElement(By.cssSelector(".timepicker-minutes")).findElement(By.xpath(".//td[contains(text(), '" + minuteString + "')]")).click();
		loadOrFadeWait();
	}

	public void setMinuteUsingIncrementDecrement(final int minute) {
		final int difference = minute - Integer.parseInt(timepickerMinute().getText());
		if (difference > 0) {
			for (int i = 1; i <= difference; i++) {
				incrementMinutes();
			}
		} else {
			for (int i = - 1; i >= difference; i--) {
				decrementMinutes();
			}
		}
		loadOrFadeWait();
	}

	public void setHourUsingIncrementDecrement(final int hour) {
		final int difference = hour - Integer.parseInt(timepickerHour().getText());
		if (difference > 0) {
			for (int i = 1; i <= difference; i++) {
				incrementHours();
			}
		} else {
			for (int i = - 1; i >= difference; i--) {
				decrementHours();
			}
		}
		loadOrFadeWait();
	}

	public WebElement startDateTextBoxButton() {
		return getParent(findElement(By.cssSelector(".promotion-schedule-start .fa-calendar-o")));
	}

	public WebElement endDateTextBoxButton() {
		return getParent(findElement(By.cssSelector(".promotion-schedule-end .fa-calendar-o")));
	}

	public WebElement finalDateTextBoxButton() {
		return getParent(findElement(By.cssSelector(".promotion-end-date .fa-calendar-o")));
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

	public void schedulePromotion(final Date startDate, final Date endDate, final Frequency frequency, final Date finalDate) {
		loadOrFadeWait();
		schedule().click();
		continueButton(WizardStep.ENABLE_SCHEDULE).click();
		loadOrFadeWait();
		startDateTextBoxButton().click();
		calendarDateSelect(startDate);
		startDateTextBoxButton().click();
		endDateTextBoxButton().click();
		calendarDateSelect(endDate);
		endDateTextBoxButton().click();
		continueButton(WizardStep.START_END).click();
		loadOrFadeWait();
		repeatWithFrequencyBelow().click();
		selectFrequency(frequency);
		continueButton(WizardStep.FREQUENCY).click();
		loadOrFadeWait();
		finalDateTextBoxButton().click();
		calendarDateSelect(finalDate);
		finalDateTextBoxButton().click();
		finishButton(WizardStep.FINAL).click();
		loadOrFadeWait();
		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(getDriver().findElement(By.cssSelector("[data-route='promotions']"))));
	}

	public List<String> getAvailableFrequencies() {
		findElement(By.cssSelector(".promotion-schedule-frequency .dropdown-toggle")).click();
		final List<String> frequencies = new ArrayList<>();
		for (final WebElement frequency : findElements(By.cssSelector(".promotion-schedule-frequency-item"))) {
			frequencies.add(frequency.getText());
		}
		return frequencies;
	}

	public void navigateWizardAndSetEndDate(final Date endDate) {
		loadOrFadeWait();
		schedule().click();
		continueButton(SchedulePage.WizardStep.ENABLE_SCHEDULE).click();
		loadOrFadeWait();
		endDateTextBoxButton().click();
		calendarDateSelect(endDate);
		endDateTextBoxButton().click();
		continueButton(SchedulePage.WizardStep.START_END).click();
		loadOrFadeWait();
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

	public static class Placeholder {

		private final TopNavBar topNavBar;

		public Placeholder(final TopNavBar topNavBar) {
			this.topNavBar = topNavBar;
		}

		public SchedulePage $schedulePage(final WebElement element) {
			return new SchedulePage(topNavBar, element);
		}
	}
}
