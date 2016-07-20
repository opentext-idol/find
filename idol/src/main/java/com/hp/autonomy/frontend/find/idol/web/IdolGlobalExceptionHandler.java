/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.web;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.web.ErrorResponse;
import com.hp.autonomy.frontend.find.core.web.GlobalExceptionHandler;
import com.hp.autonomy.searchcomponents.core.search.AutoCorrectException;
import com.hp.autonomy.types.requests.Spelling;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@ControllerAdvice
public class IdolGlobalExceptionHandler extends GlobalExceptionHandler {
    private static final String SECURITY_INFO_TOKEN_EXPIRED_ID = "AXEQUERY538";

    //This is not an exhaustive list (if adding do not forget to add DAH versions of all AXE errors)
    private Set<String> userErrors = new HashSet<>(Arrays.asList(
            "AXEQUERY502",
            "DAHQUERY502",
            "AXEQUERY504",
            "DAHQUERY504",
            "AXEQUERY505",
            "DAHQUERY505",
            "AXEQUERY507",
            "DAHQUERY507",
            "AXEQUERY508",
            "DAHQUERY508",
            "AXEQUERY509",
            "DAHQUERY509",
            "AXEQUERY511",
            "DAHQUERY511",
            "AXEQUERY512",
            "DAHQUERY512",
            "AXEQUERY513",
            "DAHQUERY513",
            "AXEGETQUERYTAGVALUES502",
            "DAHGETQUERYTAGVALUES502",
            "AXEGETQUERYTAGVALUES507",
            "DAHGETQUERYTAGVALUES507",
            "AXEGETQUERYTAGVALUES508",
            "DAHGETQUERYTAGVALUES508",
            "AXEGETQUERYTAGVALUES519",
            "DAHGETQUERYTAGVALUES519",
            "AXEGETQUERYTAGVALUES520",
            "DAHGETQUERYTAGVALUES520",
            "AXEGETQUERYTAGVALUES522",
            "DAHGETQUERYTAGVALUES522",
            "AXEGETQUERYTAGVALUES538",
            "DAHGETQUERYTAGVALUES538",
            "QMSQUERY-2147435967",
            "QMSQUERY-2147435888",
            "AXEGETQUERYTAGVALUES504",
            "DAHGETQUERYTAGVALUES504",
            "AXEGETQUERYTAGVALUES509",
            "DAHGETQUERYTAGVALUES509",
            "AXEGETQUERYTAGVALUES512",
            "DAHGETQUERYTAGVALUES512",
            "AXEGETQUERYTAGVALUES513",
            "DAHGETQUERYTAGVALUES513"
    ));

    @ExceptionHandler(AciErrorException.class)
    @ResponseBody
    public ErrorResponse authenticationFailedHandler(final AciErrorException exception, final HttpServletResponse response) {
        if (SECURITY_INFO_TOKEN_EXPIRED_ID.equals(exception.getErrorId())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return new ErrorResponse("Security Info has expired");
        }

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        final ErrorResponse errorResponse = new ErrorResponse(exception.getMessage());

        if (!userErrors.contains(exception.getErrorId())) {
            log.error("Unhandled Idol Error with uuid {}", errorResponse.getUuid());
            log.error("Stack trace", exception);
        }

        return errorResponse;
    }

    @ExceptionHandler(AutoCorrectException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public SpellingErrorResponse invalidSpellingCorrection(final AutoCorrectException e) {
        return new SpellingErrorResponse(e.getMessage(), e.getSpelling());
    }

    @Getter
    private final class SpellingErrorResponse extends ErrorResponse{
        private Spelling autoCorrection;

        public SpellingErrorResponse(final String message, final Spelling autoCorrection) {
            super(message);
            this.autoCorrection = autoCorrection;
        }
    }
}
