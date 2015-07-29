package com.hp.autonomy.frontend.find.parametricfields;

import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.fields.FieldType;
import com.hp.autonomy.hod.client.api.textindex.query.fields.RetrieveIndexFieldsRequestBuilder;
import com.hp.autonomy.hod.client.api.textindex.query.fields.RetrieveIndexFieldsResponse;
import com.hp.autonomy.hod.client.api.textindex.query.fields.RetrieveIndexFieldsService;
import com.hp.autonomy.hod.client.error.HodErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class IndexFieldsServiceImpl implements IndexFieldsService {

    @Autowired
    private RetrieveIndexFieldsService retrieveIndexFieldsService;

    @Override
    public Set<String> getParametricFields(final ResourceIdentifier index) throws HodErrorException {
        final RetrieveIndexFieldsRequestBuilder fieldsParams = new RetrieveIndexFieldsRequestBuilder()
                .setFieldType(FieldType.parametric);

        final RetrieveIndexFieldsResponse indexFields = retrieveIndexFieldsService.retrieveIndexFields(index, fieldsParams);

        return new HashSet<>(indexFields.getAllFields());
    }
}
