/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.fieldtext;

import com.hp.autonomy.aci.content.fieldtext.EXISTS;
import com.hp.autonomy.aci.content.fieldtext.FieldText;
import com.hp.autonomy.aci.content.fieldtext.MATCH;
import com.hp.autonomy.aci.content.fieldtext.NRANGE;
import com.hp.autonomy.aci.content.fieldtext.RANGE;
import com.hp.autonomy.frontend.find.core.fields.FieldAndValue;
import com.hp.autonomy.frontend.find.core.fields.ParametricRange;
import com.hp.autonomy.searchcomponents.core.fields.FieldsMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FieldTextParserImpl implements FieldTextParser {

    private final FieldsMapper fieldsMapper;

    @Autowired
    public FieldTextParserImpl(final FieldsMapper fieldsMapper) {
        this.fieldsMapper = fieldsMapper;
    }

    @Override
    public String toFieldText(final Collection<FieldAndValue> fieldAndValues, final Collection<ParametricRange> parametricRanges, final Collection<String> parametricExists) {
        final Collection<FieldText> fieldTexts = new HashSet<>();

        if (CollectionUtils.isNotEmpty(fieldAndValues)) {
            final Collection<FieldAndValue> restoredFieldAndValues = new HashSet<>();
            fieldAndValues.forEach(fieldAndValue -> {
                final Collection<String> values = fieldsMapper.restoreFieldValue(fieldAndValue.getField(), fieldAndValue.getValue());
                values.forEach(value -> restoredFieldAndValues.add(new FieldAndValue(fieldAndValue.getField(), value)));
            });
            fieldTexts.addAll(restoredFieldAndValues.stream()
                    .collect(Collectors.groupingBy(FieldAndValue::getField)).entrySet().stream()
                    .map(entry -> parseMatchFieldText(entry.getKey(), entry.getValue())).collect(Collectors.toSet()));
        }
        if (CollectionUtils.isNotEmpty(parametricRanges)) {
            fieldTexts.addAll(parametricRanges.stream()
                    .map(this::parseRangeFieldText)
                    .collect(Collectors.toSet()));
        }
        if (CollectionUtils.isNotEmpty(parametricExists)) {
            fieldTexts.add(new EXISTS(parametricExists));
        }

        final Optional<FieldText> reducedFieldText = fieldTexts.stream().reduce(FieldText::AND);
        return reducedFieldText.isPresent() ? reducedFieldText.get().toString() : "";
    }

    private FieldText parseMatchFieldText(final String field, final Collection<FieldAndValue> values) {
        return new MATCH(field, values.stream().map(FieldAndValue::getValue).collect(Collectors.toSet()));
    }

    private FieldText parseRangeFieldText(final ParametricRange range) {
        FieldText fieldText = null;
        switch (range.getType()) {
            case Date:
                // RANGE.Type.EPOCH expects milliseconds
                // Cast to long as they should be timestamps
                fieldText = new RANGE(range.getField(), (long) range.getMin() * 1000, (long) range.getMax() * 1000, RANGE.Type.EPOCH);
                break;
            case Numeric:
                fieldText = new NRANGE(range.getField(), range.getMin(), range.getMax());
                break;
        }

        return fieldText;
    }
}
