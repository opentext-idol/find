package com.hp.autonomy.frontend.find.parametricfields;

import com.hp.autonomy.frontend.find.ApiKeyService;
import com.hp.autonomy.iod.client.api.search.FieldType;
import com.hp.autonomy.iod.client.api.search.RetrieveIndexFieldsRequestBuilder;
import com.hp.autonomy.iod.client.api.search.RetrieveIndexFieldsResponse;
import com.hp.autonomy.iod.client.api.search.RetrieveIndexFieldsService;
import com.hp.autonomy.iod.client.error.IodErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class IndexFieldsServiceImpl implements IndexFieldsService {

    @Autowired
    private RetrieveIndexFieldsService retrieveIndexFieldsService;

    @Autowired
    private ApiKeyService apiKeyService;

    @Override
    public Set<String> getParametricFields(final String index) throws IodErrorException {
        final Map<String, Object> fieldsParams = new RetrieveIndexFieldsRequestBuilder()
                .setIndex(index)
                .setFieldType(FieldType.parametric)
                .build();

        final RetrieveIndexFieldsResponse indexFields = retrieveIndexFieldsService.retrieveIndexFields(
                apiKeyService.getApiKey(),
                fieldsParams);

        return new HashSet<>(indexFields.getAllFields());
    }
}
