/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

// TODO: Unify with ISO version
@NoArgsConstructor(access = AccessLevel.NONE)
public class RequestUtils {

    /**
     * Build the href attribute for the base tag of an HTML response to the given request. The output base URL should
     * result in the base being the context path of the application. It is relative to account for reverse proxying, and
     * ends in a forward slash.
     * @param request The request to construct a base URL for (can be a forward request)
     * @return The value of the href attribute for the response base tag
     */
    public static String buildBaseUrl(final HttpServletRequest request) {
        // The request might have been forwarded internally
        final String requestUri = Optional.ofNullable((String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI))
                .orElseGet(request::getRequestURI);

        final String path = requestUri.replaceFirst(request.getContextPath(), "");

        // Subtract 1 for the / after the context path
        final int depth = StringUtils.countMatches(path, "/") - 1;
        return depth <= 0 ? "." : StringUtils.repeat("../", depth);
    }

}
