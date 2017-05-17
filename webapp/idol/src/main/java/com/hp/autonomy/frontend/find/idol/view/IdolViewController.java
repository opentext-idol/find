/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.view;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.view.ViewController;
import com.hp.autonomy.frontend.find.core.web.ControllerUtils;
import com.hp.autonomy.frontend.find.core.web.ErrorModelAndViewInfo;
import com.hp.autonomy.frontend.logging.Markers;
import com.hp.autonomy.searchcomponents.idol.view.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(IdolViewController.VIEW_PATH)
@Slf4j
class IdolViewController extends ViewController<IdolViewRequest, String, AciErrorException> {
    private final ControllerUtils controllerUtils;

    @SuppressWarnings("TypeMayBeWeakened")
    @Autowired
    public IdolViewController(
            final IdolViewServerService viewServerService,
            final ObjectFactory<IdolViewRequestBuilder> viewRequestBuilder,
            final ControllerUtils controllerUtils
    ) {
        super(viewServerService, viewRequestBuilder);
        this.controllerUtils = controllerUtils;
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleViewDocumentNotFoundException(
            final ViewDocumentNotFoundException e,
            final HttpServletRequest request,
            final ServletResponse response
    ) {
        response.reset();

        final String reference = e.getReference();

        log.info(Markers.AUDIT, "TRIED TO VIEW NON EXISTENT DOCUMENT WITH REFERENCE {}", reference);

        return controllerUtils.buildErrorModelAndView(new ErrorModelAndViewInfo.Builder()
                .setRequest(request)
                .setMainMessageCode("error.documentNotFound")
                .setSubMessageCode("error.referenceDoesNotExist")
                .setSubMessageArguments(new Object[]{reference})
                .setStatusCode(HttpStatus.NOT_FOUND.value())
                .setContactSupport(true)
                .setException(e)
                .build());
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleViewServerErrorException(
            final ViewServerErrorException e,
            final HttpServletRequest request,
            final ServletResponse response
    ) {
        response.reset();

        final String reference = e.getReference();

        log.info(Markers.AUDIT, "TRIED TO VIEW DOCUMENT WITH REFERENCE {} BUT VIEW SERVER RETURNED AN ERROR PAGE", reference);

        return controllerUtils.buildErrorModelAndView(new ErrorModelAndViewInfo.Builder()
                .setRequest(request)
                .setMainMessageCode("error.viewServerErrorMain")
                .setSubMessageCode("error.viewServerErrorSub")
                .setSubMessageArguments(new Object[]{reference})
                .setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setContactSupport(true)
                .setException(e)
                .build());
    }
}
