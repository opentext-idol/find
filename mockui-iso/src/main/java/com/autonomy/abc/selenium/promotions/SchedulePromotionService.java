package com.autonomy.abc.selenium.promotions;

import com.autonomy.abc.selenium.actions.ServiceBase;
import com.autonomy.abc.selenium.application.IsoApplication;
import com.autonomy.abc.selenium.iso.IdolIsoElementFactory;
import com.hp.autonomy.frontend.selenium.element.DatePicker;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.apache.commons.lang3.time.DateUtils;
import org.openqa.selenium.WebElement;

import java.util.Date;

public class SchedulePromotionService<T extends IdolIsoElementFactory> extends ServiceBase<T> {
    private SchedulePage schedulePage;

    public SchedulePromotionService(IsoApplication<? extends T> application) {
        super(application);
    }

    //starting from promotion details
    public SchedulePage goToSchedule(){
        IdolPromotionsDetailPage promotionsDetailPage = getElementFactory().getPromotionsDetailPage();
        promotionsDetailPage.schedulePromotion();
        return getElementFactory().getSchedulePage();
    }

    public SchedulePage schedulePage(){return getElementFactory().getSchedulePage();}

    public void navigateToScheduleDuration(){
        schedulePage = schedulePage();
        schedulePage.schedule().click();
        schedulePage.continueButton().click();
        Waits.loadOrFadeWait();
    }

    public void schedulePromotion(final Date startDate, final Date endDate, final SchedulePage.Frequency frequency, final Date finalDate) {

        navigateToScheduleRecurrence(startDate,endDate,frequency);
        scheduleDurationSelector(schedulePage.finalDateCalendar(),finalDate);

        schedulePage.finishButton().click();
        Waits.loadOrFadeWait();
    }

    public void schedulePromotion(final Date startDate, final Date endDate, final SchedulePage.Frequency frequency) {
        navigateToScheduleRecurrence(startDate,endDate,frequency);
        schedulePage.never().click();
        schedulePage.finishButton().click();
        Waits.loadOrFadeWait();
    }

    private void navigateToScheduleRecurrence(final Date startDate, final Date endDate, final SchedulePage.Frequency frequency){
        navigateToScheduleDuration();

        //maybe the clicking should be inside the method? Check everywhere it's used
        scheduleDurationSelector(schedulePage.startDateCalendar(),startDate);
        //schedulePage.startDateCalendar().click();

        scheduleDurationSelector(schedulePage.endDateCalendar(),endDate);
        //schedulePage.endDateCalendar().click();

        schedulePage.continueButton().click();
        Waits.loadOrFadeWait();

        schedulePage.repeatWithFrequencyBelow().click();
        schedulePage.selectFrequency(frequency);
        schedulePage.continueButton().click();
        Waits.loadOrFadeWait();
    }

    public void scheduleDurationSelector(WebElement calendarButton, Date date){
        DatePicker datePicker = openDatePicker(calendarButton);
        datePicker.calendarDateSelect(date);
        calendarButton.click();
    }

    public DatePicker openDatePicker(WebElement calendarButton){
        schedulePage = schedulePage();
        calendarButton.click();
        return new DatePicker(schedulePage,getDriver());
    }

    public void resetDateToToday(WebElement calendarButton){
        DatePicker datePicker = new DatePicker(schedulePage,getDriver());
        datePicker.open();
        datePicker.resetDateToToday();
    }

    public void setStartDate(int daysFromNow){
        scheduleDurationSelector(schedulePage.startDateCalendar(), DateUtils.addDays(schedulePage.getTodayDate(), daysFromNow));
        //what is the point?!
        //schedulePage.startDateCalendar().click();
    }
    public void setEndDate(int daysFromNow){
        scheduleDurationSelector(schedulePage.endDateCalendar(),DateUtils.addDays(schedulePage.getTodayDate(),daysFromNow));
        //schedulePage.endDateCalendar().click();
    }

    public void navigateWizardAndSetEndDate(final Date endDate) {
        navigateToScheduleDuration();

        scheduleDurationSelector(schedulePage.endDateCalendar(),endDate);
        //schedulePage.endDateCalendar().click();

        schedulePage.continueButton().click();
        Waits.loadOrFadeWait();
        schedulePage.repeatWithFrequencyBelow().click();
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



}


