/*
 * Copyright 2015 Open Text.
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

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.util.BindErrorUtils;

@Slf4j
@ControllerAdvice
public abstract class GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ResponseBody
    public ErrorResponse requestMethodNotSupportedHandler(final HttpRequestMethodNotSupportedException exception) throws HttpRequestMethodNotSupportedException {
        return handler(exception, false);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse messageNotReadableHandler(final HttpMessageNotReadableException exception) throws HttpMessageNotReadableException {
        return handler(exception, false);
    }

    @ExceptionHandler(ClientAbortException.class)
    @ResponseBody
    public ErrorResponse connectionAbort(final ClientAbortException e) {
        // Tomcat-specific exception when existing request is aborted by client
        log.debug("Client aborted connection", e);
        return null;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse methodArgumentNotValidHandler(final MethodArgumentNotValidException e) {
        final String message = e.getParameter() + ": " + BindErrorUtils.resolveAndJoin(e.getFieldErrors());
        return new ErrorResponse(message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse methodArgumentTypeMismatchHandler(final MethodArgumentTypeMismatchException e) {
        final String message = "parameter '" + e.getName() + "': wrong type";
        return new ErrorResponse(message);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse noResourceFoundHandler(final NoResourceFoundException e) throws NoResourceFoundException {
        return handler(e, false);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handlerMethodValidationHandler(final HandlerMethodValidationException e) {
        final var res = e.getParameterValidationResults().get(0);
        final String message = "parameter '" + res.getMethodParameter().getParameterName() + "': " +
                BindErrorUtils.resolveAndJoin(res.getResolvableErrors());
        return new ErrorResponse(message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public <E extends Exception> ErrorResponse handler(final E exception) throws E {
        return handler(exception, true);
    }

    private <E extends Exception> ErrorResponse handler(final E exception, final boolean internal) throws E {
        // boilerplate - see http://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc
        if(AnnotationUtils.findAnnotation(exception.getClass(), ResponseStatus.class) != null) {
            throw exception;
        }

        final ErrorResponse errorResponse = new ErrorResponse(exception.getMessage());

        if (internal) {
            log.error("Unhandled exception with uuid {}", errorResponse.getUuid());
            log.error("Stack trace", exception);
        }

        return errorResponse;
    }
}
