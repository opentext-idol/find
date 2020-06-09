/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.export.service;

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
