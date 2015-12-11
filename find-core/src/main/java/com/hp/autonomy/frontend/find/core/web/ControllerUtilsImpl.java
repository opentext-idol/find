/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ControllerUtilsImpl implements ControllerUtils {
    private static final Pattern JSON_ESCAPE_PATTERN = Pattern.compile("</", Pattern.LITERAL);

    private final ObjectMapper objectMapper;

    @Autowired
    public ControllerUtilsImpl(final ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    @Override
    public String convertToJson(final Object object) throws JsonProcessingException
    {
        // As we are inserting into a script tag escape </ to prevent injection
        return JSON_ESCAPE_PATTERN.matcher(objectMapper.writeValueAsString(object)).replaceAll("<\\/");
    }
}
