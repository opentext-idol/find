/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.hp.autonomy.frontend.find.core.beanconfiguration.DispatcherServletConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

@Slf4j
public abstract class CustomErrorController {
    static final String MESSAGE_CODE_AUTHENTICATION_ERROR_MAIN = "error.authenticationErrorMain";
    static final String MESSAGE_CODE_AUTHENTICATION_ERROR_SUB = "error.authenticationErrorSub";
    static final String MESSAGE_CODE_CLIENT_AUTHENTICATION_ERROR_MAIN = "error.clientAuthenticationErrorMain";
    static final String MESSAGE_CODE_CLIENT_AUTHENTICATION_ERROR_SUB = "error.clientAuthenticationErrorSub";
    static final String MESSAGE_CODE_INTERNAL_SERVER_ERROR_SUB = "error.internalServerErrorSub";
    static final String MESSAGE_CODE_INTERNAL_SERVER_ERROR_MAIN = "error.internalServerErrorMain";
    static final String MESSAGE_CODE_NOT_FOUND_MAIN = "error.notFoundMain";
    static final String MESSAGE_CODE_NOT_FOUND_SUB = "error.notFoundSub";
    static final String STATUS_CODE_PARAM = "statusCode";

    private final ControllerUtils controllerUtils;

    protected CustomErrorController(final ControllerUtils controllerUtils) {
        this.controllerUtils = controllerUtils;
    }

    protected abstract URI getAuthenticationErrorUrl(final HttpServletRequest request);

    protected abstract URI getErrorUrl(final HttpServletRequest request);

    @RequestMapping(DispatcherServletConfiguration.AUTHENTICATION_ERROR_PATH)
    public ModelAndView authenticationErrorPage(final HttpServletRequest request, final HttpServletResponse response) {
        return controllerUtils.buildErrorModelAndView(new ErrorModelAndViewInfo.Builder()
                .setRequest(request)
                .setMainMessageCode(MESSAGE_CODE_AUTHENTICATION_ERROR_MAIN)
                .setSubMessageCode(MESSAGE_CODE_AUTHENTICATION_ERROR_SUB)
                .setStatusCode(response.getStatus())
                .setButtonHref(getAuthenticationErrorUrl(request))
                .setAuthError(true)
                .build());
    }

    @RequestMapping(DispatcherServletConfiguration.CLIENT_AUTHENTICATION_ERROR_PATH)
    public ModelAndView clientAuthenticationErrorPage(
            @RequestParam(STATUS_CODE_PARAM) final int statusCode,
            final HttpServletRequest request
    ) {
        return controllerUtils.buildErrorModelAndView(new ErrorModelAndViewInfo.Builder()
                .setRequest(request)
                .setMainMessageCode(MESSAGE_CODE_CLIENT_AUTHENTICATION_ERROR_MAIN)
                .setSubMessageCode(MESSAGE_CODE_CLIENT_AUTHENTICATION_ERROR_SUB)
                .setStatusCode(statusCode)
                .setButtonHref(getAuthenticationErrorUrl(request))
                .build());
    }

    @RequestMapping(DispatcherServletConfiguration.SERVER_ERROR_PATH)
    public ModelAndView serverErrorPage(final HttpServletRequest request, final HttpServletResponse response) {
        final Exception exception = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        return controllerUtils.buildErrorModelAndView(new ErrorModelAndViewInfo.Builder()
                .setRequest(request)
                .setMainMessageCode(MESSAGE_CODE_INTERNAL_SERVER_ERROR_MAIN)
                .setSubMessageCode(MESSAGE_CODE_INTERNAL_SERVER_ERROR_SUB)
                .setSubMessageArguments(null)
                .setStatusCode(response.getStatus())
                .setContactSupport(true)
                .setButtonHref(getErrorUrl(request))
                .setException(exception)
                .build());
    }

    @RequestMapping(DispatcherServletConfiguration.NOT_FOUND_ERROR_PATH)
    public ModelAndView notFoundError(final HttpServletRequest request, final HttpServletResponse response) {
        return controllerUtils.buildErrorModelAndView(new ErrorModelAndViewInfo.Builder()
                .setRequest(request)
                .setMainMessageCode(MESSAGE_CODE_NOT_FOUND_MAIN)
                .setSubMessageCode(MESSAGE_CODE_NOT_FOUND_SUB)
                .setStatusCode(response.getStatus())
                .setContactSupport(true)
                .setButtonHref(getErrorUrl(request))
                .build());
    }
}
