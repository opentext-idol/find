/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.fieldtext;

import com.hp.autonomy.frontend.find.core.fields.FieldAndValue;
import com.hp.autonomy.frontend.find.core.fields.ParametricRange;

import java.util.Collection;

@FunctionalInterface
public interface FieldTextParser {
    String toFieldText(Collection<FieldAndValue> fieldTextEntry, Collection<ParametricRange> parametricRanges, Collection<String> parametricExists);
}
