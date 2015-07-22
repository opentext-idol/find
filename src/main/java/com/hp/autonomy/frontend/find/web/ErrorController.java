/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.web;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

@Controller
@Slf4j
public class ErrorController {

    public static final String CLIENT_AUTHENTICATION_ERROR = "/client-authentication-error";

    @RequestMapping("/authentication-error")
    public ModelAndView authenticationErrorPage(final HttpServletRequest request) throws ServletException, IOException {
        return buildModelAndView(request, "error.authenticationErrorMain", "error.authenticationErrorSub", null);
    }

    @RequestMapping(CLIENT_AUTHENTICATION_ERROR)
    public ModelAndView clientAuthenticationErrorPage(final HttpServletRequest request) throws ServletException, IOException {
        return buildModelAndView(request, "error.clientAuthenticationErrorMain", "error.clientAuthenticationErrorSub", null);
    }

    @RequestMapping("/server-error")
    public ModelAndView serverErrorPage(final HttpServletRequest request) {
        final Exception exception = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        final String subMessageCode;
        final Object[] subMessageArguments;

        if (exception != null) {
            final UUID uuid = UUID.randomUUID();
            log.error("Exception with UUID {}", uuid);
            log.error("Stack trace", exception);
            subMessageCode = "error.internalServerErrorSub";
            subMessageArguments = new Object[]{uuid};
        } else {
            subMessageCode = "error.internalServerErrorSub.noUuid";
            subMessageArguments = null;
        }

        return buildModelAndView(request, "error.internalServerErrorMain", subMessageCode, subMessageArguments);
    }

    @RequestMapping("/not-found-error")
    public ModelAndView notFoundError(final HttpServletRequest request) {
        return buildModelAndView(request, "error.notFoundMain", "error.notFoundSub", null);
    }

    private ModelAndView buildModelAndView(
            final HttpServletRequest request,
            final String mainMessageCode,
            final String subMessageCode,
            final Object[] subMessageArguments
    ) {
        final ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("mainMessageCode", mainMessageCode);
        modelAndView.addObject("subMessageArguments", subMessageArguments);
        modelAndView.addObject("subMessageCode", subMessageCode);
        modelAndView.addObject("baseUrl", getBaseUrl(request));

        return modelAndView;
    }

    private String getBaseUrl(final HttpServletRequest request) {
        final String originalUri = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
        final String requestUri;

        if (originalUri != null) {
            requestUri = originalUri;
        }
        else {
            requestUri = request.getRequestURI();
        }

        final String path = requestUri.replaceFirst(request.getContextPath(), "");

        final int depth = StringUtils.countMatches(path, "/") - 1;

        final String baseUrl;

        if (depth <= 0) {
            baseUrl = ".";
        } else {
            baseUrl = StringUtils.repeat("../", depth);
        }

        return baseUrl;
    }

}
