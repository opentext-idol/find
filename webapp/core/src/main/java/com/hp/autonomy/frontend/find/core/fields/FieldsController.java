/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.fields;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.frontend.find.core.configuration.UiCustomization;
import com.hp.autonomy.searchcomponents.core.fields.FieldsRequest;
import com.hp.autonomy.searchcomponents.core.fields.FieldsService;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import com.hp.autonomy.types.requests.idol.actions.tags.ValueDetails;
import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping(FieldsController.FIELDS_PATH)
public abstract class FieldsController<R extends FieldsRequest, E extends Exception, S extends Serializable, Q extends QueryRestrictions<S>, P extends ParametricRequest<S>> {
    public static final String FIELDS_PATH = "/api/public/fields";
    public static final String GET_PARAMETRIC_FIELDS_PATH = "/parametric";
    static final String GET_PARAMETRIC_NUMERIC_FIELDS_PATH = "/parametric-numeric";
    public static final String GET_PARAMETRIC_DATE_FIELDS_PATH = "/parametric-date";

    private final FieldsService<R, E> fieldsService;
    private final ParametricValuesService<P, S, E> parametricValuesService;
    private final ObjectFactory<ParametricRequest.Builder<P, S>> parametricRequestBuilderFactory;
    private final ConfigService<? extends FindConfig> configService;

    protected FieldsController(
            final FieldsService<R, E> fieldsService,
            final ParametricValuesService<P, S, E> parametricValuesService,
            final ObjectFactory<ParametricRequest.Builder<P, S>> parametricRequestBuilderFactory,
            final ConfigService<? extends FindConfig> configService) {
        this.fieldsService = fieldsService;
        this.parametricValuesService = parametricValuesService;
        this.parametricRequestBuilderFactory = parametricRequestBuilderFactory;
        this.configService = configService;
    }

    /**
     * Create the query restrictions required to fetch the absolute min and max values for the given field request.
     */
    protected abstract Q createValueDetailsQueryRestrictions(R request);

    @RequestMapping(value = GET_PARAMETRIC_FIELDS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<TagName> getParametricFields(final R request) throws E {
        final FieldTypeParam[] fieldTypeParams = {FieldTypeParam.Parametric, FieldTypeParam.Numeric, FieldTypeParam.NumericDate};
        final Map<FieldTypeParam, List<TagName>> response = getFields(request, fieldTypeParams, Collections.emptyList());
        final List<TagName> parametricFields = new ArrayList<>(response.get(FieldTypeParam.Parametric));
        parametricFields.removeAll(response.get(FieldTypeParam.Numeric));
        parametricFields.removeAll(response.get(FieldTypeParam.NumericDate));
        return parametricFields;
    }

    @RequestMapping(value = GET_PARAMETRIC_NUMERIC_FIELDS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<FieldAndValueDetails> getParametricNumericFields(final R request) throws E {
        return fetchParametricFieldAndValueDetails(request, FieldTypeParam.Numeric, Collections.emptyList());
    }

    @RequestMapping(value = GET_PARAMETRIC_DATE_FIELDS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<FieldAndValueDetails> getParametricDateFields(final R request) throws E {
        return fetchParametricFieldAndValueDetails(request, FieldTypeParam.NumericDate, Collections.singletonList(ParametricValuesService.AUTN_DATE_FIELD));
    }

    /**
     * Fetch the parametric fields of the given type along with their min and max values.
     */
    protected List<FieldAndValueDetails> fetchParametricFieldAndValueDetails(final R request, final FieldTypeParam fieldType, final Collection<String> additionalFields) throws E {
        final FieldTypeParam[] fieldTypes = {FieldTypeParam.Parametric, fieldType};
        final Map<FieldTypeParam, List<TagName>> response = getFields(request, fieldTypes, additionalFields);
        final Collection<TagName> parametricFields = new ArrayList<>(response.get(FieldTypeParam.Parametric));
        parametricFields.retainAll(response.get(fieldType));

        parametricFields.addAll(additionalFields.stream().map(TagName::new).collect(Collectors.toList()));

        final List<String> fieldNames = parametricFields.stream().map(TagName::getId).collect(Collectors.toCollection(LinkedList::new));

        final P parametricRequest = parametricRequestBuilderFactory.getObject()
                .setFieldNames(fieldNames)
                .setQueryRestrictions(createValueDetailsQueryRestrictions(request))
                .build();

        final Map<TagName, ValueDetails> valueDetailsResponse = parametricValuesService.getValueDetails(parametricRequest);

        final List<FieldAndValueDetails> output = new LinkedList<>();

        for (final TagName tagName : parametricFields) {
            final FieldAndValueDetails.Builder builder = new FieldAndValueDetails.Builder()
                    .setId(tagName.getId())
                    .setName(tagName.getName());

            final ValueDetails valueDetails = valueDetailsResponse.get(tagName);

            if (valueDetails != null) {
                builder
                        .setMax(valueDetails.getMax())
                        .setMin(valueDetails.getMin())
                        .setTotalValues(valueDetails.getTotalValues());
            }

            output.add(builder.build());
        }

        return output;
    }

    private Map<FieldTypeParam, List<TagName>> getFields(final R request, final FieldTypeParam[] fieldTypeParams, final Collection<String> additionalFields) throws E {
        final UiCustomization uiCustomization = configService.getConfig().getUiCustomization();
        final Collection<String> parametricWhitelist = uiCustomization == null || uiCustomization.getParametricWhitelist() == null
                ? Collections.emptyList()
                : uiCustomization.getParametricWhitelist()
                .stream()
                .map(fieldName -> normaliseFieldName(fieldName, additionalFields))
                .collect(Collectors.toList());
        final Collection<String> parametricBlacklist = uiCustomization == null || uiCustomization.getParametricBlacklist() == null
                ? Collections.emptyList()
                : uiCustomization.getParametricBlacklist()
                .stream()
                .map(fieldName -> normaliseFieldName(fieldName, additionalFields))
                .collect(Collectors.toList());

        return fieldsService.getFields(request, fieldTypeParams)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()
                        .stream()
                        .filter(tagName -> (parametricWhitelist.isEmpty() || parametricWhitelist.contains(tagName.getId())) && !parametricBlacklist.contains(tagName.getId()))
                        .collect(Collectors.toList()))
                );
    }

    //TODO: this logic should be in haven-search-components or somewhere similar

    private static final String FULL_PATH_IDENTIFIER = "DOCUMENT/";

    private String normaliseFieldName(final String fieldName, final Collection<String> specialFields) {
        String normalisedFieldName = fieldName;
        if (fieldName.contains(FULL_PATH_IDENTIFIER)) {
            if (!fieldName.startsWith("/")) {
                normalisedFieldName = '/' + fieldName;
            }
        } else if (!specialFields.contains(fieldName)) {
            normalisedFieldName = '/' + FULL_PATH_IDENTIFIER + fieldName;
        }

        return normalisedFieldName;
    }
}
