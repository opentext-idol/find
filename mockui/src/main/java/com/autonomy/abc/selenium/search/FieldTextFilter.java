package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.page.search.SearchBase;
import org.openqa.selenium.ElementNotVisibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FieldTextFilter implements SearchFilter {
    private String fieldText;

    public FieldTextFilter(String fieldText) {
        this.fieldText = fieldText;
    }

    @Override
    public void apply(SearchFilter.Filterable page) {
        if (page instanceof SearchBase) {
            ((SearchBase) page).setFieldText(fieldText);
        }
    }

    public void clear(SearchFilter.Filterable page) {
        if (page instanceof SearchBase) {
            ((SearchBase) page).clearFieldText();
        }
    }
}
