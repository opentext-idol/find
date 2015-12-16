package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.element.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DatePickerFilter implements SearchFilter {
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final DatePickerHandler fromHandler = new DatePickerHandler();
    private final DatePickerHandler untilHandler = new DatePickerHandler();

    public DatePickerFilter from(Date date) {
        fromHandler.date = date;
        return this;
    }

    public DatePickerFilter until(Date date) {
        untilHandler.date = date;
        return this;
    }

    @Override
    public void apply(SearchFilter.Filterable searchBase) {
        if (searchBase instanceof Filterable) {
            apply((Filterable) searchBase);
        }
    }

    protected void apply(Filterable dateFilterable) {
        fromHandler.applyTo(dateFilterable.fromDatePicker());
        untilHandler.applyTo(dateFilterable.untilDatePicker());
    }

    public interface Filterable {
        DatePicker fromDatePicker();
        DatePicker untilDatePicker();
    }

    private static class DatePickerHandler {
        private Date date;

        private void applyTo(DatePicker datePicker) {
            if (date != null) {
                datePicker.calendarDateSelect(date);
            }
        }
    }
}
