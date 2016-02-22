/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.web;

import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.CustomErrorController;
import com.hp.autonomy.frontend.find.core.web.FindController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

/**
 * On premise error page handling
 */
@Controller
public class IdolErrorController extends CustomErrorController {
    public static final String BASE_SEARCH_URL = FindController.PUBLIC_PATH + "#find/search/query";

    @Autowired
    public IdolErrorController(final ControllerUtils controllerUtils) {
        super(controllerUtils);
    }

    @Override
    protected URI getAuthenticationErrorUrl(final HttpServletRequest request) {
        return getUrl(request);
    }

    @Override
    protected URI getErrorUrl(final HttpServletRequest request) {
        return getUrl(request);
    }

    private URI getUrl(final HttpServletRequest request) {
        return URI.create(request.getContextPath() + BASE_SEARCH_URL);
    }
}
