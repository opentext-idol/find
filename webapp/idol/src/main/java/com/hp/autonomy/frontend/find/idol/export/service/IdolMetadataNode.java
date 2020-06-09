/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.idol.export.service;

import com.hp.autonomy.frontend.find.core.export.service.MetadataNode;
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
