/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.export;

import com.hp.autonomy.frontend.find.core.export.MetadataNode;
import lombok.Getter;
import org.springframework.core.convert.converter.Converter;

@SuppressWarnings("NonSerializableFieldInSerializableClass")
@Getter
enum IdolMetadataNode implements MetadataNode {
    REFERENCE("Reference", "autn:reference", ExportQueryResponseProcessor.STRING_CONVERTER),
    DATABASE("Database", "autn:database", ExportQueryResponseProcessor.STRING_CONVERTER),
    TITLE("Title", "autn:title", ExportQueryResponseProcessor.STRING_CONVERTER),
    SUMMARY("Summary", "autn:summary", ExportQueryResponseProcessor.STRING_CONVERTER),
    WEIGHT("Weight", "autn:weight", ExportQueryResponseProcessor.STRING_CONVERTER),
    DATE("Date", "autn:date", ExportQueryResponseProcessor.DATE_CONVERTER);

    private final String name;
    private final String nodeName;
    private final Converter<String, String> converter;

    IdolMetadataNode(final String name, final String nodeName, final Converter<String, String> converter) {
        this.name = name;
        this.nodeName = nodeName;
        this.converter = converter;
    }
}
