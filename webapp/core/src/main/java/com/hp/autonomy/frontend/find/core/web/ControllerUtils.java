/*
 * Copyright 2015-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Common logic within controller classes
 */
public interface ControllerUtils {
    String SPRING_CSRF_ATTRIBUTE = "_csrf";

    String convertToJson(final Object object) throws JsonProcessingException;

    String getMessage(String code, Object[] args) throws NoSuchMessageException;

    ModelAndView buildErrorModelAndView(final ErrorModelAndViewInfo errorInfo);
}
