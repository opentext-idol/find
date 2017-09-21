package com.autonomy.abc.selenium.find.application;

import org.openqa.selenium.WebDriver;

public class FindHodFind extends HodFind<FindHodFindElementFactory> {

    @Override
    public void withDriver(final WebDriver webDriver) {
        setElementFactory(new FindHodFindElementFactory(webDriver));
    }
}
