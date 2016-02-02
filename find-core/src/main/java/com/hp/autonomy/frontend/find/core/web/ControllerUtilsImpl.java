/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class ControllerUtilsImpl implements ControllerUtils {
    private static final Pattern JSON_ESCAPE_PATTERN = Pattern.compile("</", Pattern.LITERAL);

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;
    private final String commit;

    @Autowired
    public ControllerUtilsImpl(final ObjectMapper objectMapper, final MessageSource messageSource, @Value("${application.commit}") final String commit) {
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
        this.commit = commit;
    }

    @Override
    public String convertToJson(final Object object) throws JsonProcessingException {
        // As we are inserting into a script tag escape </ to prevent injection
        return JSON_ESCAPE_PATTERN.matcher(objectMapper.writeValueAsString(object)).replaceAll("<\\/");
    }

    @Override
    public String getMessage(final String code, final Object[] args) throws NoSuchMessageException {
        return messageSource.getMessage(code, args, Locale.ENGLISH);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    @Override
    public ModelAndView buildErrorModelAndView(
            final HttpServletRequest request,
            final String mainMessageCode,
            final String subMessageCode,
            final Object[] subMessageArguments,
            final Integer statusCode,
            final boolean contactSupport
    ) {
        final ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("mainMessage", getMessage(mainMessageCode, null));
        modelAndView.addObject("subMessage", getMessage(subMessageCode, subMessageArguments));
        modelAndView.addObject("baseUrl", getBaseUrl(request));
        modelAndView.addObject("statusCode", statusCode);
        if (contactSupport) {
            modelAndView.addObject("contactSupport", getMessage("error.contactSupport", null));
        }
        modelAndView.addObject(MvcConstants.GIT_COMMIT.value(), commit);

        return modelAndView;
    }

    private String getBaseUrl(final HttpServletRequest request) {
        final String originalUri = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
        final String requestUri = originalUri != null ? originalUri : request.getRequestURI();
        final String path = requestUri.replaceFirst(request.getContextPath(), "");
        final int depth = StringUtils.countMatches(path, "/") - 1;

        return depth <= 0 ? "." : StringUtils.repeat("../", depth);
    }
}
