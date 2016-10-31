/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import org.junit.Test;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// TODO: Unify with ISO version
public class RequestUtilsTest {
    private static final String CONTEXT_PATH = "/context";

    @Test
    public void buildsBaseUrlForRootPath() {
        final HttpServletRequest request = mockRequest("/", null);
        assertThat(RequestUtils.buildBaseUrl(request), is("."));
    }

    @Test
    public void buildsBaseUrlForDeepPath() {
        final HttpServletRequest request = mockRequest("/foo/bar/baz", null);
        assertThat(RequestUtils.buildBaseUrl(request), is("../../"));
    }

    @Test
    public void buildsBaseUrlForForward() {
        final HttpServletRequest request = mockRequest("/forward/error", "/foo/bar/baz");
        assertThat(RequestUtils.buildBaseUrl(request), is("../../"));
    }

    @Test
    public void endsInAForwardSlash() {
        final HttpServletRequest request = mockRequest("/foo/bar", null);
        assertThat(RequestUtils.buildBaseUrl(request), is("../"));
    }

    private HttpServletRequest mockRequest(final String requestUri, final String forwardedRequestUrl) {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn(CONTEXT_PATH);
        when(request.getRequestURI()).thenReturn(CONTEXT_PATH + requestUri);

        if (forwardedRequestUrl != null) {
            when(request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI)).thenReturn(CONTEXT_PATH + forwardedRequestUrl);
        }

        return request;
    }
}