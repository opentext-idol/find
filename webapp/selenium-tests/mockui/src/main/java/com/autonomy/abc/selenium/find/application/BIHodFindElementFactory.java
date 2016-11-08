package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.IdolFindPage;
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

    @Override
    public FindPage getFindPage() {
        return new IdolFindPage.Factory().create(getDriver());
    }
}
