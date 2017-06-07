/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.customization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum Status {
    /**
     * Error creating directory or listing directory
     */
    DIRECTORY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),

    /**
     * File with given name already exists
     */
    FILE_EXISTS(HttpStatus.CONFLICT),

    /**
     * An IOException was thrown while writing the file
     */
    IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),

    /**
     * The file was missing or of an incorrect MIME type
     */
    INVALID_FILE(HttpStatus.UNSUPPORTED_MEDIA_TYPE),

    /**
     * The file was uploaded successfully
     */
    SUCCESS(HttpStatus.OK);

    @JsonIgnore
    private final HttpStatus httpStatus;

    Status(final HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
