/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Common logic within controller classes
 */
public interface ControllerUtils {
    String SPRING_CSRF_ATTRIBUTE = "_csrf";

    String convertToJson(final Object object) throws JsonProcessingException;

    @SuppressWarnings("MethodWithTooManyParameters")
    ModelAndView buildErrorModelAndView(
            HttpServletRequest request,
            String mainMessageCode,
            String subMessageCode,
            Object[] subMessageArguments,
            Integer statusCode,
            boolean contactSupport
    );
}
