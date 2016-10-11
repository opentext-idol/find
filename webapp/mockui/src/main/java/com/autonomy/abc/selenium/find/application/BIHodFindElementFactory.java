package com.autonomy.abc.selenium.find.application;

import com.hp.autonomy.frontend.selenium.element.FormInput;
import org.openqa.selenium.WebDriver;

public class BIHodFindElementFactory extends HodFindElementFactory {
    protected BIHodFindElementFactory(WebDriver driver) {
        super(driver);
    }

    @Override
    public FormInput getSearchBox() {
        return getConceptsPanel().getConceptBoxInput();
    }
}
