/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find.application;

import org.openqa.selenium.WebDriver;

public class BIHodFind extends HodFind<BIHodFindElementFactory> {
    @Override
    public void withDriver(final WebDriver webDriver) {
        setElementFactory(new BIHodFindElementFactory(webDriver));
    }
}
