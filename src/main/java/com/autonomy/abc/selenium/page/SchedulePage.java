package com.autonomy.abc.selenium.page;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SchedulePage extends AppElement implements AppPage{


	public SchedulePage(final TopNavBar topNavBar, final WebElement $el) {
		super($el, topNavBar.getDriver());
	}

	@Override
	public void navigateToPage()  {
		getDriver().get("promotions/schedule");
	}

	public WebElement alwaysActive() {
		return findElement(By.xpath(".//h4[contains(text(), 'Always active')]"));
	}

	public WebElement schedule() {
		return findElement(By.xpath(".//h4[contains(text(), 'Schedule')]"));
	}

	public WebElement continueButton(final String dataStep) {
		return findElement(By.cssSelector("[data-step='" + dataStep + "']")).findElement(By.xpath(".//button[contains(text(), 'Continue')]"));
	}

	public WebElement cancelButton(final String dataStep) {
		return findElement(By.cssSelector("[data-step='" + dataStep + "']")).findElement(By.xpath(".//button[contains(text(), 'Cancel')]"));
	}

	public WebElement finishButton(final String dataStep) {
		return findElement(By.cssSelector("[data-step='" + dataStep + "']")).findElement(By.xpath(".//button[contains(text(), 'Finish')]"));
	}

	public WebElement startDateTextBox() {
		return findElement(By.cssSelector(".promotion-schedule-start [type='text']"));
	}

	public WebElement endDateTextBox() {
		return findElement(By.cssSelector(".promotion-schedule-end [type='text']"));
	}

	public int getSelectedDayOfMonth() {
		return Integer.parseInt(getDriver().findElement(By.cssSelector(".datepicker-days .today")).getText());
	}

	public String getSelectedMonth() {
		return getDriver().findElement(By.cssSelector(".datepicker-switch")).getText().split("\\s+")[0];
	}

	// currentCalendarView can take the values 'days', 'months' or 'years'
	// TODO: Enum
	public WebElement getDatepickerSwitch(final String currentCalendarView) {
		return getDriver().findElement(By.cssSelector(".datepicker-" + currentCalendarView + " .datepicker-switch"));
	}

	public void calendarDateSelect(final Date date) {
		final SimpleDateFormat day = new SimpleDateFormat("dd");
		final SimpleDateFormat month = new SimpleDateFormat("MMM");
		final SimpleDateFormat year = new SimpleDateFormat("YYYY");

		getDatepickerSwitch("days").click();
		getDatepickerSwitch("months").click();
		datepickerYearSelect(year.format(date));
		datepickerMonthSelect(month.format(date));
		datepickerDaySelect(day.format(date));
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
		getDriver().findElement(By.xpath(".//td[@class='day' or @class='day active' or @class='day active today'][text()='" + strippedDay + "']")).click();
		loadOrFadeWait();
	}

	// frequency can contain values 'Daily', 'Weekly', 'Monthly' or 'Yearly'
	// TODO: Enum?
	public void selectFrequency(final String frequency) {
		findElement(By.cssSelector(".promotion-schedule-frequency .dropdown-toggle")).click();
		findElement(By.xpath(".//a[text()='" + frequency + "']")).click();
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
		return (new SimpleDateFormat("dd/MM/YYYY")).format(date);
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
