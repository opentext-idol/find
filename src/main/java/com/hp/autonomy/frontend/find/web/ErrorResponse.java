package com.hp.autonomy.frontend.find.web;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/*
 * $Id: $
 *
 * Copyright (c) 2014, Autonomy Systems Ltd.
 *
 * Last modified by $Author: $ on $Date: $
 */
@Getter
@EqualsAndHashCode
class ErrorResponse {

    private final UUID uuid = UUID.randomUUID();
    private final String message;

    public ErrorResponse(final String message) {
        this.message = message;
    }

}