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

package com.hp.autonomy.frontend.find.idol.savedsearches;

import com.hp.autonomy.aci.content.fieldtext.*;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.savedsearches.*;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FieldTextParserImpl implements FieldTextParser {
    @Autowired
    private final ConfigService<IdolFindConfig> configService;

    FieldTextParserImpl(final ConfigService<IdolFindConfig> configService) {
        this.configService = configService;
    }

    // WARNING: This logic is duplicated in the client side SelectedValuesCollection
    @Override
    public String toFieldText(
        final SavedSearch<?, ?> savedSearch, final Boolean applyDocumentSelection
    ) {
        final Set<FieldAndValue> parametricValues = savedSearch.getParametricValues();
        final Set<NumericRangeRestriction> numericRangeRestrictions = savedSearch.getNumericRangeRestrictions();
        final Set<DateRangeRestriction> dateRangeRestrictions = savedSearch.getDateRangeRestrictions();
        final Optional<FieldText> documentSelectionFieldText = applyDocumentSelection ?
            documentSelectionToFieldText(
                savedSearch.getDocumentSelectionIsWhitelist(),
                savedSearch.getDocumentSelection()) :
            Optional.empty();

        return andFieldText(Arrays.asList(
                valuesToFieldText(parametricValues),
                rangesToFieldText(numericRangeRestrictions, this::numericRangeToFieldText),
                rangesToFieldText(dateRangeRestrictions, this::dateRangeToFieldText),
                documentSelectionFieldText));
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

    private Optional<FieldText> documentSelectionToFieldText(
        final boolean isWhitelist, final Set<DocumentSelection> documents
    ) {
        if (CollectionUtils.isEmpty(documents)) {
            if (isWhitelist) {
                final String uuid = UUID.randomUUID().toString();
                return Optional.of(new MATCH(configService.getConfig().getReferenceField(), uuid));
            } else {
                return Optional.empty();
            }
        } else {
            final FieldText matchAny = new MATCH(
                configService.getConfig().getReferenceField(),
                documents.stream().map(doc -> doc.getReference()).collect(Collectors.toList()));
            return Optional.of(isWhitelist ? matchAny : matchAny.NOT());
        }
    }

}
