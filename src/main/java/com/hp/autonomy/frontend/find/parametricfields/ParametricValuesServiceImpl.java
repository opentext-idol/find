package com.hp.autonomy.frontend.find.parametricfields;

import com.hp.autonomy.frontend.find.ApiKeyService;
import com.hp.autonomy.iod.client.api.search.FieldNames;
import com.hp.autonomy.iod.client.api.search.GetParametricValuesRequestBuilder;
import com.hp.autonomy.iod.client.api.search.GetParametricValuesService;
import com.hp.autonomy.iod.client.api.search.ParametricSort;
import com.hp.autonomy.iod.client.error.IodErrorException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class ParametricValuesServiceImpl implements ParametricValuesService {

    @Autowired
    private IndexFieldsService indexFieldsService;

    @Autowired
    private GetParametricValuesService getParametricValuesService;

    @Autowired
    private ApiKeyService apiKeyService;

    @Override
    public Set<ParametricFieldName> getAllParametricValues(final ParametricRequest parametricRequest) throws IodErrorException {
        final Set<String> consolidatedIndexFields = new HashSet<>();

        for (final String index : parametricRequest.getDatabases()) {
            consolidatedIndexFields.addAll(indexFieldsService.getParametricFields(index));
        }

        if(consolidatedIndexFields.isEmpty()) {
            return Collections.emptySet();
        }

        final String parametricIndexFieldsCsv = StringUtils.join(consolidatedIndexFields, ',');

        final Map<String, Object> parametricParams = new GetParametricValuesRequestBuilder()
                .setSort(ParametricSort.document_count)
                .setText(parametricRequest.getQueryText())
                .setFieldText(parametricRequest.getFieldText())
                .setIndexes(new ArrayList<>(parametricRequest.getDatabases()))
                .setMaxValues(5)
                .build();

        final FieldNames fieldNames = getParametricValuesService.getParametricValues(apiKeyService.getApiKey(), parametricIndexFieldsCsv, parametricParams);
        final Set<String> fieldNamesSet = fieldNames.getFieldNames();
        final Set<ParametricFieldName> parametricFieldNames = new HashSet<>();

        for (final String name : fieldNamesSet) {
            final Set<FieldNames.ValueAndCount> values = new HashSet<>(fieldNames.getValuesAndCountsForFieldName(name));
            parametricFieldNames.add(new ParametricFieldName(name, values));
        }

        return parametricFieldNames;
    }
}
