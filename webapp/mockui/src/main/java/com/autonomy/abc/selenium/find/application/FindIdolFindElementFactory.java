package com.autonomy.abc.selenium.find.application;

import com.hp.autonomy.frontend.selenium.element.FormInput;
import org.openqa.selenium.WebDriver;

@SuppressWarnings("WeakerAccess")
public class FindIdolFindElementFactory extends IdolFindElementFactory{

    FindIdolFindElementFactory(final WebDriver driver){
        super(driver);
    }

    @Override
    public FormInput getSearchBox() {
        return getTopNavBar().getSearchBoxInput();
    }
}
