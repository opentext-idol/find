/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.web;

import com.hp.autonomy.frontend.find.core.web.AbstractErrorControllerTest;
import org.junit.Before;

import java.net.MalformedURLException;

public class IdolErrorControllerTest extends AbstractErrorControllerTest {
    @Override
    @Before
    public void setUp() throws MalformedURLException {
        errorController = new IdolErrorController(controllerUtils);
        super.setUp();
    }
}
