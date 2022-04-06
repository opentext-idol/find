/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.controlpoint;

import com.hp.autonomy.frontend.find.idol.testutil.AssertExt;
import com.hp.autonomy.frontend.find.idol.testutil.TestClock;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    public static HttpResponse buildResponse(
        final int statusCode, final String statusMessage, final String body
    ) {
        final BasicHttpResponse response =
            new BasicHttpResponse(HttpVersion.HTTP_1_1, statusCode, statusMessage);
        response.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        return response;
    }

    /**
     * Create an HTTP response object for a successful login request.
     */
    public static HttpResponse buildLoginResponse() {
        return buildResponse(200, "OK", "{\"access_token\":\"the token\",\"expires_in\": 600}");
    }

    /**
     * Create an HTTP response object for a successful request with integer response body.
     */
    public static HttpResponse buildStandardResponse() {
        return buildResponse(200, "OK", "456");
    }

    /**
     * Assert that a specific number of requests have been made, and return the request objects in
     * order.
     */
    private List<HttpUriRequest> getRequests(final int expectedCalls)
        throws IOException
    {
        final ArgumentCaptor<HttpUriRequest> captor = new ArgumentCaptor<>();
        Mockito.verify(defaultHttpClient, Mockito.times(expectedCalls)).execute(captor.capture());
        return captor.getAllValues();
    }

    @Before
    public void setUp() throws IOException {
        defaultHttpClient = Mockito.mock(HttpClient.class);
        Mockito.when(defaultHttpClient.execute(Mockito.any()))
            .thenReturn(buildLoginResponse())
            .thenReturn(buildStandardResponse());
        defaultServerDetails = ControlPointServerDetails.builder()
            .protocol("http").host("cp-host").port(123).basePath("base/path")
            .username("cp-user").password("cp-pass")
            .build();
        clock = new TestClock();
        defaultCPClient = new ControlPointApiClient(defaultHttpClient, defaultServerDetails);
        defaultCPClient.setClock(clock);
    }

    @Test
    public void testGet() throws ControlPointApiException, IOException {
        final int result = defaultCPClient.get("status", Arrays.asList(
            new BasicNameValuePair("version", "3"),
            new BasicNameValuePair("format", "standard")
        ), Integer.class);
        final List<HttpUriRequest> requests = getRequests(2);

        Assert.assertEquals("first request should be POST", "POST", requests.get(0).getMethod());
        final HttpPost loginReq = (HttpPost) requests.get(0);
        Assert.assertEquals("first request should be login",
            "http://cp-host:123/base/path/login",
            loginReq.getURI().toString());
        Assert.assertEquals("first request should include credentials",
            "grant_type=password&username=cp-user&password=cp-pass",
            IOUtils.toString(loginReq.getEntity().getContent()));

        Assert.assertEquals("second request should be GET", "GET", requests.get(1).getMethod());
        Assert.assertEquals("second request should have correct URL",
            "http://cp-host:123/base/path/status?version=3&format=standard",
            requests.get(1).getURI().toString());
        Assert.assertEquals("second request should include auth token",
            "Bearer the token",
            requests.get(1).getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());

        Assert.assertEquals("should parse result", 456, result);
    }

    @Test
    public void testPostUrlencoded() throws ControlPointApiException, IOException {
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
            "http://cp-host:123/base/path/copy?version=3&format=standard", req.getURI().toString());
        Assert.assertEquals("should include auth token",
            "Bearer the token",
            req.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());
        Assert.assertEquals("should have correct body",
            "when=tomorrow&why=backup",
            IOUtils.toString(req.getEntity().getContent()));

        Assert.assertEquals("should parse result", 456, result);
    }

    @Test
    public void testExtraPathSeparators() throws ControlPointApiException, IOException {
        final ControlPointServerDetails serverDetails =
            defaultServerDetails.toBuilder().basePath("/base//path/").build();
        new ControlPointApiClient(defaultHttpClient, serverDetails)
            .get("//network/status/", Collections.singletonList(
                new BasicNameValuePair("version", "3")
            ), Void.class);
        final List<HttpUriRequest> requests = getRequests(2);

        Assert.assertEquals("path should have normalised separators",
            "http://cp-host:123/base/path/network/status?version=3",
            requests.get(1).getURI().toString());
    }

    @Test
    public void testSpecialPathCharacters() throws ControlPointApiException, IOException {
        final ControlPointServerDetails serverDetails =
            defaultServerDetails.toBuilder().basePath("base%/path").build();
        new ControlPointApiClient(defaultHttpClient, serverDetails)
            .get("status?", Collections.singletonList(
                new BasicNameValuePair("version", "3")
            ), Void.class);
        final List<HttpUriRequest> requests = getRequests(2);

        Assert.assertEquals("path should have encoded characters",
            "http://cp-host:123/base%25/path/status%3F?version=3",
            requests.get(1).getURI().toString());
    }

    @Test
    public void testSpecialQueryParamCharacters() throws ControlPointApiException, IOException {
        defaultCPClient.get("status", Collections.singletonList(
            new BasicNameValuePair("%format?", "&standard")
        ), Void.class);
        final List<HttpUriRequest> requests = getRequests(2);

        Assert.assertEquals("querystring should have encoded characters",
            "http://cp-host:123/base/path/status?%25format%3F=%26standard",
            requests.get(1).getURI().toString());
    }

    @Test
    public void testEmptyQueryParams() throws ControlPointApiException, IOException {
        defaultCPClient.get("status", Collections.emptyList(), Void.class);
        final List<HttpUriRequest> requests = getRequests(2);

        Assert.assertEquals("querystring should be missing",
            "http://cp-host:123/base/path/status",
            requests.get(1).getURI().toString());
    }

    @Test
    public void testMultipleQueryParamValues() throws ControlPointApiException, IOException {
        defaultCPClient.get("status", Arrays.asList(
            new BasicNameValuePair("formats", "standard"),
            new BasicNameValuePair("formats", "small"),
            new BasicNameValuePair("formats", "blue")
        ), Void.class);
        final List<HttpUriRequest> requests = getRequests(2);

        Assert.assertEquals("querystring should have all values",
            "http://cp-host:123/base/path/status?formats=standard&formats=small&formats=blue",
            requests.get(1).getURI().toString());
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
        Mockito.when(defaultHttpClient.execute(Mockito.any()))
            .thenReturn(buildLoginResponse())
            .thenReturn(buildResponse(200, "OK", resJson));

        final TestResponse res = defaultCPClient.get("status", Collections.singletonList(
            new BasicNameValuePair("format", "standard")
        ), TestResponse.class);

        Assert.assertEquals("should ignore extra field", "val1", res.field1);
    }

    @Test
    public void testSkipResponse() throws ControlPointApiException, IOException {
        final String resJson = "{";
        Mockito.when(defaultHttpClient.execute(Mockito.any()))
            .thenReturn(buildLoginResponse())
            .thenReturn(buildResponse(200, "OK", resJson));

        final TestResponse res = defaultCPClient.get("status", Collections.singletonList(
            new BasicNameValuePair("format", "standard")
        ), null);

        Assert.assertNull("should return null", res);
    }

    public void testResponseInvalidJson() throws ControlPointApiException, IOException {
        final String resJson = "{";
        Mockito.when(defaultHttpClient.execute(Mockito.any()))
            .thenReturn(buildLoginResponse())
            .thenReturn(buildResponse(200, "OK", resJson));

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
        Mockito.when(defaultHttpClient.execute(Mockito.any()))
            .thenReturn(buildLoginResponse())
            .thenReturn(buildResponse(401, "Unauthorized", resJson));

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
        Mockito.when(defaultHttpClient.execute(Mockito.any()))
            .thenReturn(buildLoginResponse())
            .thenReturn(buildResponse(401, "Unauthorized", resText));

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
        Mockito.when(defaultHttpClient.execute(Mockito.any()))
            .thenReturn(buildResponse(401, "OK", resJson))
            .thenReturn(buildStandardResponse());

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
        Mockito.when(defaultHttpClient.execute(Mockito.any()))
            .thenReturn(buildLoginResponse())
            .thenReturn(buildStandardResponse())
            .thenReturn(buildStandardResponse())
            .thenReturn(buildLoginResponse())
            .thenReturn(buildStandardResponse());

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
        Mockito.when(defaultHttpClient.execute(Mockito.any())).thenReturn(buildStandardResponse());
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
