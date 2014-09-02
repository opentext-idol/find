package com.hp.autonomy.frontend.find.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/*
 * $Id: $
 *
 * Copyright (c) 2014, Autonomy Systems Ltd.
 *
 * Last modified by $Author: $ on $Date: $
 */
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