package com.autonomy.abc.selenium.element;

import com.autonomy.abc.selenium.util.Waits;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DatePicker extends AppElement {
	/* should pass in the .input-group element */
	public DatePicker(final WebElement element, final WebDriver driver) {
		super(element, driver);
	}

	public void open() {
		if (!isOpen()) {
			toggleOpen();
		}
	}

	public void close() {
		if (isOpen()) {
			toggleOpen();
		}
	}

	private boolean isOpen() {
		return findElements(By.cssSelector(".dropdown-menu")).size() > 0;
	}

	private void toggleOpen() {
		findElement(By.cssSelector(".input-group-addon")).click();
	}

	public WebElement timePickerHour() {
		return findElement(By.cssSelector(".timepicker-hour"));
	}

	public WebElement timePickerMinute() {
		return findElement(By.cssSelector(".timepicker-minute"));
	}

	public void selectTimePickerHour(final int hour) {
		if (hour > 23 || hour < 0) {
			throw new IllegalArgumentException("Hours must not be greater than 23 or less than 0");
		}
		String hourString = String.valueOf(hour);
		if (hourString.length() == 1) {
			hourString = '0' + hourString;
		}
		findElement(By.cssSelector(".timepicker-hours")).findElement(By.xpath(".//td[contains(text(), '" + hourString + "')]")).click();
	}

	public void selectTimePickerMinute(int minute) {
		if (minute > 59 || minute < 0) {
			throw new IllegalArgumentException("Minutes must not be greater than 59 or less than 0");
		}
		minute = minute / 5 * 5; //rounding to nearest 5
		String minuteString = String.valueOf(minute);
		if (minuteString.length() == 1) {
			minuteString = '0' + minuteString;
		}
		findElement(By.cssSelector(".timepicker-minutes")).findElement(By.xpath(".//td[contains(text(), '" + minuteString + "')]")).click();
		Waits.loadOrFadeWait();
	}

	public void setMinuteUsingIncrementDecrement(final int minute) {
		final int difference = minute - Integer.parseInt(timePickerMinute().getText());
		if (difference > 0) {
			for (int i = 1; i <= difference; i++) {
				incrementMinutes();
			}
		} else {
			for (int i = - 1; i >= difference; i--) {
				decrementMinutes();
			}
		}
		Waits.loadOrFadeWait();
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

	// TODO: what is this doing?
	public void togglePicker() {
		findElement(By.cssSelector("[data-action='togglePicker']")).click();
	}

	public int getSelectedDayOfMonth() {
		return Integer.parseInt(getDriver().findElement(By.cssSelector(".datepicker-days td.active")).getText());
	}

	public String getSelectedMonth() {
		return getDriver().findElement(By.cssSelector(".picker-switch")).getText().split("\\s+")[0];
	}

	private WebElement getDatePickerSwitch(final CalendarView currentCalendarView) {
		return getDriver().findElement(By.cssSelector(".datepicker-" + currentCalendarView.getTitle() + " .picker-switch"));
	}

	private enum CalendarView {
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
		final SimpleDateFormat hour = new SimpleDateFormat("HH");
		final SimpleDateFormat minute = new SimpleDateFormat("mm");

		getDatePickerSwitch(CalendarView.DAYS).click();
		getDatePickerSwitch(CalendarView.MONTHS).click();
		datePickerYearSelect(year.format(date));
		datePickerMonthSelect(month.format(date));
		datePickerDaySelect(day.format(date));
		togglePicker();
		Waits.loadOrFadeWait();
		timePickerHour().click();
		selectTimePickerHour(Integer.parseInt(hour.format(date)));
		Waits.loadOrFadeWait();
		timePickerMinute().click();
		selectTimePickerMinute(Integer.parseInt(minute.format(date)));
		setMinuteUsingIncrementDecrement(Integer.parseInt(minute.format(date)));
	}

	private void datePickerYearSelect(final String year) {
		getDriver().findElement(By.xpath(".//span[contains(@class, 'year')][text()='" + year + "']")).click();
		Waits.loadOrFadeWait();
	}

	private void datePickerMonthSelect(final String month) {
		getDriver().findElement(By.xpath(".//span[contains(@class, 'month')][text()='" + month + "']")).click();
		Waits.loadOrFadeWait();
	}

	private void datePickerDaySelect(final String day) {
		final String strippedDay = StringUtils.stripStart(day, "0");
		getDriver().findElement(By.xpath(".//td[@class='day' or @class='day today' or @class='day today weekend' or @class='day active' or @class='day active today' or @class='day weekend'][text()='" + strippedDay + "']")).click();
		Waits.loadOrFadeWait();
	}
}
