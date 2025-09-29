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

import com.hp.autonomy.frontend.find.idol.testutil.AssertExt;
import com.hp.autonomy.frontend.find.idol.testutil.TestClock;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

public class ControlPointApiClientTest {
    private HttpClient defaultHttpClient;
    private ControlPointServerDetails defaultServerDetails;
    private TestClock clock;
    private ControlPointApiClient defaultCPClient;


    /**
     * Type for testing API response parsing for objects.
     */
    private static class TestResponse {
        public String field1;
        public String field2;
    }


    /**
     * Create a simple HTTP response object.
     */
    public static Answer<?> buildAnswer(final int statusCode, final String body) {
        return inv -> {
            final BasicClassicHttpResponse response = new BasicClassicHttpResponse(statusCode);
            response.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
            return inv.getArgument(1, HttpClientResponseHandler.class).handleResponse(response);
        };
    }

    /**
     * Create an HTTP response object for a successful login request.
     */
    public static Answer<?> buildLoginAnswer() {
        return buildAnswer(200, "{\"access_token\":\"the token\",\"expires_in\": 600}");
    }

    /**
     * Create an HTTP response object for a successful request with integer response body.
     */
    public static Answer<?> buildStandardAnswer() {
        return buildAnswer(200, "456");
    }

    /**
     * Assert that a specific number of requests have been made, and return the request objects in
     * order.
     */
    private List<HttpUriRequest> getRequests(final int expectedCalls)
        throws IOException
    {
        final ArgumentCaptor<HttpUriRequest> captor = ArgumentCaptor.forClass(HttpUriRequest.class);
        Mockito.verify(defaultHttpClient, Mockito.times(expectedCalls))
                .execute(captor.capture(), Mockito.any(HttpClientResponseHandler.class));
        return captor.getAllValues();
    }

    @Before
    public void setUp() throws IOException {
        defaultHttpClient = Mockito.mock(HttpClient.class);
        Mockito.doAnswer(buildLoginAnswer())
            .doAnswer(buildStandardAnswer())
            .when(defaultHttpClient).execute(Mockito.any(), Mockito.<HttpClientResponseHandler<?>>any());
        defaultServerDetails = ControlPointServerDetails.builder()
            .protocol("http").host("cp-host").port(123).basePath("base/path")
            .username("cp-user").password("cp-pass")
            .build();
        clock = new TestClock();
        defaultCPClient = new ControlPointApiClient(defaultHttpClient, defaultServerDetails);
        defaultCPClient.setClock(clock);
    }

    @Test
    public void testGet() throws Exception {
        final int result = defaultCPClient.get("status", Arrays.asList(
            new BasicNameValuePair("version", "3"),
            new BasicNameValuePair("format", "standard")
        ), Integer.class);
        final List<HttpUriRequest> requests = getRequests(2);

        Assert.assertEquals("first request should be POST", "POST", requests.get(0).getMethod());
        final HttpPost loginReq = (HttpPost) requests.get(0);
        Assert.assertEquals("first request should be login",
            "http://cp-host:123/base/path/login",
            loginReq.getUri().toString());
        Assert.assertEquals("first request should include credentials",
            "grant_type=password&username=cp-user&password=cp-pass",
            IOUtils.toString(loginReq.getEntity().getContent()));

        Assert.assertEquals("second request should be GET", "GET", requests.get(1).getMethod());
        Assert.assertEquals("second request should have correct URL",
            "http://cp-host:123/base/path/status?version=3&format=standard",
            requests.get(1).getUri().toString());
        Assert.assertEquals("second request should include auth token",
            "Bearer the token",
            requests.get(1).getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());

        Assert.assertEquals("should parse result", 456, result);
    }

    @Test
    public void testPostUrlencoded() throws Exception {
        final int result = defaultCPClient.postUrlencoded("copy", Arrays.asList(
            new BasicNameValuePair("version", "3"),
            new BasicNameValuePair("format", "standard")
        ), Arrays.asList(
            new BasicNameValuePair("when", "tomorrow"),
            new BasicNameValuePair("why", "backup")
        ), Integer.class);
        final List<HttpUriRequest> requests = getRequests(2);

        Assert.assertEquals("should be POST", "POST", requests.get(1).getMethod());
        final HttpPost req = (HttpPost) requests.get(1);
        Assert.assertEquals("should have correct URL",
            "http://cp-host:123/base/path/copy?version=3&format=standard", req.getUri().toString());
        Assert.assertEquals("should include auth token",
            "Bearer the token",
            req.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());
        Assert.assertEquals("should have correct body",
            "when=tomorrow&why=backup",
            IOUtils.toString(req.getEntity().getContent()));

        Assert.assertEquals("should parse result", 456, result);
    }

    @Test
    public void testExtraPathSeparators() throws Exception {
        final ControlPointServerDetails serverDetails =
            defaultServerDetails.toBuilder().basePath("/base//path/").build();
        new ControlPointApiClient(defaultHttpClient, serverDetails)
            .get("//network/status/", Collections.singletonList(
                new BasicNameValuePair("version", "3")
            ), Void.class);
        final List<HttpUriRequest> requests = getRequests(2);

        Assert.assertEquals("path should have normalised separators",
            "http://cp-host:123/base/path/network/status?version=3",
            requests.get(1).getUri().toString());
    }

    @Test
    public void testSpecialPathCharacters() throws Exception {
        final ControlPointServerDetails serverDetails =
            defaultServerDetails.toBuilder().basePath("base%/path").build();
        new ControlPointApiClient(defaultHttpClient, serverDetails)
            .get("status?", Collections.singletonList(
                new BasicNameValuePair("version", "3")
            ), Void.class);
        final List<HttpUriRequest> requests = getRequests(2);

        Assert.assertEquals("path should have encoded characters",
            "http://cp-host:123/base%25/path/status%3F?version=3",
            requests.get(1).getUri().toString());
    }

    @Test
    public void testSpecialQueryParamCharacters() throws Exception {
        defaultCPClient.get("status", Collections.singletonList(
            new BasicNameValuePair("%format?", "&standard")
        ), Void.class);
        final List<HttpUriRequest> requests = getRequests(2);

        Assert.assertEquals("querystring should have encoded characters",
            "http://cp-host:123/base/path/status?%25format%3F=%26standard",
            requests.get(1).getUri().toString());
    }

    @Test
    public void testEmptyQueryParams() throws Exception {
        defaultCPClient.get("status", Collections.emptyList(), Void.class);
        final List<HttpUriRequest> requests = getRequests(2);

        Assert.assertEquals("querystring should be missing",
            "http://cp-host:123/base/path/status",
            requests.get(1).getUri().toString());
    }

    @Test
    public void testMultipleQueryParamValues() throws Exception {
        defaultCPClient.get("status", Arrays.asList(
            new BasicNameValuePair("formats", "standard"),
            new BasicNameValuePair("formats", "small"),
            new BasicNameValuePair("formats", "blue")
        ), Void.class);
        final List<HttpUriRequest> requests = getRequests(2);

        Assert.assertEquals("querystring should have all values",
            "http://cp-host:123/base/path/status?formats=standard&formats=small&formats=blue",
            requests.get(1).getUri().toString());
    }

    @Test
    public void testSpecialUrlencodedBodyCharacters() throws ControlPointApiException, IOException {
        defaultCPClient.postUrlencoded("copy", Collections.emptyList(), Collections.singletonList(
            new BasicNameValuePair("%when?", "&tomorrow")
        ), Void.class);
        final List<HttpUriRequest> requests = getRequests(2);

        Assert.assertEquals("should be POST", "POST", requests.get(1).getMethod());
        final HttpPost req = (HttpPost) requests.get(1);
        Assert.assertEquals("body should have encoded characters",
            "%25when%3F=%26tomorrow",
            IOUtils.toString(req.getEntity().getContent()));
    }

    @Test
    public void testMultipleUrlencodedBodyValues() throws ControlPointApiException, IOException {
        defaultCPClient.postUrlencoded("status", Collections.emptyList(), Arrays.asList(
            new BasicNameValuePair("when", "tomorrow"),
            new BasicNameValuePair("when", "yesterday"),
            new BasicNameValuePair("when", "later")
        ), Void.class);
        final List<HttpUriRequest> requests = getRequests(2);

        Assert.assertEquals("should be POST", "POST", requests.get(1).getMethod());
        final HttpPost req = (HttpPost) requests.get(1);
        Assert.assertEquals("body should have all values",
            "when=tomorrow&when=yesterday&when=later",
            IOUtils.toString(req.getEntity().getContent()));
    }

    @Test
    public void testResponseExtraFields() throws ControlPointApiException, IOException {
        final String resJson = "{\"field1\":\"val1\",\"field2\":\"val2\",\"field3\":\"val3\"}";
        Mockito.doAnswer(buildLoginAnswer())
            .doAnswer(buildAnswer(200, resJson))
            .when(defaultHttpClient).execute(Mockito.any(), Mockito.<HttpClientResponseHandler<?>>any());

        final TestResponse res = defaultCPClient.get("status", Collections.singletonList(
            new BasicNameValuePair("format", "standard")
        ), TestResponse.class);

        Assert.assertEquals("should ignore extra field", "val1", res.field1);
    }

    @Test
    public void testSkipResponse() throws ControlPointApiException, IOException {
        final String resJson = "{";
        Mockito.doAnswer(buildLoginAnswer())
            .doAnswer(buildAnswer(200, resJson))
            .when(defaultHttpClient).execute(Mockito.any(), Mockito.<HttpClientResponseHandler<?>>any());

        final TestResponse res = defaultCPClient.get("status", Collections.singletonList(
            new BasicNameValuePair("format", "standard")
        ), null);

        Assert.assertNull("should return null", res);
    }

    @Test
    public void testResponseInvalidJson() throws ControlPointApiException, IOException {
        final String resJson = "{";
        Mockito.doAnswer(buildLoginAnswer())
            .doAnswer(buildAnswer(200, resJson))
            .when(defaultHttpClient).execute(Mockito.any(), Mockito.<HttpClientResponseHandler<?>>any());

        AssertExt.assertThrows(ControlPointServiceException.class, () -> {
            defaultCPClient.get("status", Collections.singletonList(
                new BasicNameValuePair("format", "standard")
            ), TestResponse.class);
        });
    }

    @Test
    public void testResponseError() throws ControlPointApiException, IOException {
        final String resJson = "{" +
            "\"error\": \"Invalid Grant\"," +
            "\"error_description\":\"Wrong credentials\"}";
        Mockito.doAnswer(buildLoginAnswer())
            .doAnswer(buildAnswer(401, resJson))
            .when(defaultHttpClient).execute(Mockito.any(), Mockito.<HttpClientResponseHandler<?>>any());

        final ControlPointApiException e = AssertExt.assertThrows(
            ControlPointApiException.class,
            () -> {
                defaultCPClient.get("status", Collections.singletonList(
                    new BasicNameValuePair("format", "standard")
                ), Integer.class);
            });

        Assert.assertEquals("exception should have status code", 401, e.getStatusCode());
        Assert.assertEquals("expection should have parsed error ID",
            ControlPointApiException.ErrorId.INVALID_GRANT,
            e.getErrorId());
        Assert.assertEquals("expection should have parsed error message",
            "Wrong credentials",
            e.getMessage());
    }

    @Test
    public void testResponseErrorUnexpectedFormat() throws ControlPointApiException, IOException {
        final String resText = "Something went wrong";
        Mockito.doAnswer(buildLoginAnswer())
            .doAnswer(buildAnswer(401, resText))
            .when(defaultHttpClient).execute(Mockito.any(), Mockito.<HttpClientResponseHandler<?>>any());

        final ControlPointServiceException e = AssertExt.assertThrows(
            ControlPointServiceException.class,
            () -> {
                defaultCPClient.get("status", Collections.singletonList(
                    new BasicNameValuePair("format", "standard")
                ), Integer.class);
            });

        Assert.assertEquals("exception should have response body",
            "Unexpected response from ControlPoint API: Something went wrong",
            e.getMessage());
    }

    @Test
    public void testLoginError() throws ControlPointApiException, IOException {
        final String resJson = "{" +
            "\"error\": \"Invalid Grant\"," +
            "\"error_description\":\"Wrong credentials\"}";
        Mockito.doAnswer(buildAnswer(401, resJson))
            .doAnswer(buildStandardAnswer())
            .when(defaultHttpClient).execute(Mockito.any(), Mockito.<HttpClientResponseHandler<?>>any());

        AssertExt.assertThrows(
            ControlPointApiException.class,
            () -> {
                defaultCPClient.get("status", Collections.singletonList(
                    new BasicNameValuePair("format", "standard")
                ), TestResponse.class);
            });
        // should only make 1 request
        getRequests(1);
    }

    @Test
    public void testMultipleRequests() throws ControlPointApiException, IOException {
        defaultCPClient.get("status", Collections.singletonList(
            new BasicNameValuePair("format", "standard")
        ), Integer.class);
        defaultCPClient.get("status", Collections.singletonList(
            new BasicNameValuePair("format", "standard")
        ), Integer.class);

        final List<HttpUriRequest> requests = getRequests(3);
        Assert.assertEquals("should login", "POST", requests.get(0).getMethod());
        Assert.assertEquals("should make request", "GET", requests.get(1).getMethod());
        Assert.assertEquals("should make another request without another login",
            "GET", requests.get(2).getMethod());
    }

    @Test
    public void testRequestAfterTokenExpiry() throws ControlPointApiException, IOException {
        Mockito.doAnswer(buildLoginAnswer())
            .doAnswer(buildStandardAnswer())
            .doAnswer(buildStandardAnswer())
            .doAnswer(buildLoginAnswer())
            .doAnswer(buildStandardAnswer())
            .when(defaultHttpClient).execute(Mockito.any(), Mockito.<HttpClientResponseHandler<?>>any());

        defaultCPClient.get("status", Collections.singletonList(
            new BasicNameValuePair("format", "standard")
        ), Integer.class);
        clock.tick(Duration.ofSeconds(589)); // before expiry
        defaultCPClient.get("status", Collections.singletonList(
            new BasicNameValuePair("format", "standard")
        ), Integer.class);
        clock.tick(Duration.ofSeconds(12)); // after expiry
        defaultCPClient.get("status", Collections.singletonList(
            new BasicNameValuePair("format", "standard")
        ), Integer.class);

        final List<HttpUriRequest> requests = getRequests(5);
        Assert.assertEquals("should login", "POST", requests.get(0).getMethod());
        Assert.assertEquals("should make request", "GET", requests.get(1).getMethod());
        Assert.assertEquals("shouldn't login again before expiry",
            "GET", requests.get(2).getMethod());
        Assert.assertEquals("should login again after expiry", "POST", requests.get(3).getMethod());
        Assert.assertEquals("should make request after 2nd login",
            "GET", requests.get(4).getMethod());
    }

    @Test
    public void testGet_noCredentials() throws ControlPointApiException, IOException {
        Mockito.doAnswer(buildStandardAnswer())
                .when(defaultHttpClient).execute(Mockito.any(), Mockito.<HttpClientResponseHandler<?>>any());
        final ControlPointServerDetails serverDetails = ControlPointServerDetails.builder()
            .protocol("http").host("cp-host").port(123).basePath("base/path")
            .build();
        final ControlPointApiClient cpClient =
            new ControlPointApiClient(defaultHttpClient, serverDetails);
        cpClient.setClock(clock);

        final int result = cpClient.get("status", Arrays.asList(
            new BasicNameValuePair("version", "3"),
            new BasicNameValuePair("format", "standard")
        ), Integer.class);
        final List<HttpUriRequest> requests = getRequests(1);

        Assert.assertEquals("only request should be GET", "GET", requests.get(0).getMethod());
        Assert.assertEquals("should parse result", 456, result);
    }

}
