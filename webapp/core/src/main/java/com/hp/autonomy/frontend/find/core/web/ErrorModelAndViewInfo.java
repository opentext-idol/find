/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
    private final String mainMessage;
    private final String subMessage;
    private final String mainMessageCode;
    private final String subMessageCode;
    private final Object[] subMessageArguments;
    private final Integer statusCode;
    private final boolean contactSupport;
    private final URI buttonHref;
    private final Exception exception;
    private final boolean isAuthError;

    private ErrorModelAndViewInfo(final Builder builder) {
        request = builder.request;
        mainMessage = builder.mainMessage;
        subMessage = builder.subMessage;
        mainMessageCode = builder.mainMessageCode;
        subMessageCode = builder.subMessageCode;
        subMessageArguments = builder.subMessageArguments;
        statusCode = builder.statusCode;
        contactSupport = builder.contactSupport;
        buttonHref = builder.buttonHref;
        exception = builder.exception;
        isAuthError = builder.isAuthError;
    }

    @Accessors(chain = true)
    @NoArgsConstructor
    @Setter
    public static class Builder {
        private HttpServletRequest request;
        private String mainMessage;
        private String subMessage;
        private String mainMessageCode;
        private String subMessageCode;
        private Object[] subMessageArguments;
        private Integer statusCode;
        private boolean contactSupport;
        private URI buttonHref;
        private Exception exception;
        private boolean isAuthError;

        public ErrorModelAndViewInfo build() {
            return new ErrorModelAndViewInfo(this);
        }
    }
}
