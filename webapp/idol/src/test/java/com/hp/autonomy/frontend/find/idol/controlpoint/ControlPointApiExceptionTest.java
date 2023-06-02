/*
 * Copyright 2020 Open Text.
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
