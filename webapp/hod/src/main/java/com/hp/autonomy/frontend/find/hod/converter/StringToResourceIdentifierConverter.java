/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.converter;

import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@Component
public class StringToResourceIdentifierConverter implements Converter<String, ResourceIdentifier> {
    @Override
    public ResourceIdentifier convert(final String s) {
        final String[] parts = s.split(":");

        return new ResourceIdentifier(decodeUriComponent(parts[0]), decodeUriComponent(parts[1]));
    }

    private String decodeUriComponent(final String part) {
        try {
            return URLDecoder.decode(part, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new AssertionError("All JVMs must support UTF-8", e);
        }
    }
}
