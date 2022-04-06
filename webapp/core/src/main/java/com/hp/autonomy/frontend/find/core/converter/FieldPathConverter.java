/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.converter;

import com.hp.autonomy.searchcomponents.core.fields.FieldPathNormaliser;
import com.hp.autonomy.types.requests.idol.actions.tags.FieldPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Converts field names sent from the client to normalised tag names
 */
@Component
public class FieldPathConverter implements Converter<String, FieldPath> {
    private final FieldPathNormaliser fieldPathNormaliser;

    @Autowired
    public FieldPathConverter(final FieldPathNormaliser fieldPathNormaliser) {
        this.fieldPathNormaliser = fieldPathNormaliser;
    }

    @Override
    public FieldPath convert(final String source) {
        return fieldPathNormaliser.normaliseFieldPath(decodeUriComponent(source));
    }

    private String decodeUriComponent(final String part) {
        try {
            return URLDecoder.decode(part, "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new AssertionError("All JVMs must support UTF-8", e);
        }
    }
}
