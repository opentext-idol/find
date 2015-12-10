/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.web;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.web.ErrorResponse;
import com.hp.autonomy.frontend.find.core.web.GlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class IdolGlobalExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(AciErrorException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> hodErrorHandler(final AciErrorException exception) {
        final ErrorResponse errorResponse = new ErrorResponse(exception.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
