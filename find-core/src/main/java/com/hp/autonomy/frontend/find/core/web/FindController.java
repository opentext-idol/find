/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.autonomy.frontend.configuration.AuthenticationConfig;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.configuration.LoginTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class FindController {

    public static final String PUBLIC_PATH = "/public/";

    @Autowired
    private ConfigService<? extends AuthenticationConfig<?>> authenticationConfigService;

    @Autowired
    private ObjectMapper contextObjectMapper;

    @RequestMapping("/")
    public void index(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final String contextPath = request.getContextPath();

        if (LoginTypes.DEFAULT.equals(authenticationConfigService.getConfig().getAuthentication().getMethod())) {
            response.sendRedirect(contextPath + "/loginPage");
        } else {
            response.sendRedirect(contextPath + PUBLIC_PATH);
        }
    }

    @RequestMapping(value = PUBLIC_PATH, method = RequestMethod.GET)
    public ModelAndView userName() throws JsonProcessingException {
        final String username = SecurityContextHolder.getContext().getAuthentication().getName();
        final Map<String, Object> config = new HashMap<>();
        config.put("username", username);

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("configJson", convertToJson(config));

        return new ModelAndView("public", attributes);
    }

    protected String convertToJson(final Object object) throws JsonProcessingException {
        return contextObjectMapper.writeValueAsString(object).replace("</", "<\\/");
    }
}
