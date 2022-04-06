/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */
package com.hp.autonomy.frontend.find.idol.web;

import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.CustomErrorController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

/**
 * On premise error page handling
 */
@Controller
class IdolErrorController extends CustomErrorController {
    @Autowired
    public IdolErrorController(final ControllerUtils controllerUtils) {
        super(controllerUtils);
    }

    @Override
    protected URI getAuthenticationErrorUrl(final HttpServletRequest request) {
        return getErrorUrl(request);
    }
}
