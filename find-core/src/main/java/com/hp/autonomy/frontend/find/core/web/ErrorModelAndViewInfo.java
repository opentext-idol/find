/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

@Data
public class ErrorModelAndViewInfo {
    private final HttpServletRequest request;
    private final String mainMessageCode;
    private final String subMessageCode;
    private final Object[] subMessageArguments;
    private final Integer statusCode;
    private final boolean contactSupport;
    private final URI buttonHref;
    private final Exception exception;
    private final boolean isAuthError;

    @Accessors(chain = true)
    @NoArgsConstructor
    @Setter
    public static class Builder {
        private HttpServletRequest request;
        private String mainMessageCode;
        private String subMessageCode;
        private Object[] subMessageArguments;
        private Integer statusCode;
        private boolean contactSupport;
        private URI buttonHref;
        private Exception exception;
        private boolean isAuthError;

        public ErrorModelAndViewInfo build() {
            return new ErrorModelAndViewInfo(request, mainMessageCode, subMessageCode, subMessageArguments, statusCode, contactSupport, buttonHref, exception, isAuthError);
        }
    }
}
