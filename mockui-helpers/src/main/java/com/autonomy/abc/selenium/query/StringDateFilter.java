package com.autonomy.abc.selenium.query;

import com.hp.autonomy.frontend.selenium.element.FormInput;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringDateFilter implements QueryFilter {
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final StringDateHandler fromHandler = new StringDateHandler();
    private final StringDateHandler untilHandler = new StringDateHandler();

    public StringDateFilter from(final Date date) {
        fromHandler.date = date;
        return this;
    }

    public StringDateFilter until(final Date date) {
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
        fromHandler.applyTo(dateFilterable, dateFilterable.fromDateInput());
        untilHandler.applyTo(dateFilterable, dateFilterable.untilDateInput());
        loseFocus(dateFilterable);
    }

    // filter is only applied when element loses focus
    private void loseFocus(final Filterable dateFilterable) {
        dateFilterable.fromDateInput().getElement().click();
        dateFilterable.untilDateInput().getElement().click();
    }

    @Override
    public String toString() {
        return "DatePickerFilter:" + fromHandler + '-' + untilHandler;
    }

    public interface Filterable {
        FormInput fromDateInput();
        FormInput untilDateInput();
        String formatInputDate(Date date);
    }

    private static class StringDateHandler {
        private Date date;

        private void applyTo(final Filterable formatter, final FormInput dateInput) {
            if (date != null) {
                dateInput.setValue(formatter.formatInputDate(date));
            }
        }

        @Override
        public String toString() {
            return String.valueOf(date);
        }
    }
}
