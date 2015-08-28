package com.hp.autonomy.frontend.find.parametricfields;

import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.parametric.FieldNames;
import com.hp.autonomy.hod.client.api.textindex.query.parametric.GetParametricValuesRequestBuilder;
import com.hp.autonomy.hod.client.api.textindex.query.parametric.GetParametricValuesService;
import com.hp.autonomy.hod.client.api.textindex.query.parametric.ParametricSort;
import com.hp.autonomy.hod.client.error.HodErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class ParametricValuesServiceImpl implements ParametricValuesService {

    @Autowired
    private IndexFieldsService indexFieldsService;

    @Autowired
    private GetParametricValuesService getParametricValuesService;

    @Override
    public Set<ParametricFieldName> getAllParametricValues(final ParametricRequest parametricRequest) throws HodErrorException {
        final Set<String> consolidatedIndexFields = new HashSet<>();

        for (final ResourceIdentifier index : parametricRequest.getDatabases()) {
            consolidatedIndexFields.addAll(indexFieldsService.getParametricFields(index));
        }

        if(consolidatedIndexFields.isEmpty()) {
            return Collections.emptySet();
        }

        final GetParametricValuesRequestBuilder parametricParams = new GetParametricValuesRequestBuilder()
                .setSort(ParametricSort.document_count)
                .setText(parametricRequest.getQueryText())
                .setFieldText(parametricRequest.getFieldText())
                .setMaxValues(5);

        final FieldNames fieldNames = getParametricValuesService.getParametricValues(consolidatedIndexFields, parametricRequest.getDatabases(), parametricParams);
        final Set<String> fieldNamesSet = fieldNames.getFieldNames();
        final Set<ParametricFieldName> parametricFieldNames = new HashSet<>();

        for (final String name : fieldNamesSet) {
            final Set<FieldNames.ValueAndCount> values = new HashSet<>(fieldNames.getValuesAndCountsForFieldName(name));
            if(!values.isEmpty()) {
                parametricFieldNames.add(new ParametricFieldName(name, values));
            }
        }

        return parametricFieldNames;
    }
}
