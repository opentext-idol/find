package com.autonomy.abc.selenium.query;

import com.hp.autonomy.frontend.selenium.element.DatePicker;

import java.util.Date;

public class DatePickerFilter implements QueryFilter {
    private final DatePickerHandler fromHandler = new DatePickerHandler();
    private final DatePickerHandler untilHandler = new DatePickerHandler();

    public DatePickerFilter from(final Date date) {
        fromHandler.date = date;
        return this;
    }

    public DatePickerFilter until(final Date date) {
        untilHandler.date = date;
        return this;
    }

    @Override
    public void apply(final QueryFilter.Filterable searchBase) {
        if (searchBase instanceof Filterable) {
            apply((Filterable) searchBase);
        }
    }

    protected void apply(final Filterable dateFilterable) {
        fromHandler.applyTo(dateFilterable.fromDatePicker());
        untilHandler.applyTo(dateFilterable.untilDatePicker());
    }

    @Override
    public String toString() {
        return "DatePickerFilter:" + fromHandler + "-" + untilHandler;
    }

    public interface Filterable {
        DatePicker fromDatePicker();
        DatePicker untilDatePicker();
    }

    private static class DatePickerHandler {
        private Date date;

        private void applyTo(final DatePicker datePicker) {
            if (date != null) {
                datePicker.open();
                datePicker.calendarDateSelect(date);
                datePicker.close();
            }
        }

        @Override
        public String toString() {
            return String.valueOf(date);
        }
    }
}
