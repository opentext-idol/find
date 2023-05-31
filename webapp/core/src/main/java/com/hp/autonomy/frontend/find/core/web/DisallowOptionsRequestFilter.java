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

package com.hp.autonomy.frontend.find.core.web;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The OPTIONS HTTP method is seen by some as a security issue.  The Find API isn't documented, so
 * we don't need CORS, so we don't need OPTIONS - so just disable it.
 */
@Component
public class DisallowOptionsRequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final FilterChain chain
    ) throws ServletException, IOException {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            response.sendError(HttpStatus.METHOD_NOT_ALLOWED.value());
        } else {
            chain.doFilter(request, response);
        }
    }

}
