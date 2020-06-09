/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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
