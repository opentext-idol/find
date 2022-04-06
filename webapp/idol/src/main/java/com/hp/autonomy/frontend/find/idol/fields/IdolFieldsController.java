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

package com.hp.autonomy.frontend.find.idol.fields;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.frontend.find.core.fields.FieldAndValueDetails;
import com.hp.autonomy.frontend.find.core.fields.FieldComparatorFactory;
import com.hp.autonomy.frontend.find.core.fields.FieldsController;
import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import com.hp.autonomy.searchcomponents.idol.fields.IdolFieldsRequest;
import com.hp.autonomy.searchcomponents.idol.fields.IdolFieldsRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.fields.IdolFieldsService;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequest;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricValuesService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.List;

@Controller
class IdolFieldsController extends FieldsController<IdolFieldsRequest, AciErrorException, IdolQueryRestrictions, IdolParametricRequest> {
    private final ObjectFactory<IdolFieldsRequestBuilder> fieldsRequestBuilderFactory;
    private final ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory;

    @SuppressWarnings({"TypeMayBeWeakened", "ConstructorWithTooManyParameters"})
    @Autowired
    IdolFieldsController(
        final IdolFieldsService fieldsService,
        final IdolParametricValuesService parametricValuesService,
        final ObjectFactory<IdolParametricRequestBuilder> parametricRequestBuilderFactory,
        final FieldComparatorFactory fieldComparatorFactory,
        final TagNameFactory tagNameFactory,
        final ConfigService<? extends FindConfig<?, ?>> configService,
        final ObjectFactory<IdolFieldsRequestBuilder> fieldsRequestBuilderFactory,
        final ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory)
    {
        super(fieldsService, parametricValuesService, parametricRequestBuilderFactory, fieldComparatorFactory, tagNameFactory, configService);
        this.fieldsRequestBuilderFactory = fieldsRequestBuilderFactory;
        this.queryRestrictionsBuilderFactory = queryRestrictionsBuilderFactory;
    }

    @RequestMapping(value = GET_PARAMETRIC_FIELDS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public List<FieldAndValueDetails<?>> getParametricFields(
        @RequestParam(FIELD_TYPES_PARAM) final Collection<FieldTypeParam> fieldTypes)
        throws AciErrorException
    {
        return getParametricFields(fieldsRequestBuilderFactory.getObject()
                                       .fieldTypes(fieldTypes)
                                       .build());
    }

    @Override
    protected IdolQueryRestrictions createValueDetailsQueryRestrictions(final IdolFieldsRequest request) {
        return queryRestrictionsBuilderFactory.getObject()
            .queryText("*")
            .build();
    }
}
