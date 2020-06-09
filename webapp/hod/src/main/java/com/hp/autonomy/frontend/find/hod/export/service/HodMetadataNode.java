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

package com.hp.autonomy.frontend.find.hod.export.service;

import com.hp.autonomy.frontend.find.core.export.service.MetadataNode;
import com.hp.autonomy.searchcomponents.core.config.FieldType;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import lombok.Getter;

import java.util.function.Function;

@SuppressWarnings("NonSerializableFieldInSerializableClass")
@Getter
public enum HodMetadataNode implements MetadataNode {
    REFERENCE("Reference", FieldType.STRING, HodSearchResult::getReference),
    DATABASE("Database", FieldType.STRING, HodSearchResult::getIndex),
    TITLE("Title", FieldType.STRING, HodSearchResult::getTitle),
    SUMMARY("Summary", FieldType.STRING, HodSearchResult::getSummary),
    WEIGHT("Weight", FieldType.NUMBER, HodSearchResult::getWeight),
    DATE("Date", FieldType.DATE, HodSearchResult::getDate);

    private final String displayName;
    private final FieldType fieldType;
    private final Function<HodSearchResult, Object> getter;

    HodMetadataNode(final String displayName, final FieldType fieldType, final Function<HodSearchResult, Object> getter) {
        this.displayName = displayName;
        this.fieldType = fieldType;
        this.getter = getter;
    }

    @Override
    public String getName() {
        return name();
    }
}
