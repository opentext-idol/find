/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.export;

import com.hp.autonomy.frontend.find.core.export.MetadataNode;
import lombok.Getter;

@SuppressWarnings("NonSerializableFieldInSerializableClass")
@Getter
enum HodMetadataNode implements MetadataNode {
    REFERENCE("Reference"),
    DATABASE("Database"),
    TITLE("Title"),
    SUMMARY("Summary"),
    WEIGHT("Weight"),
    DATE("Date");

    private final String name;

    HodMetadataNode(final String name) {
        this.name = name;
    }
}
