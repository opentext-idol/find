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
        searchBase.expandFilter(SearchBase.Filter.FIELD_TEXT);
        searchBase.loadOrFadeWait();
        searchBase.fieldTextAddButton().click();
        searchBase.fieldTextInput().sendKeys(fieldText);
        searchBase.fieldTextTickConfirm().click();
        searchBase.loadOrFadeWait();
    }

    public void clear(SearchBase searchBase) {
        searchBase.expandFilter(SearchBase.Filter.FIELD_TEXT);
        searchBase.loadOrFadeWait();
        try {
            searchBase.fieldTextRemoveButton().click();
            searchBase.loadOrFadeWait();
        } catch (ElementNotVisibleException e) {
            logger.warn("Field text already cleared");
        }
    }
}
