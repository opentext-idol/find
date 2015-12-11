package com.autonomy.abc.selenium.search;

import com.autonomy.abc.selenium.page.search.SearchBase;
import org.openqa.selenium.ElementNotVisibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FieldTextFilter implements SearchFilter {
    private Logger logger = LoggerFactory.getLogger(FieldTextFilter.class);
    private String fieldText;

    public FieldTextFilter(String fieldText) {
        this.fieldText = fieldText;
    }

    @Override
    public void apply(SearchBase searchBase) {
        searchBase.setFieldText(fieldText);
    }

    public void clear(SearchBase searchBase) {
        searchBase.clearFieldText();
    }
}
