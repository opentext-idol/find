/*
 * (c) Copyright 2015-2016 Micro Focus or one of its affiliates.
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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    @Test
    public void setFilenameHeader() {
        final HttpServletResponse response = new MockHttpServletResponse();
        RequestUtils.setFilenameHeader(response, "the file.txt");
        Assert.assertEquals(
            response.getHeader("Content-Disposition"),
            "attachment; filename=\"the file.txt\"");
    }

    @Test
    public void setFilenameHeader_escapingRequired() {
        final HttpServletResponse response = new MockHttpServletResponse();
        RequestUtils.setFilenameHeader(response, "some\\thing\\the \"file\".txt");
        Assert.assertEquals(
            response.getHeader("Content-Disposition"),
            "attachment; filename=\"some\\\\thing\\\\the \\\"file\\\".txt\"");
    }

}
