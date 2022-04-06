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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import sun.misc.Regexp;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Unify with ISO version
@NoArgsConstructor(access = AccessLevel.NONE)
public class RequestUtils {
    private static final Pattern BACKSLASH = Pattern.compile("\\\\");
    private static final Pattern QUOTE = Pattern.compile( "\"");

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

    /**
     * Set a header on the HTTP response which sets the download filename.
     */
    public static void setFilenameHeader(
        final HttpServletResponse response, final String filename
    ) {
        // Spring 5 provides ContentDisposition for this, but we're on 4
        final String escapedFilename = QUOTE.matcher(
            BACKSLASH.matcher(filename).replaceAll("\\\\\\\\")
        ).replaceAll("\\\\\"");
        response.setHeader("Content-Disposition",
            "attachment; filename=\"" + escapedFilename + "\"");
    }

}
