/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.aci.content.fieldtext.FieldText;
import com.hp.autonomy.aci.content.fieldtext.FieldTexts;
import com.hp.autonomy.aci.content.fieldtext.MATCH;
import com.hp.autonomy.aci.content.fieldtext.NRANGE;
import com.hp.autonomy.aci.content.fieldtext.RANGE;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class FieldTextParserImpl implements FieldTextParser {
    // WARNING: This logic is duplicated in the client side SelectedValuesCollection
    @Override
    public String toFieldText(final SavedSearch<?> savedSearch) {
        final Set<FieldAndValue> parametricValues = savedSearch.getParametricValues();
        final Set<ParametricRange> parametricRanges = savedSearch.getParametricRanges();

        final FieldText valuesFieldText = valuesToFieldText(parametricValues);
        final FieldText rangesFieldText = rangesToFieldText(parametricRanges);
        if (valuesFieldText == null && rangesFieldText == null) {
            return "";
        }

        return (valuesFieldText != null && rangesFieldText != null ? valuesFieldText.AND(rangesFieldText) : valuesFieldText != null ? valuesFieldText : rangesFieldText).toString();
    }

    private FieldText valuesToFieldText(final Collection<FieldAndValue> parametricValues) {
        if (CollectionUtils.isEmpty(parametricValues)) {
            return null;
        } else {
            final Map<String, List<String>> fieldToValues = new HashMap<>();

            for (final FieldAndValue fieldAndValue : parametricValues) {
                List<String> values = fieldToValues.get(fieldAndValue.getField());

                if (values == null) {
                    values = new LinkedList<>();
                    fieldToValues.put(fieldAndValue.getField(), values);
                }

                values.add(fieldAndValue.getValue());
            }

            final Iterator<Map.Entry<String, List<String>>> iterator = fieldToValues.entrySet().iterator();
            FieldText fieldText = fieldAndValuesToFieldText(iterator.next());

            while (iterator.hasNext()) {
                fieldText = fieldText.AND(fieldAndValuesToFieldText(iterator.next()));
            }

            return fieldText;
        }
    }

    private FieldText rangesToFieldText(final Collection<ParametricRange> parametricRanges) {
        if (CollectionUtils.isEmpty(parametricRanges)) {
            return null;
        } else {
            final Iterator<ParametricRange> iterator = parametricRanges.iterator();
            FieldText fieldText = rangeToFieldText(iterator.next());
            while (iterator.hasNext()) {
                fieldText = fieldText.AND(rangeToFieldText(iterator.next()));
            }

            return fieldText;
        }
    }

    private FieldText fieldAndValuesToFieldText(final Map.Entry<String, List<String>> fieldAndValues) {
        return new MATCH(fieldAndValues.getKey(), fieldAndValues.getValue());
    }

    private FieldText rangeToFieldText(final ParametricRange range) {
        FieldText fieldText = null;
        switch (range.getType()) {
            case Date:
                // RANGE.Type.EPOCH expects milliseconds
                fieldText = new RANGE(range.getField(), range.getMin() * 1000, range.getMax() * 1000, RANGE.Type.EPOCH);
                break;
            case Numeric:
                fieldText = new NRANGE(range.getField(), range.getMin(), range.getMax());
                break;
        }

        return fieldText;
    }
}
