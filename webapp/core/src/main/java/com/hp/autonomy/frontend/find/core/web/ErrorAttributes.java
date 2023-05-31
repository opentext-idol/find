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

public enum ErrorAttributes {
    MAIN_MESSAGE("mainMessage"),
    SUB_MESSAGE("subMessage"),
    BASE_URL("baseUrl"),
    STATUS_CODE("statusCode"),
    CONTACT_SUPPORT("contactSupport"),
    BUTTON_HREF("buttonHref"),
    BUTTON_MESSAGE("buttonMessage"),
    AUTH_ERROR("isAuthError");

    private final String value;

    ErrorAttributes(final String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
