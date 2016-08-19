/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.web;

import com.hp.autonomy.frontend.find.core.web.ErrorResponse;
import com.hp.autonomy.frontend.find.core.web.GlobalExceptionHandler;
import com.hp.autonomy.hod.client.api.authentication.HodAuthenticationFailedException;
import com.hp.autonomy.hod.client.error.HodErrorCode;
import com.hp.autonomy.hod.client.error.HodErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.EnumSet;
import java.util.Set;

@Slf4j
@ControllerAdvice
public class HodGlobalExceptionHandler extends GlobalExceptionHandler {
    private final Set<HodErrorCode> userErrors = EnumSet.of(
            HodErrorCode.INVALID_QUERY_TEXT,
            HodErrorCode.NO_IGNORE_SPECIALS,
            HodErrorCode.INVALID_FIELD_VALUE,
            HodErrorCode.INPUT_TOO_LONG,
            HodErrorCode.REACHED_MAXIMUM_RUNS_IN_24_HOURS,
            HodErrorCode.QUERY_PROFILE_NAME_INVALID
    );

    @ExceptionHandler(HodAuthenticationFailedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorResponse authenticationFailedHandler(final HodAuthenticationFailedException exception) {
        return new ErrorResponse("TOKEN HAS EXPIRED");
    }

    @ExceptionHandler(HodErrorException.class)
    @ResponseBody
    public ResponseEntity<HodErrorResponse> hodErrorHandler(final HodErrorException exception) {
        final HodErrorResponse hodErrorResponse = new HodErrorResponse("HOD Error", exception.getErrorCode());

        if (!userErrors.contains(exception.getErrorCode())) {
            log.error("Unhandled HodErrorException with uuid {}", hodErrorResponse.getUuid());
            log.error("Stack trace", exception);
        }

        return new ResponseEntity<>(hodErrorResponse, exception.isServerError() ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST);
    }
}
