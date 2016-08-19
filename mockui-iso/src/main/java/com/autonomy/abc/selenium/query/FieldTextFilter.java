package com.autonomy.abc.selenium.query;

import com.autonomy.abc.selenium.search.SearchBase;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.WebElement;

public class FieldTextFilter implements QueryFilter {
    private final String fieldText;

    public FieldTextFilter(final String fieldText) {
        this.fieldText = fieldText;
    }

    @Override
    public void apply(final QueryFilter.Filterable page) {
        if (page instanceof SearchBase) {
            final SearchBase searchBase = (SearchBase) page;
            searchBase.expand(SearchBase.Facet.FIELD_TEXT);
            try {
                searchBase.fieldTextAddButton().click();
                Waits.loadOrFadeWait();
            } catch (final ElementNotVisibleException e) {
			/* already clicked */
            }

            final WebElement editButton = searchBase.fieldTextEditButton();

            if(editButton.isDisplayed()) {
                searchBase.fieldTextEditButton().click();
            }
            
            searchBase.fieldTextInput().setAndSubmit(fieldText);
        }
    }

    public void clear(final QueryFilter.Filterable page) {
        if (page instanceof SearchBase) {
            ((SearchBase) page).clearFieldText();
        }
    }

    @Override
    public String toString() {
        return "FieldTextFilter:" + fieldText;
    }
}
