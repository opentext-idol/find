/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.savedsearches;

import com.hp.autonomy.aci.content.fieldtext.FieldText;
import com.hp.autonomy.aci.content.fieldtext.MATCH;
import com.hp.autonomy.aci.content.fieldtext.NRANGE;
import com.hp.autonomy.aci.content.fieldtext.RANGE;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Component
public class FieldTextParserImpl implements FieldTextParser {
    // WARNING: This logic is duplicated in the client side SelectedValuesCollection
    @Override
    public String toFieldText(final SavedSearch<?, ?> savedSearch) {
        final Set<FieldAndValue> parametricValues = savedSearch.getParametricValues();
        final Set<NumericRangeRestriction> numericRangeRestrictions = savedSearch.getNumericRangeRestrictions();
        final Set<DateRangeRestriction> dateRangeRestrictions = savedSearch.getDateRangeRestrictions();

        return andFieldText(Arrays.asList(
                valuesToFieldText(parametricValues),
                rangesToFieldText(numericRangeRestrictions, this::numericRangeToFieldText),
                rangesToFieldText(dateRangeRestrictions, this::dateRangeToFieldText)));
    }

    private String andFieldText(final Iterable<Optional<FieldText>> fieldTextItems) {
        final Iterator<Optional<FieldText>> iterator = fieldTextItems.iterator();
        Optional<FieldText> maybeFieldText = iterator.next();
        while (iterator.hasNext()) {
            final Optional<FieldText> nextMaybeFieldText = iterator.next();
            maybeFieldText = maybeFieldText
                    .map(fieldText -> Optional.of(nextMaybeFieldText
                            .map(fieldText::AND)
                            .orElse(fieldText)))
                    .orElse(nextMaybeFieldText);
        }
        return maybeFieldText
                .map(FieldText::toString)
                .orElse("");
    }

    private Optional<FieldText> valuesToFieldText(final Collection<FieldAndValue> parametricValues) {
        if (CollectionUtils.isEmpty(parametricValues)) {
            return Optional.empty();
        } else {
            final Map<String, List<String>> fieldToValues = new HashMap<>();

            for (final FieldAndValue fieldAndValue : parametricValues) {
                final List<String> values = fieldToValues.computeIfAbsent(fieldAndValue.getField(), key -> new LinkedList<>());
                values.add(fieldAndValue.getValue());
            }

            final Iterator<Map.Entry<String, List<String>>> iterator = fieldToValues.entrySet().iterator();
            FieldText fieldText = fieldAndValuesToFieldText(iterator.next());

            while (iterator.hasNext()) {
                fieldText = fieldText.AND(fieldAndValuesToFieldText(iterator.next()));
            }

            return Optional.of(fieldText);
        }
    }

    private <T> Optional<FieldText> rangesToFieldText(final Collection<T> rangeRestrictions,
                                            final Function<T, FieldText> toFieldText) {
        if (CollectionUtils.isEmpty(rangeRestrictions)) {
            return Optional.empty();
        } else {
            final Iterator<T> iterator = rangeRestrictions.iterator();
            FieldText fieldText = toFieldText.apply(iterator.next());
            while (iterator.hasNext()) {
                fieldText = fieldText.AND(toFieldText.apply(iterator.next()));
            }

            return Optional.of(fieldText);
        }
    }

    private FieldText fieldAndValuesToFieldText(final Map.Entry<String, List<String>> fieldAndValues) {
        return new MATCH(fieldAndValues.getKey(), fieldAndValues.getValue());
    }

    private FieldText numericRangeToFieldText(final NumericRangeRestriction range) {
        return new NRANGE(range.getField(), range.getMin(), range.getMax());
    }

    private FieldText dateRangeToFieldText(final DateRangeRestriction range) {
        return new RANGE(range.getField(), range.getMin(), range.getMax());
    }
}
