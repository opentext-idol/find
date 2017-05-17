/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.autonomy.abc.selenium.find.application;

import com.hp.autonomy.frontend.selenium.application.Application;
import com.hp.autonomy.frontend.selenium.control.Window;

public class BIHodFind extends HodFind<BIHodFindElementFactory> {
    @Override
    public Application<BIHodFindElementFactory> inWindow(final Window window) {
        setElementFactory(new BIHodFindElementFactory(window.getSession().getDriver()));
        return this;
    }
}
