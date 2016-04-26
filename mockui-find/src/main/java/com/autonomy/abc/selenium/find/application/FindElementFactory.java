package com.autonomy.abc.selenium.find.application;

import com.hp.autonomy.frontend.selenium.application.ElementFactoryBase;
import com.hp.autonomy.frontend.selenium.application.PageMapper;
import org.openqa.selenium.WebDriver;

public abstract class FindElementFactory extends ElementFactoryBase {
    protected FindElementFactory(WebDriver driver, PageMapper<?> mapper) {
        super(driver, mapper);
    }
}
