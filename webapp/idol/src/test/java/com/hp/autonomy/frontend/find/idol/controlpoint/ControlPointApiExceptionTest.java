/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.controlpoint;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

public class ControlPointApiExceptionTest {

    @Test
    public void testKnownErrorCode() {
        final ControlPointApiException e = new ControlPointApiException(400,
            new ControlPointErrorResponse("Invalid Grant", "Wrong credentials"));
        Assert.assertEquals("should map to correct enum value",
            ControlPointApiException.ErrorId.INVALID_GRANT, e.getErrorId());
    }

    @Test
    public void testUnknownErrorCode() {
        final ControlPointApiException e = new ControlPointApiException(400,
            new ControlPointErrorResponse("Something Else", "Out of disk space"));
        Assert.assertEquals("should map to UNKNOWN enum value",
            ControlPointApiException.ErrorId.UNKNOWN, e.getErrorId());
    }

}
