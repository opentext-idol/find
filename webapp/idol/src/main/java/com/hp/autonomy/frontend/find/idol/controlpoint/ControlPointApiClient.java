/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.controlpoint;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Call ControlPoint APIs.  Methods also throw {@link ControlPointServiceException}.
 */
@RequiredArgsConstructor
public class ControlPointApiClient {
    /**
     * Treat {@link ControlPointLoginResponse.accessToken} as expired if it will expire within this
     * many seconds.  Used to account for delays in processing the token when logging in, and delays
     * in using the token in an API request.
     */
    private static final int TOKEN_EXPIRY_LEEWAY_SECONDS = 10;
    // used to deserialised responses
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final HttpClient httpClient;
    private final ControlPointServerDetails serverDetails;
    @Setter
    private Clock clock = Clock.systemUTC();
    // most recent login response, if we've ever logged in
    private ControlPointLoginResponse loginState;
    // time after which the current login should be treated as expired
    private Instant tokenExpireTime;

    /**
     * Construct a normalised request path from a sequence of components which are already
     * URL-encoded and so may contain '/' characters.  The result doesn't start or end in '/'.
     */
    private String buildPath(final List<String> parts) {
        return parts.stream()
            .flatMap(part -> Arrays.stream(part.split("/")))
            .filter(component -> !component.isEmpty())
            .collect(Collectors.joining("/"));
    }

    /**
     * Construct a request URL.
     *
     * @param path URL-encoded path which may contain '/'
     * @param queryParams Query-string parameters
     * @return Request URL
     */
    private URI buildUrl(final String path, final List<NameValuePair> queryParams) {
        try {
            return new URIBuilder()
                .setScheme(serverDetails.getProtocol())
                .setHost(serverDetails.getHost())
                .setPort(serverDetails.getPort())
                .setPath("/" + buildPath(Arrays.asList(serverDetails.getBasePath(), path)))
                .setQuery(URLEncodedUtils.format(queryParams, StandardCharsets.UTF_8))
                .build();
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Failed to construct request URL");
        }
    }

    /**
     * Construct an HTTP GET request.
     *
     * @param path URL-encoded path which may contain '/'
     * @param queryParams Query-string parameters
     * @return Request
     */
    private HttpUriRequest buildGetRequest(
        final String path,
        final List<NameValuePair> queryParams
    ) {
        return new HttpGet(buildUrl(path, queryParams));
    }

    /**
     * Construct an HTTP POST request with a URL-encoded request body.
     *
     * @param path URL-encoded path which may contain '/'
     * @param queryParams Query-string parameters
     * @param bodyParams Parameters to send in the request body
     * @return Request
     */
    private HttpUriRequest buildUrlencodedPostRequest(
        final String path,
        final List<NameValuePair> queryParams,
        final List<NameValuePair> bodyParams
    ) {
        final HttpPost request = new HttpPost(buildUrl(path, queryParams));
        final String body = URLEncodedUtils.format(bodyParams, StandardCharsets.UTF_8);
        request.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        return request;
    }

    /**
     * Perform an API request, handle errors, and parse the JSON response into the given type.  If
     * resultType is null, don't read the response, and return null.
     */
    private <R> R performRequest(final HttpUriRequest request, final Class<R> resultType)
        throws ControlPointApiException
    {
        final HttpResponse response;
        final int statusCode;
        // read the entire response before parsing, so that if parsing fails, it can be included in
        // the error
        final String resBody;
        try {
            response = httpClient.execute(request);
            statusCode = response.getStatusLine().getStatusCode();
            resBody = IOUtils.toString(response.getEntity().getContent());
        } catch (final IOException e) {
            throw new ControlPointServiceException(e);
        }

        try {
            if (statusCode >= 200 && statusCode < 300) {
                if (resultType == null) {
                    return null;
                } else {
                    return objectMapper.readValue(resBody, resultType);
                }
            } else {
                final ControlPointErrorResponse error =
                    objectMapper.readValue(resBody, ControlPointErrorResponse.class);
                throw new ControlPointApiException(response.getStatusLine().getStatusCode(), error);
            }
        } catch (final IOException e) {
            throw new ControlPointServiceException(
                "Unexpected response from ControlPoint API: " + resBody);
        }
    }

    /**
     * Authenticate with the ControlPoint server and store the result in {@link loginState}.
     */
    private void authenticate() throws ControlPointApiException {
        loginState = performRequest(
            buildUrlencodedPostRequest("login", Collections.emptyList(), Arrays.asList(
                new BasicNameValuePair("grant_type", "password"),
                new BasicNameValuePair("username", serverDetails.getUsername()),
                new BasicNameValuePair("password", serverDetails.getPassword())
            )),
            ControlPointLoginResponse.class);
        tokenExpireTime = clock.instant()
            .minusSeconds(TOKEN_EXPIRY_LEEWAY_SECONDS)
            .plusSeconds(loginState.getExpiresInSeconds());
    }

    /**
     * Ensure we're logged in, perform an authenticated API request, handle errors, and parse the
     * JSON response into the given type.
     */
    private <R> R performAuthenticatedRequest(
        final HttpUriRequest request,
        final Class<R> resultType
    ) throws ControlPointApiException {
        if (serverDetails.getUsername() != null) {
            if (loginState == null || tokenExpireTime.isBefore(clock.instant())) {
                authenticate();
            }
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + loginState.getAccessToken());
        }
        return performRequest(request, resultType);
    }

    /**
     * Send an API request using HTTP GET.
     *
     * @param path URL-encoded path which may contain '/'
     * @param queryParams Query-string parameters
     * @param resultType Type to parse the JSON response into, or null to skip reading the response
     * @return Parsed response
     */
    public <R> R get(
        final String path,
        final List<NameValuePair> queryParams,
        final Class<R> resultType
    ) throws ControlPointApiException {
        return performAuthenticatedRequest(buildGetRequest(path, queryParams), resultType);
    }

    /**
     * Send an API request using HTTP POST with a URL-encoded request body.
     *
     * @param path URL-encoded path which may contain '/'
     * @param queryParams Query-string parameters
     * @param resultType Type to parse the JSON response into, or null to skip reading the response
     * @param bodyParams Parameters to send in the request body
     * @return Parsed response
     */
    public <R> R postUrlencoded(
        final String path,
        final List<NameValuePair> queryParams,
        final List<NameValuePair> bodyParams,
        final Class<R> resultType
    ) throws ControlPointApiException {
        return performAuthenticatedRequest(
            buildUrlencodedPostRequest(path, queryParams, bodyParams),
            resultType);
    }

}
