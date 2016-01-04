package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.element.FormInput;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringDateFilter implements SearchFilter {
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final StringDateHandler fromHandler = new StringDateHandler();
    private final StringDateHandler untilHandler = new StringDateHandler();

    public StringDateFilter from(Date date) {
        fromHandler.date = date;
        return this;
    }

    public StringDateFilter until(Date date) {
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
        fromHandler.applyTo(dateFilterable.fromDateInput());
        untilHandler.applyTo(dateFilterable.untilDateInput());
        loseFocus(dateFilterable);
    }

    // filter is only applied when element loses focus
    private void loseFocus(Filterable dateFilterable) {
        dateFilterable.fromDateInput().getElement().click();
        dateFilterable.untilDateInput().getElement().click();
    }

    @Override
    public String toString() {
        return "DatePickerFilter:" + fromHandler + "-" + untilHandler;
    }

    public interface Filterable {
        FormInput fromDateInput();
        FormInput untilDateInput();
    }

    private static class StringDateHandler {
        private Date date;

        private void applyTo(FormInput dateInput) {
            if (date != null) {
                dateInput.setValue(FORMAT.format(date));
            }
        }

        @Override
        public String toString() {
            return date == null ? "null" : FORMAT.format(date);
        }
    }
}
