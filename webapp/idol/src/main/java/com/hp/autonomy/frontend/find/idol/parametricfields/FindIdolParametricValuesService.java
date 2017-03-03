/*
 * Copyright 2015-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.parametricfields;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.aci.content.ranges.Range;
import com.hp.autonomy.aci.content.ranges.Ranges;
import com.hp.autonomy.searchcomponents.core.caching.CacheNames;
import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import com.hp.autonomy.searchcomponents.core.parametricvalues.BucketingParams;
import com.hp.autonomy.searchcomponents.core.parametricvalues.BucketingParamsHelper;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.idol.annotations.IdolService;
import com.hp.autonomy.searchcomponents.idol.fields.IdolFieldsRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.fields.IdolFieldsService;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequest;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricValuesService;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.QueryExecutor;
import com.hp.autonomy.types.idol.responses.FlatField;
import com.hp.autonomy.types.idol.responses.GetQueryTagValuesResponseData;
import com.hp.autonomy.types.idol.responses.RecursiveField;
import com.hp.autonomy.types.idol.responses.TagValue;
import com.hp.autonomy.types.requests.idol.actions.tags.*;
import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import com.hp.autonomy.types.requests.idol.actions.tags.params.GetQueryTagValuesParams;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default Idol implementation of {@link ParametricValuesService}
 */
@Primary
@Service
@IdolService
class FindIdolParametricValuesService implements IdolParametricValuesService {
    private static final String VALUE_NODE_NAME = "value";
    private static final String VALUES_NODE_NAME = "values";
    private static final String VALUE_MIN_NODE_NAME = "valuemin";
    private static final String VALUE_MAX_NODE_NAME = "valuemax";
    private static final String VALUE_AVERAGE_NODE_NAME = "valueaverage";
    private static final String VALUE_SUM_NODE_NAME = "valuesum";

    private final HavenSearchAciParameterHandler parameterHandler;
    private final IdolFieldsService fieldsService;
    private final ObjectFactory<IdolFieldsRequestBuilder> fieldsRequestBuilderFactory;
    private final BucketingParamsHelper bucketingParamsHelper;
    private final TagNameFactory tagNameFactory;
    private final QueryExecutor queryExecutor;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    @Autowired
    FindIdolParametricValuesService(
            final HavenSearchAciParameterHandler parameterHandler,
            final IdolFieldsService fieldsService,
            final ObjectFactory<IdolFieldsRequestBuilder> fieldsRequestBuilderFactory,
            final BucketingParamsHelper bucketingParamsHelper,
            final TagNameFactory tagNameFactory,
            final QueryExecutor queryExecutor
    ) {
        this.parameterHandler = parameterHandler;
        this.fieldsService = fieldsService;
        this.fieldsRequestBuilderFactory = fieldsRequestBuilderFactory;
        this.bucketingParamsHelper = bucketingParamsHelper;
        this.tagNameFactory = tagNameFactory;
        this.queryExecutor = queryExecutor;
    }

    @Override
    public Set<QueryTagInfo> getParametricValues(final IdolParametricRequest parametricRequest) throws AciErrorException {
        final Collection<TagName> fieldNames = new HashSet<>();
        fieldNames.addAll(parametricRequest.getFieldNames());

        if (fieldNames.isEmpty()) {
            fieldNames.addAll(lookupFields());
        }

        return fieldNames.isEmpty()
                ? Collections.emptySet()
                : getFlatFields(parametricRequest, fieldNames).stream()
                        .map(this::flatFieldToTagInfo)
                        .filter(queryTagInfo -> !queryTagInfo.getValues().isEmpty())
                        .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @Cacheable(CacheNames.PARAMETRIC_VALUES_IN_BUCKETS)
    public List<RangeInfo> getNumericParametricValuesInBuckets(final IdolParametricRequest parametricRequest, final Map<TagName, BucketingParams> bucketingParamsPerField) throws AciErrorException {
        if (parametricRequest.getFieldNames().isEmpty()) {
            return Collections.emptyList();
        } else {
            bucketingParamsHelper.validateBucketingParams(parametricRequest, bucketingParamsPerField);

            final Map<TagName, List<Double>> boundariesPerField = bucketingParamsPerField.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> bucketingParamsHelper.calculateBoundaries(entry.getValue())));

            final List<Range> ranges = boundariesPerField.entrySet().stream()
                    .map(entry -> new Range(entry.getKey().getId(), ArrayUtils.toPrimitive(entry.getValue().toArray(new Double[entry.getValue().size()]))))
                    .collect(Collectors.toList());

            final IdolParametricRequest bucketingRequest = parametricRequest.toBuilder()
                    .maxValues(null)
                    .start(1)
                    .ranges(ranges)
                    .build();

            return getFlatFields(bucketingRequest, parametricRequest.getFieldNames()).stream()
                    .map(flatFieldToRangeInfo(boundariesPerField))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<RecursiveField> getDependentParametricValues(final IdolParametricRequest parametricRequest) throws AciErrorException {
        final Collection<TagName> fieldNames = new ArrayList<>();
        fieldNames.addAll(parametricRequest.getFieldNames());
        if (fieldNames.isEmpty()) {
            fieldNames.addAll(lookupFields());
        }

        final List<RecursiveField> results;
        if (fieldNames.isEmpty()) {
            results = Collections.emptyList();
        } else {
            final AciParameters aciParameters = createAciParameters(parametricRequest.getQueryRestrictions(), parametricRequest.isModified());

            parameterHandler.addSecurityInfo(aciParameters);
            aciParameters.add(GetQueryTagValuesParams.DocumentCount.name(), true);
            aciParameters.add(GetQueryTagValuesParams.FieldName.name(), tagNamesToFieldNamesParam(fieldNames));
            aciParameters.add(GetQueryTagValuesParams.FieldDependence.name(), true);
            aciParameters.add(GetQueryTagValuesParams.FieldDependenceMultiLevel.name(), true);

            final GetQueryTagValuesResponseData responseData = executeAction(parametricRequest, aciParameters);

            results = responseData.getValues() == null ? Collections.emptyList() : responseData.getValues().getField();
        }

        return results;
    }

    @Override
    public Map<TagName, ValueDetails> getValueDetails(final IdolParametricRequest parametricRequest) throws AciErrorException {
        if (parametricRequest.getFieldNames().isEmpty()) {
            return Collections.emptyMap();
        } else {
            final AciParameters aciParameters = createAciParameters(parametricRequest.getQueryRestrictions(), parametricRequest.isModified());

            parameterHandler.addSecurityInfo(aciParameters);
            aciParameters.add(GetQueryTagValuesParams.MaxValues.name(), 1);
            aciParameters.add(GetQueryTagValuesParams.FieldName.name(), tagNamesToFieldNamesParam(parametricRequest.getFieldNames()));
            aciParameters.add(GetQueryTagValuesParams.ValueDetails.name(), true);
            aciParameters.add(GetQueryTagValuesParams.DateOffset.name(), "day");

            final GetQueryTagValuesResponseData responseData = executeAction(parametricRequest, aciParameters);
            final Collection<FlatField> fields = responseData.getField();

            final Map<TagName, ValueDetails> output = new LinkedHashMap<>();

            for (final FlatField field : fields) {
                final List<JAXBElement<? extends Serializable>> valueElements = field.getValueAndSubvalueOrValues();

                final ValueDetails.Builder builder = new ValueDetails.Builder()
                        .setTotalValues(flatFieldTotalValues(field));

                for (final JAXBElement<?> element : valueElements) {
                    final String elementLocalName = element.getName().getLocalPart();

                    if (VALUE_MIN_NODE_NAME.equals(elementLocalName)) {
                        builder.setMin((Double) element.getValue());
                    } else if (VALUE_MAX_NODE_NAME.equals(elementLocalName)) {
                        builder.setMax((Double) element.getValue());
                    } else if (VALUE_AVERAGE_NODE_NAME.equals(elementLocalName)) {
                        builder.setAverage((Double) element.getValue());
                    } else if (VALUE_SUM_NODE_NAME.equals(elementLocalName)) {
                        builder.setSum((Double) element.getValue());
                    }
                }

                final TagName tagName = tagNameFactory.buildTagName(field.getName().get(0));
                output.put(tagName, builder.build());
            }

            return output;
        }
    }

    private AciParameters createAciParameters(final IdolQueryRestrictions queryRestrictions, final boolean modified) {
        final AciParameters aciParameters = new AciParameters(TagActions.GetQueryTagValues.name());
        parameterHandler.addSearchRestrictions(aciParameters, queryRestrictions);
        if (modified) {
            parameterHandler.addQmsParameters(aciParameters, queryRestrictions);
        }
        return aciParameters;
    }

    private Collection<TagName> lookupFields() {
        return fieldsService.getFields(fieldsRequestBuilderFactory.getObject().build(), FieldTypeParam.Parametric).get(FieldTypeParam.Parametric);
    }

    private Function<FlatField, RangeInfo> flatFieldToRangeInfo(final Map<TagName, List<Double>> boundariesPerField) {
        return flatField -> {
            final TagName tagName = tagNameFactory.buildTagName(flatField.getName().get(0));

            final List<JAXBElement<? extends Serializable>> valueElements = flatField.getValueAndSubvalueOrValues();
            int count = 0;

            final List<RangeInfo.Value> values = new LinkedList<>();

            for (final JAXBElement<?> element : valueElements) {
                final String elementLocalName = element.getName().getLocalPart();

                if (VALUE_NODE_NAME.equals(elementLocalName)) {
                    final TagValue tagValue = (TagValue) element.getValue();
                    final String[] rangeValues = tagValue.getValue().split(",");
                    final double min = Double.parseDouble(rangeValues[0]);
                    final double max = Double.parseDouble(rangeValues[1]);
                    values.add(new RangeInfo.Value(tagValue.getCount(), min, max));
                } else if (VALUES_NODE_NAME.equals(elementLocalName)) {
                    count = (Integer) element.getValue();
                }
            }

            final List<Double> boundaries = boundariesPerField.get(tagName);

            // If no documents match the query parameters, GetQueryTagValues does not return any buckets
            if (values.isEmpty()) {
                values.addAll(bucketingParamsHelper.emptyBuckets(boundaries));
            }

            // All buckets have the same size, so just use the value from the first one
            final double bucketSize = boundaries.get(1) - boundaries.get(0);
            return new RangeInfo(tagName, count, boundaries.get(0), boundaries.get(boundaries.size() - 1), bucketSize, values);
        };
    }

    private QueryTagInfo flatFieldToTagInfo(final FlatField flatField) {
        final Set<QueryTagCountInfo> values = flatField.getValueAndSubvalueOrValues().stream()
                .filter(element -> VALUE_NODE_NAME.equals(element.getName().getLocalPart()))
                .map(element -> {
                    final TagValue tagValue = (TagValue) element.getValue();
                    return new QueryTagCountInfo(tagValue.getValue(), tagValue.getCount());
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));

        final String name = flatField.getName().get(0);
        return new QueryTagInfo(tagNameFactory.buildTagName(name), values, flatFieldTotalValues(flatField));
    }

    private int flatFieldTotalValues(final FlatField flatField) {
        // If no values are returned for a field, IDOL does not return a <total_values> element
        return flatField.getTotalValues() == null ? 0: flatField.getTotalValues();
    }

    private Collection<FlatField> getFlatFields(final IdolParametricRequest parametricRequest, final Collection<TagName> fieldNames) {
        final AciParameters aciParameters = new AciParameters(TagActions.GetQueryTagValues.name());
        parameterHandler.addSearchRestrictions(aciParameters, parametricRequest.getQueryRestrictions());

        if (parametricRequest.isModified()) {
            parameterHandler.addQmsParameters(aciParameters, parametricRequest.getQueryRestrictions());
        }

        parameterHandler.addSecurityInfo(aciParameters);
        aciParameters.add(GetQueryTagValuesParams.DocumentCount.name(), true);
        aciParameters.add(GetQueryTagValuesParams.Start.name(), parametricRequest.getStart());
        aciParameters.add(GetQueryTagValuesParams.MaxValues.name(), parametricRequest.getMaxValues());
        aciParameters.add(GetQueryTagValuesParams.FieldName.name(), tagNamesToFieldNamesParam(fieldNames));
        aciParameters.add(GetQueryTagValuesParams.Sort.name(), parametricRequest.getSort());
        aciParameters.add(GetQueryTagValuesParams.Ranges.name(), new Ranges(parametricRequest.getRanges()));
        aciParameters.add(GetQueryTagValuesParams.ValueDetails.name(), true);
        aciParameters.add(GetQueryTagValuesParams.TotalValues.name(), true);
        aciParameters.add(GetQueryTagValuesParams.DatePeriod.name(), "day");

        final GetQueryTagValuesResponseData responseData = executeAction(parametricRequest, aciParameters);
        return responseData.getField();
    }

    private String tagNamesToFieldNamesParam(final Collection<TagName> fieldNames) {
        return StringUtils.join(fieldNames.stream().map(TagName::getId).toArray(String[]::new), ',');
    }

    private GetQueryTagValuesResponseData executeAction(final ParametricRequest<IdolQueryRestrictions> idolParametricRequest, final AciParameters aciParameters) {
        return queryExecutor.executeGetQueryTagValues(
                aciParameters,
                idolParametricRequest.isModified() ? QueryRequest.QueryType.MODIFIED : QueryRequest.QueryType.RAW
        );
    }
}

