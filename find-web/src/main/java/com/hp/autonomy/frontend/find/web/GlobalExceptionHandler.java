/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.web;

import com.hp.autonomy.hod.client.api.authentication.HodAuthenticationFailedException;
import com.hp.autonomy.hod.client.error.HodErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ResponseBody
    public ErrorResponse requestMethodNotSupportedHandler(final HttpRequestMethodNotSupportedException exception) throws Exception {
        return handler(exception);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse messageNotReadableHandler(final HttpMessageNotReadableException exception) throws Exception {
        return handler(exception);
    }

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
        final ResponseEntity<HodErrorResponse> entity;

        if (exception.isServerError()) {
            entity = new ResponseEntity<>(hodErrorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            entity = new ResponseEntity<>(hodErrorResponse, HttpStatus.BAD_REQUEST);
        }

        return entity;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handler(final Exception exception) throws Exception {
        // boilerplate - see http://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc
        if (AnnotationUtils.findAnnotation(exception.getClass(), ResponseStatus.class) != null) {
            throw exception;
        }

        final ErrorResponse errorResponse = new ErrorResponse(exception.getMessage());

        log.error("Unhandled exception with uuid {}", errorResponse.getUuid());
        log.error("Stack trace", exception);

        return errorResponse;
    }
}
