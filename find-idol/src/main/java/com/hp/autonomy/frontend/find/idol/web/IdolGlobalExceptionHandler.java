/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.web;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.web.ErrorResponse;
import com.hp.autonomy.frontend.find.core.web.GlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@ControllerAdvice
public class IdolGlobalExceptionHandler extends GlobalExceptionHandler {
    private static final String SECURITY_INFO_TOKEN_EXPIRED_ID = "AXEQUERY538";

    @ExceptionHandler(AciErrorException.class)
    @ResponseBody
    public ErrorResponse authenticationFailedHandler(final AciErrorException exception, final HttpServletResponse response) {
        if (SECURITY_INFO_TOKEN_EXPIRED_ID.equals(exception.getErrorId())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return new ErrorResponse("Security Info has expired");
        }

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return handler(exception);
    }
}
