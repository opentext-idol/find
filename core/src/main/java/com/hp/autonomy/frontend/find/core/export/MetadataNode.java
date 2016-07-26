/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.export;

/**
 * Information for hard coded metadata (either Idol or HoD)
 */
public interface MetadataNode {
    /**
     * The prettified name to use in the exported data if a header line is required
     *
     * @return the prettified name
     */
    String getName();
}
