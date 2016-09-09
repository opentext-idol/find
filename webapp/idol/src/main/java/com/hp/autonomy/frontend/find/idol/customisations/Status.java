/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customisations;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum Status {
    DIRECTORY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_EXISTS(HttpStatus.CONFLICT),
    IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_FILE(HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    SUCCESS(HttpStatus.OK);

    @JsonIgnore
    private final HttpStatus httpStatus;

    Status(final HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
