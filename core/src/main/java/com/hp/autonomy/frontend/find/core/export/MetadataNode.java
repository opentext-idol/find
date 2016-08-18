/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

import com.hp.autonomy.searchcomponents.core.config.FieldType;

/**
 * Information for hard coded metadata (either Idol or HoD)
 */
public interface MetadataNode {
    /**
     * The prettified name to use in the exported data if a header line is required
     *
     * @return the prettified name
     */
    String getDisplayName();

    /**
     * @return The IDOL type of the meta-field
     */
    FieldType getFieldType();

    /**
     * @return A unique ID for this metadata node; in practice, the name of the enum constant
     */
    String getName();
}