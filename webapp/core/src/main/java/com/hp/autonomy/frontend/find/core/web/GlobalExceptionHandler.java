/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public abstract class GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ResponseBody
    public ErrorResponse requestMethodNotSupportedHandler(final HttpRequestMethodNotSupportedException exception) throws HttpRequestMethodNotSupportedException {
        return handler(exception);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse messageNotReadableHandler(final HttpMessageNotReadableException exception) throws HttpMessageNotReadableException {
        return handler(exception);
    }

    @ExceptionHandler(ClientAbortException.class)
    @ResponseBody
    public ErrorResponse connectionAbort(final ClientAbortException e) {
        // Tomcat-specific exception when existing request is aborted by client
        log.debug("Client aborted connection", e);
        return null;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public <E extends Exception> ErrorResponse handler(final E exception) throws E {
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
