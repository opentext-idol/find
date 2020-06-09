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

package com.hp.autonomy.frontend.find.hod.view;

import com.hp.autonomy.frontend.find.core.view.ViewController;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo;
import com.hp.autonomy.hod.client.api.authentication.HodAuthenticationFailedException;
import com.hp.autonomy.hod.client.api.resource.ResourceName;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.view.HodViewRequest;
import com.hp.autonomy.searchcomponents.hod.view.HodViewRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.view.HodViewServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(ViewController.VIEW_PATH)
@Slf4j
class HodViewController extends ViewController<HodViewRequest, ResourceName, HodErrorException> {
    private static final String HOD_ERROR_MESSAGE_CODE_PREFIX = "error.iodErrorCode.";
    private static final String HOD_ERROR_MESSAGE_CODE_MAIN = "error.iodErrorMain";
    private static final String HOD_ERROR_MESSAGE_CODE_SUB = "error.iodErrorSub";
    private static final String HOD_ERROR_MESSAGE_CODE_SUB_NULL = "error.iodErrorSubNull";
    private static final String HOD_ERROR_MESSAGE_CODE_TOKEN_EXPIRED = "error.iodTokenExpired";
    private static final String HOD_ERROR_MESSAGE_CODE_INTERNAL_MAIN = "error.internalServerErrorMain";
    private static final String HOD_ERROR_MESSAGE_CODE_INTERNAL_SUB = "error.internalServerErrorSub";
    private static final String HOD_ERROR_MESSAGE_CODE_UNKNOWN = "error.unknownError";

    private final ControllerUtils controllerUtils;

    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    public HodViewController(final HodViewServerService viewServerService,
                             final ObjectFactory<HodViewRequestBuilder> viewRequestBuilderFactory,
                             final ControllerUtils controllerUtils) {
        super(viewServerService, viewRequestBuilderFactory);
        this.controllerUtils = controllerUtils;
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @ExceptionHandler
    public ModelAndView handleHodErrorException(
            final HodErrorException e,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        response.reset();

        log.error("HodErrorException thrown while viewing document", e);

        final String errorKey = HOD_ERROR_MESSAGE_CODE_PREFIX + e.getErrorCode();
        String hodErrorMessage;

        try {
            hodErrorMessage = controllerUtils.getMessage(errorKey, null);
        } catch (final NoSuchMessageException ignored) {
            // we don't have a key in the bundle for this error code
            hodErrorMessage = controllerUtils.getMessage(HOD_ERROR_MESSAGE_CODE_UNKNOWN, null);
        }

        final int errorCode = e.isServerError() ? HttpServletResponse.SC_INTERNAL_SERVER_ERROR : HttpServletResponse.SC_BAD_REQUEST;

        final String subMessageCode;
        final Object[] subMessageArgs;
        if (hodErrorMessage != null) {
            subMessageCode = HOD_ERROR_MESSAGE_CODE_SUB;
            subMessageArgs = new String[]{hodErrorMessage};
        } else {
            subMessageCode = HOD_ERROR_MESSAGE_CODE_SUB_NULL;
            subMessageArgs = null;
        }

        response.setStatus(errorCode);

        return controllerUtils.buildErrorModelAndView(new ErrorModelAndViewInfo.Builder()
                .setRequest(request)
                .setMainMessageCode(HOD_ERROR_MESSAGE_CODE_MAIN)
                .setSubMessageCode(subMessageCode)
                .setSubMessageArguments(subMessageArgs)
                .setStatusCode(errorCode)
                .setContactSupport(true)
                .setException(e)
                .build());
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @ExceptionHandler
    public ModelAndView hodAuthenticationFailedException(
            final HodAuthenticationFailedException e,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        response.reset();
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        log.error("HodAuthenticationFailedException thrown while viewing document", e);

        return controllerUtils.buildErrorModelAndView(new ErrorModelAndViewInfo.Builder()
                .setRequest(request)
                .setMainMessageCode(HOD_ERROR_MESSAGE_CODE_MAIN)
                .setSubMessageCode(HOD_ERROR_MESSAGE_CODE_TOKEN_EXPIRED)
                .setStatusCode(HttpServletResponse.SC_FORBIDDEN)
                .setAuthError(true)
                .build());
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGeneralException(
            final Exception e,
            final HttpServletRequest request,
            final ServletResponse response
    ) {
        response.reset();

        return controllerUtils.buildErrorModelAndView(new ErrorModelAndViewInfo.Builder()
                .setRequest(request)
                .setMainMessageCode(HOD_ERROR_MESSAGE_CODE_INTERNAL_MAIN)
                .setSubMessageCode(HOD_ERROR_MESSAGE_CODE_INTERNAL_SUB)
                .setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                .setContactSupport(true)
                .setException(e)
                .build());
    }
}
