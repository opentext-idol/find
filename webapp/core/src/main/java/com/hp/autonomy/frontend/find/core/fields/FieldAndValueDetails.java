/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.fields;

import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FieldAndValueDetails {
    private final String id;
    private final String displayName;
    private final double min;
    private final double max;
    private final long totalValues;
    private final FieldTypeParam type;
}
