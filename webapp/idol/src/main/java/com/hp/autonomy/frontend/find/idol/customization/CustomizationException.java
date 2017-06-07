/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customization;

import lombok.Getter;

@Getter
class CustomizationException extends Exception {
    private static final long serialVersionUID = -393590873895440300L;

    private final Status status;

    CustomizationException(final Status status, final String message) {
        super(message);

        this.status = status;
    }
}
