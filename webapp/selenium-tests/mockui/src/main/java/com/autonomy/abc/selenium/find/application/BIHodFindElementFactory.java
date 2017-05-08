/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find.application;

import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.IdolFindPage.Factory;
import com.hp.autonomy.frontend.selenium.element.FormInput;
import org.openqa.selenium.WebDriver;

public class BIHodFindElementFactory extends HodFindElementFactory {
    protected BIHodFindElementFactory(final WebDriver driver) {
        super(driver);
    }

    @Override
    public FormInput getSearchBox() {
        return getConceptsPanel().getConceptBoxInput();
    }

    @Override
    public FindPage getFindPage() {
        return new Factory().create(getDriver());
    }
}
