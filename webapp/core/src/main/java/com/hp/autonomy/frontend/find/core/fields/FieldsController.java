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

package com.hp.autonomy.frontend.find.core.fields;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.frontend.find.core.configuration.UiCustomization;
import com.hp.autonomy.searchcomponents.core.fields.FieldsRequest;
import com.hp.autonomy.searchcomponents.core.fields.FieldsService;
import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequestBuilder;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.types.requests.idol.actions.tags.FieldPath;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import com.hp.autonomy.types.requests.idol.actions.tags.ValueDetails;
import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequestMapping(FieldsController.FIELDS_PATH)
public abstract class FieldsController<R extends FieldsRequest, E extends Exception, Q extends QueryRestrictions<?>, P extends ParametricRequest<Q>> {
    public static final String FIELDS_PATH = "/api/public/fields";
    public static final String GET_PARAMETRIC_FIELDS_PATH = "/parametric";
    public static final String FIELD_TYPES_PARAM = "fieldTypes";
    private final FieldsService<R, E> fieldsService;
    private final ParametricValuesService<P, Q, E> parametricValuesService;
    private final ObjectFactory<? extends ParametricRequestBuilder<P, Q, ?>> parametricRequestBuilderFactory;
    private final FieldComparatorFactory fieldComparatorFactory;
    private final TagNameFactory tagNameFactory;
    private final ConfigService<? extends FindConfig<?, ?>> configService;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    protected FieldsController(
        final FieldsService<R, E> fieldsService,
        final ParametricValuesService<P, Q, E> parametricValuesService,
        final ObjectFactory<? extends ParametricRequestBuilder<P, Q, ?>> parametricRequestBuilderFactory,
        final FieldComparatorFactory fieldComparatorFactory,
        final TagNameFactory tagNameFactory,
        final ConfigService<? extends FindConfig<?, ?>> configService
    ) {
        this.fieldsService = fieldsService;
        this.parametricValuesService = parametricValuesService;
        this.parametricRequestBuilderFactory = parametricRequestBuilderFactory;
        this.fieldComparatorFactory = fieldComparatorFactory;
        this.tagNameFactory = tagNameFactory;
        this.configService = configService;
    }

    /**
     * Create the query restrictions required to fetch the absolute min and max values for the given field request.
     */
    protected abstract Q createValueDetailsQueryRestrictions(R request);

    protected List<FieldAndValueDetails<?>> getParametricFields(final R request) throws E {
        final Predicate<TagName> predicate = alwaysAndNeverShowFilter();
        final Map<FieldTypeParam, Set<TagName>> response = fieldsService.getFields(request).entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().filter(predicate).collect(Collectors.toSet())));
        final TagName autnDateField;
        if(request.getFieldTypes().contains(FieldTypeParam.NumericDate) && predicate.test(autnDateField = tagNameFactory.buildTagName(ParametricValuesService.AUTN_DATE_FIELD))) {
            response.compute(FieldTypeParam.NumericDate, (key, maybeValue) -> Optional.ofNullable(maybeValue)
                .map(value -> {
                    value.add(autnDateField);
                    return value;
                })
                .orElse(Collections.singleton(autnDateField))
            );
        }

        final List<FieldAndValueDetails<?>> output = new ArrayList<>();
        output.addAll(fetchDateParametricFieldAndValueDetails(request, response));
        output.addAll(fetchNumericParametricFieldAndValueDetails(request, response));
        output.addAll(fetchParametricFieldAndValueDetails(response));
        output.sort(fieldComparatorFactory.parametricFieldComparator());

        return output;
    }

    private <T extends Comparable<? super T> & Serializable> Collection<FieldAndValueDetails<T>> fetchParametricFieldAndValueDetails(final Map<FieldTypeParam, Set<TagName>> response) throws E {
        return fetchParametricFieldAndValueDetails(FieldTypeParam.Parametric, response, tagNames -> Collections.emptyMap());
    }

    private Collection<FieldAndValueDetails<Double>> fetchNumericParametricFieldAndValueDetails(final R request,
                                                                                                final Map<FieldTypeParam, Set<TagName>> response) throws E {
        return fetchParametricFieldAndValueDetails(FieldTypeParam.Numeric, response, tagNames -> {
            // Fetch the value details for the fields
            final P parametricRequest = parametricRequestBuilderFactory.getObject()
                .fieldNames(tagNames.stream()
                                .map(TagName::getId)
                                .collect(Collectors.toList()))
                .queryRestrictions(createValueDetailsQueryRestrictions(request))
                .build();

            return parametricValuesService.getNumericValueDetails(parametricRequest);
        });
    }

    private Collection<FieldAndValueDetails<ZonedDateTime>> fetchDateParametricFieldAndValueDetails(
        final R request,
        final Map<FieldTypeParam, Set<TagName>> response
    ) throws E {
        return fetchParametricFieldAndValueDetails(FieldTypeParam.NumericDate, response, tagNames -> {
            // Fetch the value details for the fields
            final P parametricRequest = parametricRequestBuilderFactory.getObject()
                .fieldNames(tagNames.stream()
                                .map(TagName::getId)
                                .collect(Collectors.toList()))
                .queryRestrictions(createValueDetailsQueryRestrictions(request))
                .build();

            return parametricValuesService.getDateValueDetails(parametricRequest);
        });
    }

    /**
     * Fetch the parametric fields of the given type along with their min and max values.
     */
    private <T extends Comparable<? super T> & Serializable, V extends ValueDetails<T>>
    Collection<FieldAndValueDetails<T>> fetchParametricFieldAndValueDetails(
        final FieldTypeParam fieldType,
        final Map<FieldTypeParam, Set<TagName>> response,
        final ValueDetailsFetch<T, V, E> valueDetailsFetch
    ) throws E {
        if(!response.containsKey(fieldType)) {
            return Collections.emptyList();
        }

        final Set<TagName> tagNames = response.get(fieldType);
        tagNames.forEach(tagName -> response.entrySet()
            .stream()
            .filter(entry -> entry.getKey() != fieldType)
            .forEach(entry -> entry.getValue().remove(tagName)));

        final Map<FieldPath, V> valueDetailsResponse = valueDetailsFetch.fetch(tagNames);

        return tagNames.stream()
            .map(tagName -> {
                final FieldAndValueDetails.FieldAndValueDetailsBuilder<T> builder = FieldAndValueDetails.<T>builder()
                    .id(tagName.getId().getNormalisedPath())
                    .displayName(tagName.getDisplayName())
                    .type(fieldType);

                final V valueDetails = valueDetailsResponse.get(tagName.getId());

                if(valueDetails != null) {
                    builder
                        .max(valueDetails.getMax())
                        .min(valueDetails.getMin())
                        .totalValues(valueDetails.getTotalValues());
                }

                return builder.build();
            })
            .collect(Collectors.toList());
    }

    /**
     * @return A function which returns true if the TagName matches should be displayed after applying the always and never show lists
     */
    private Predicate<TagName> alwaysAndNeverShowFilter() {
        final UiCustomization maybeUiCustomization = configService.getConfig().getUiCustomization();
        final Collection<FieldPath> parametricAlwaysShow = Optional.ofNullable(maybeUiCustomization)
            .map(UiCustomization::getParametricAlwaysShow)
            .orElse(Collections.emptyList());
        final Collection<FieldPath> parametricNeverShow = Optional.ofNullable(maybeUiCustomization)
            .map(UiCustomization::getParametricNeverShow)
            .orElse(Collections.emptyList());

        return tagName -> (parametricAlwaysShow.isEmpty() || parametricAlwaysShow.contains(tagName.getId())) && !parametricNeverShow.contains(tagName.getId());
    }

    @FunctionalInterface
    private interface ValueDetailsFetch<T extends Comparable<? super T> & Serializable, V extends ValueDetails<T>, E extends Exception> {
        Map<FieldPath, V> fetch(final Set<TagName> tagNames) throws E;
    }
}
