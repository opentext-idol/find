/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.parametricfields;

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.aci.content.database.Databases;
import com.hp.autonomy.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.idol.parametricvalues.IdolParametricRequest;
import com.hp.autonomy.idolutils.processors.AciResponseJaxbProcessorFactory;
import com.hp.autonomy.types.idol.FlatField;
import com.hp.autonomy.types.idol.GetQueryTagValuesResponseData;
import com.hp.autonomy.types.idol.TagValue;
import com.hp.autonomy.types.requests.idol.actions.query.params.QueryParams;
import com.hp.autonomy.types.requests.idol.actions.tags.GetQueryTagValuesParams;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagCountInfo;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import com.hp.autonomy.types.requests.idol.actions.tags.TagActions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class IdolParametricValuesService implements ParametricValuesService<IdolParametricRequest, String, AciErrorException> {
    private static final String VALUE_NODE_NAME = "value";

    private final AciService contentAciService;
    private final Processor<GetQueryTagValuesResponseData> responseProcessor;

    @Autowired
    public IdolParametricValuesService(final AciService contentAciService, final AciResponseJaxbProcessorFactory aciResponseProcessorFactory) {
        this.contentAciService = contentAciService;
        responseProcessor = aciResponseProcessorFactory.createAciResponseProcessor(GetQueryTagValuesResponseData.class);
    }

    @Override
    public Set<QueryTagInfo> getAllParametricValues(final IdolParametricRequest idolParametricRequest) throws AciErrorException {
        final AciParameters aciParameters = new AciParameters(TagActions.GetQueryTagValues.name());
        aciParameters.add(QueryParams.Text.name(), idolParametricRequest.getQueryText());
        aciParameters.add(QueryParams.FieldText.name(), idolParametricRequest.getFieldText());
        aciParameters.add(QueryParams.DatabaseMatch.name(), new Databases(idolParametricRequest.getDatabases()));
        aciParameters.add(GetQueryTagValuesParams.FieldName.name(), StringUtils.join(idolParametricRequest.getFieldNames().toArray(), ','));

        final GetQueryTagValuesResponseData responseData = contentAciService.executeAction(aciParameters, responseProcessor);
        final List<FlatField> fields = responseData.getField();
        final Set<QueryTagInfo> results = new LinkedHashSet<>(fields.size());
        for (final FlatField field : fields) {
            final List<JAXBElement<? extends Serializable>> valueElements = field.getValueOrSubvalueOrValues();
            final LinkedHashSet<QueryTagCountInfo> values = new LinkedHashSet<>(valueElements.size());
            for (final JAXBElement<?> element : valueElements) {
                if (VALUE_NODE_NAME.equals(element.getName().getLocalPart())) {
                    final TagValue tagValue = (TagValue) element.getValue();
                    values.add(new QueryTagCountInfo(tagValue.getValue(), tagValue.getCount()));
                }
            }
            results.add(new QueryTagInfo(field.getName().get(0), values));
        }
        return results;
    }
}
