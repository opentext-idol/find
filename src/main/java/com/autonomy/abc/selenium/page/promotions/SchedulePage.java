package com.autonomy.abc.selenium.page.promotions;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.element.DatePicker;
import com.hp.autonomy.frontend.selenium.util.AppPage;
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

public class SchedulePage extends AppElement implements AppPage {

    private SchedulePage(final WebDriver driver) {
        super(driver.findElement(By.cssSelector(".pd-wizard")), driver);
    }

    public static SchedulePage make(final WebDriver driver) {
        SchedulePage.waitForLoad(driver);
        return new SchedulePage(driver);
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
		final DatePicker datePicker = new DatePicker(this, getDriver());
		datePicker.calendarDateSelect(startDate);
		startDateTextBoxButton().click();
		endDateTextBoxButton().click();
		datePicker.calendarDateSelect(endDate);
		endDateTextBoxButton().click();
		continueButton(WizardStep.START_END).click();
		loadOrFadeWait();
		repeatWithFrequencyBelow().click();
		selectFrequency(frequency);
		continueButton(WizardStep.FREQUENCY).click();
		loadOrFadeWait();
		finalDateTextBoxButton().click();
		datePicker.calendarDateSelect(finalDate);
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
		final DatePicker datePicker = new DatePicker(this, getDriver());
		datePicker.calendarDateSelect(endDate);
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

    @Override
    public void waitForLoad() {
        waitForLoad(getDriver());
    }

    public static void waitForLoad(final WebDriver driver) {
        new WebDriverWait(driver, 20).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".pd-wizard .current-step-pill")));
    }

}
