/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
