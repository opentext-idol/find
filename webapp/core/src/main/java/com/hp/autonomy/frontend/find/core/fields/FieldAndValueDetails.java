/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.fields;

import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
public class FieldAndValueDetails<T extends Comparable<? super T> & Serializable> {
    private final String id;
    private final String displayName;
    private final T min;
    private final T max;
    private final long totalValues;
    private final FieldTypeParam type;

    private FieldAndValueDetails(final FieldAndValueDetailsBuilder<T> builder) {
        id = builder.id;
        displayName = builder.displayName;
        min = builder.min;
        max = builder.max;
        totalValues = builder.totalValues;
        type = builder.type;
    }

    public static <T extends Comparable<? super T> & Serializable> FieldAndValueDetailsBuilder<T> builder() {
        return new FieldAndValueDetailsBuilder<>();
    }

    @Accessors(chain = true, fluent = true)
    @Setter
    @SuppressWarnings({"WeakerAccess", "unused"})
    public static class FieldAndValueDetailsBuilder<T extends Comparable<? super T> & Serializable> {
        private String id;
        private String displayName;
        private T min;
        private T max;
        private long totalValues;
        private FieldTypeParam type;

        public FieldAndValueDetails<T> build() {
            return new FieldAndValueDetails<>(this);
        }
    }
}
