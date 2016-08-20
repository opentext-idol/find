/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.export;

import com.hp.autonomy.frontend.find.core.export.MetadataNode;
import com.hp.autonomy.searchcomponents.core.config.FieldType;
import lombok.Getter;

@SuppressWarnings("NonSerializableFieldInSerializableClass")
@Getter
public enum IdolMetadataNode implements MetadataNode {
    REFERENCE("Reference", "autn:reference", FieldType.STRING),
    DATABASE("Database", "autn:database", FieldType.STRING),
    TITLE("Title", "autn:title", FieldType.STRING),
    SUMMARY("Summary", "autn:summary", FieldType.STRING),
    WEIGHT("Weight", "autn:weight", FieldType.NUMBER),
    DATE("Date", "autn:date", FieldType.DATE);

    private final FieldType fieldType;
    private final String displayName;
    private final String nodeName;

    IdolMetadataNode(final String displayName, final String nodeName, final FieldType fieldType) {
        this.displayName = displayName;
        this.nodeName = nodeName;
        this.fieldType = fieldType;
    }

    @Override
    public String getName() {
        return name();
    }
}
