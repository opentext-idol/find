/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.authentication;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Service;

@Service
public class FindAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response, final AccessDeniedException e) throws IOException, ServletException {
        // if AJAX, add 403 to the response, otherwise redirect to the given page
        if("XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Blocked by " + this.getClass().getName());
        }
        else {
            // TODO parameterize this
            response.sendRedirect(request.getContextPath() + "/loginPage");
        }
    }
}
