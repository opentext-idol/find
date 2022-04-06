/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.web;

import com.hp.autonomy.frontend.find.core.beanconfiguration.DispatcherServletConfiguration;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

@Slf4j
public abstract class CustomErrorController {
    static final String MESSAGE_CODE_AUTHENTICATION_ERROR_MAIN = "error.authenticationErrorMain";
    static final String MESSAGE_CODE_AUTHENTICATION_ERROR_SUB = "error.authenticationErrorSub";
    static final String MESSAGE_CODE_INTERNAL_SERVER_ERROR_SUB = "error.internalServerErrorSub";
    static final String MESSAGE_CODE_INTERNAL_SERVER_ERROR_MAIN = "error.internalServerErrorMain";
    static final String MESSAGE_CODE_NOT_FOUND_MAIN = "error.notFoundMain";
    static final String MESSAGE_CODE_NOT_FOUND_SUB = "error.notFoundSub";

    protected final ControllerUtils controllerUtils;

    protected CustomErrorController(final ControllerUtils controllerUtils) {
        this.controllerUtils = controllerUtils;
    }

    protected abstract URI getAuthenticationErrorUrl(final HttpServletRequest request);

    protected URI getErrorUrl(final HttpServletRequest request) {
        return URI.create(request.getContextPath() + FindController.APP_PATH);
    }

    @RequestMapping(DispatcherServletConfiguration.AUTHENTICATION_ERROR_PATH)
    public ModelAndView authenticationErrorPage(final HttpServletRequest request, final HttpServletResponse response) {
        return controllerUtils.buildErrorModelAndView(
                new Builder()
                        .setRequest(request)
                        .setMainMessageCode(MESSAGE_CODE_AUTHENTICATION_ERROR_MAIN)
                        .setSubMessageCode(MESSAGE_CODE_AUTHENTICATION_ERROR_SUB)
                        .setStatusCode(response.getStatus())
                        .setButtonHref(getAuthenticationErrorUrl(request))
                        .setAuthError(true)
                        .build()
        );
    }

    @RequestMapping(DispatcherServletConfiguration.SERVER_ERROR_PATH)
    public ModelAndView serverErrorPage(final HttpServletRequest request, final HttpServletResponse response) {
        final Exception exception = (Exception)request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        return controllerUtils.buildErrorModelAndView(
                new Builder()
                        .setRequest(request)
                        .setMainMessageCode(MESSAGE_CODE_INTERNAL_SERVER_ERROR_MAIN)
                        .setSubMessageCode(MESSAGE_CODE_INTERNAL_SERVER_ERROR_SUB)
                        .setSubMessageArguments(null)
                        .setStatusCode(response.getStatus())
                        .setContactSupport(true)
                        .setButtonHref(getErrorUrl(request))
                        .setException(exception)
                        .build()
        );
    }

    @RequestMapping(DispatcherServletConfiguration.NOT_FOUND_ERROR_PATH)
    public ModelAndView notFoundError(final HttpServletRequest request, final HttpServletResponse response) {
        return controllerUtils.buildErrorModelAndView(
                new Builder()
                        .setRequest(request)
                        .setMainMessageCode(MESSAGE_CODE_NOT_FOUND_MAIN)
                        .setSubMessageCode(MESSAGE_CODE_NOT_FOUND_SUB)
                        .setStatusCode(response.getStatus())
                        .setContactSupport(true)
                        .setButtonHref(getErrorUrl(request))
                        .build()
        );
    }
}
