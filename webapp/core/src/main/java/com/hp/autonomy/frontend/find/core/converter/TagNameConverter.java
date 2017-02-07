/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.converter;

import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Converts field names sent from the client to normalised tag names
 */
@Component
public class TagNameConverter implements Converter<String, TagName> {
    private final TagNameFactory tagNameFactory;

    @Autowired
    public TagNameConverter(final TagNameFactory tagNameFactory) {
        this.tagNameFactory = tagNameFactory;
    }

    @Override
    public TagName convert(final String source) {
        return tagNameFactory.buildTagName(decodeUriComponent(source));
    }

    private String decodeUriComponent(final String part) {
        try {
            return URLDecoder.decode(part, "UTF-8");
        } catch(final UnsupportedEncodingException e) {
            throw new AssertionError("All JVMs must support UTF-8", e);
        }
    }
}
